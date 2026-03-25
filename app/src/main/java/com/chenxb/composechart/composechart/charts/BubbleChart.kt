package com.chenxb.composechart.composechart.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.TextMeasurer
import com.chenxb.composechart.composechart.ChartBounds
import com.chenxb.composechart.composechart.data.BubbleChartData
import com.chenxb.composechart.composechart.data.BubbleDataSet
import com.chenxb.composechart.composechart.data.BubbleEntry
import com.chenxb.composechart.composechart.components.Highlight
import com.chenxb.composechart.composechart.components.LegendEntry
import com.chenxb.composechart.composechart.components.LegendForm
import com.chenxb.composechart.composechart.components.LegendStyle
import com.chenxb.composechart.composechart.components.LegendView
import com.chenxb.composechart.composechart.components.XAxisConfig
import com.chenxb.composechart.composechart.components.XAxisView
import com.chenxb.composechart.composechart.components.YAxisConfig
import com.chenxb.composechart.composechart.components.YAxisView
import com.chenxb.composechart.composechart.gestures.ChartGestureState
import com.chenxb.composechart.composechart.style.BubbleChartStyle
import com.chenxb.composechart.composechart.components.drawValueLabel
import kotlin.math.ln
import kotlin.math.min

/**
 * 气泡图
 */
@Composable
fun BubbleChart(
    data: BubbleChartData,
    modifier: Modifier = Modifier,
    style: BubbleChartStyle = BubbleChartStyle(),
    xAxisConfig: XAxisConfig = XAxisConfig(),
    yAxisConfig: YAxisConfig = YAxisConfig(),
    yAxisConfigRight: YAxisConfig? = null,
    legendStyle: LegendStyle = LegendStyle(),
    touchHitSlop: Float = 50f,
    enableGestures: Boolean = false,
    animationProgress: Float = 1f,
    onValueSelected: (BubbleEntry, Highlight) -> Unit = { _, _ -> },
    onValueDeselected: () -> Unit = {}
) {
    var chartSize by remember { mutableStateOf(IntSize.Zero) }
    var gestureState by remember { mutableStateOf(ChartGestureState()) }
    var selectedHighlight by remember { mutableStateOf<Highlight?>(null) }
    // Legend 真实高度（px），由 onSizeChanged 测量
    var legendHeightPx by remember { mutableStateOf(0f) }
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    // 计算绘图区域
    val axisLeftWidth = 60f
    val axisRightWidth = if (yAxisConfigRight != null) 60f else 20f
    val axisTopHeight = 30f
    val xAxisHeight = 40f
    // 底部高度 = X轴高度 + Legend真实高度（如果启用）
    val axisBottomHeight = xAxisHeight + if (legendStyle.isEnabled) legendHeightPx else 0f

    val chartBounds = remember(chartSize.width, chartSize.height, yAxisConfigRight, legendStyle.isEnabled, legendHeightPx) {
        if (chartSize.width > 0 && chartSize.height > 0) {
            ChartBounds(
                left = axisLeftWidth,
                top = axisTopHeight,
                right = chartSize.width - axisRightWidth,
                bottom = chartSize.height - axisBottomHeight
            )
        } else null
    }

    // ========== 性能优化：缓存计算结果 ==========

    // 缓存数据范围
    val dataRange = remember(data) {
        BubbleDataRange(
            xMin = data.getX最小值(),
            xMax = data.getX最大值(),
            yMin = data.getY最小值(),
            yMax = data.getY最大值()
        )
    }

    // 缓存 Stroke 对象
    val highlightStroke = remember { Stroke(width = 2f) }

    // 缓存气泡坐标和大小
    val cachedBubbles by remember(data, chartBounds, style, animationProgress) {
        derivedStateOf {
            if (chartBounds == null) {
                emptyList()
            } else {
                val result = mutableListOf<CachedBubbleInfo>()
                val yRange = if (dataRange.yMax != dataRange.yMin) dataRange.yMax - dataRange.yMin else 1f
                val maxSize = style.maxSize

                // 使用分组模式计算，与 centerLabels 一致
                val groupCount = (dataRange.xMax - dataRange.xMin + 1).toInt()
                val groupWidth = chartBounds.width / groupCount

                for (dataSet in data.getDataSets()) {
                    for (entry in dataSet.getEntries()) {
                        // 分组模式：x 坐标居中在组内
                        val groupIndex = entry.x.toInt() - dataRange.xMin.toInt()
                        val x = chartBounds.left + groupIndex * groupWidth + groupWidth / 2
                        val y = chartBounds.bottom - ((entry.y - dataRange.yMin) / yRange) * chartBounds.height * animationProgress

                        val normalizedSize = if (style.normalizeSize && maxSize > 0) {
                            val normalized = ln(entry.size + 1) / ln(maxSize + 1)
                            normalized * maxSize
                        } else {
                            entry.size.coerceIn(5f, maxSize)
                        }

                        val bubbleRadius = normalizedSize * animationProgress

                        result.add(
                            CachedBubbleInfo(
                                x = x,
                                y = y,
                                radius = bubbleRadius,
                                color = dataSet.color,
                                highlightColor = dataSet.highlightColor.copy(alpha = dataSet.highlightAlpha),
                                value = if (style.isDrawValues && bubbleRadius > 10f) entry.y else null
                            )
                        )
                    }
                }
                result
            }
        }
    }

    LaunchedEffect(animationProgress) {
        // 动画效果处理
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(style.baseStyle.backgroundColor)
            .onSizeChanged { chartSize = it }
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    if (enableGestures) {
                        gestureState = gestureState.copy(
                            scaleX = (gestureState.scaleX * zoom).coerceIn(0.5f, 5f),
                            scaleY = (gestureState.scaleY * zoom).coerceIn(0.5f, 5f),
                            translationX = gestureState.translationX + pan.x,
                            translationY = gestureState.translationY + pan.y
                        )
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        if (!enableGestures) return@detectTapGestures
                        chartBounds?.let { b ->
                            val dataXMin = data.getX最小值()
                            val dataXMax = data.getX最大值()
                            val dataYMin = data.getY最小值()
                            val dataYMax = data.getY最大值()

                            val xRange = dataXMax - dataXMin
                            val yRange = dataYMax - dataYMin
                            val chartWidth = b.width
                            val chartHeight = b.height

                            // 查找最近的数据点
                            var closestEntry: BubbleEntry? = null
                            var closestDistance = Float.MAX_VALUE
                            val touchThreshold = touchHitSlop

                            for (dataSet in data.getDataSets()) {
                                val entries = dataSet.getEntries()
                                for (entry in entries) {
                                    val pointX = b.left + ((entry.x - dataXMin) / xRange) * chartWidth
                                    val pointY = b.bottom - ((entry.y - dataYMin) / yRange) * chartHeight

                                    val distance = kotlin.math.sqrt(
                                        (offset.x - pointX) * (offset.x - pointX) +
                                        (offset.y - pointY) * (offset.y - pointY)
                                    )

                                    if (distance < closestDistance && distance < touchThreshold) {
                                        closestDistance = distance
                                        closestEntry = entry
                                    }
                                }
                            }

                            if (closestEntry != null) {
                                val pointX = b.left + ((closestEntry.x - dataXMin) / xRange) * chartWidth
                                val pointY = b.bottom - ((closestEntry.y - dataYMin) / yRange) * chartHeight

                                val highlight = Highlight(
                                    x = pointX,
                                    y = pointY,
                                    xIndex = closestEntry.x.toInt(),
                                    dataSetIndex = 0
                                )
                                selectedHighlight = highlight
                                onValueSelected(closestEntry, highlight)
                            } else {
                                selectedHighlight = null
                                onValueDeselected()
                            }
                        }
                    }
                )
            }
    ) {
        chartBounds?.let { b ->
            // 绘制Y轴
            YAxisView(
                config = yAxisConfig,
                bounds = b,
                yRangeMin = data.getY最小值(),
                yRangeMax = data.getY最大值(),
                modifier = Modifier.fillMaxSize(),
                isLeft = true,
                limitLines = yAxisConfig.limitLines
            )

            // 绘制右侧Y轴（如果配置了）
            yAxisConfigRight?.let { rightConfig ->
                YAxisView(
                    config = rightConfig,
                    bounds = b,
                    yRangeMin = data.getY最小值(),
                    yRangeMax = data.getY最大值(),
                    modifier = Modifier.fillMaxSize(),
                    isLeft = false,
                    limitLines = rightConfig.limitLines
                )
            }

            // 绘制X轴
            XAxisView(
                config = xAxisConfig.copy(centerLabels = true),
                bounds = b,
                xRangeMin = data.getX最小值(),
                xRangeMax = data.getX最大值(),
                modifier = Modifier.fillMaxSize()
            )
        }

        // 绘制图表内容
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            chartBounds?.let { b ->
                drawBubbleChartContentOptimized(
                    bounds = b,
                    style = style,
                    gestureState = gestureState,
                    highlight = selectedHighlight,
                    textMeasurer = textMeasurer,
                    cachedBubbles = cachedBubbles,
                    highlightStroke = highlightStroke
                )
            }
        }

        // 绘制图例 - 使用 Alignment.BottomStart 钉在底部
        if (legendStyle.isEnabled) {
            val legendEntries = data.getDataSets().map { dataSet ->
                LegendEntry(
                    label = dataSet.label,
                    color = dataSet.color,
                    form = LegendForm.CIRCLE,
                    formSize = 12f
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .onSizeChanged { size ->
                        if (size.height.toFloat() != legendHeightPx) {
                            legendHeightPx = size.height.toFloat()
                        }
                    }
            ) {
                LegendView(
                    legendStyle = legendStyle,
                    legendEntries = legendEntries,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ========== 缓存数据类 ==========

/**
 * 缓存的数据范围
 */
private data class BubbleDataRange(
    val xMin: Float,
    val xMax: Float,
    val yMin: Float,
    val yMax: Float
)

/**
 * 缓存的气泡信息
 */
private data class CachedBubbleInfo(
    val x: Float,
    val y: Float,
    val radius: Float,
    val color: Color,
    val highlightColor: Color,
    val value: Float?
)

/**
 * 优化后的绘制函数
 */
private fun DrawScope.drawBubbleChartContentOptimized(
    bounds: ChartBounds,
    style: BubbleChartStyle,
    gestureState: ChartGestureState,
    highlight: Highlight?,
    textMeasurer: TextMeasurer,
    cachedBubbles: List<CachedBubbleInfo>,
    highlightStroke: Stroke
) {
    if (cachedBubbles.isEmpty()) return

    // 应用手势变换
    translate(left = gestureState.translationX, top = gestureState.translationY) {
        scale(scaleX = gestureState.scaleX, scaleY = gestureState.scaleY, pivot = bounds.center) {

            // 绘制所有气泡
            for (bubble in cachedBubbles) {
                // 绘制气泡
                drawCircle(
                    color = bubble.color,
                    radius = bubble.radius,
                    center = Offset(bubble.x, bubble.y)
                )

                // 绘制高亮边框
                drawCircle(
                    color = bubble.highlightColor,
                    radius = bubble.radius + 2f,
                    center = Offset(bubble.x, bubble.y),
                    style = highlightStroke
                )

                // 绘制数值标签
                bubble.value?.let { value ->
                    drawValueLabel(
                        textMeasurer = textMeasurer,
                        value = value,
                        x = bubble.x,
                        y = bubble.y,
                        textColor = style.valueTextColor,
                        textSize = style.valueTextSize
                    )
                }
            }

            // 绘制高亮
            highlight?.let { h ->
                drawCircle(
                    color = style.highlightColor,
                    radius = 15f,
                    center = Offset(h.x, h.y)
                )
                drawCircle(
                    color = Color.White,
                    radius = 8f,
                    center = Offset(h.x, h.y)
                )
            }
        }
    }
}
