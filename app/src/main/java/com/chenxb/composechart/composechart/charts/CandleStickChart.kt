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
import androidx.compose.ui.geometry.Size
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
import com.chenxb.composechart.composechart.data.CandleChartData
import com.chenxb.composechart.composechart.data.CandleDataSet
import com.chenxb.composechart.composechart.data.CandleEntry
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
import com.chenxb.composechart.composechart.style.CandleChartStyle
import com.chenxb.composechart.composechart.components.drawValueLabel
import com.chenxb.composechart.composechart.components.HighlightStyle
import com.chenxb.composechart.composechart.components.EmptyDataConfig
import com.chenxb.composechart.composechart.components.EmptyDataView
import kotlin.math.min

/**
 * K线图 (CandleStick Chart)
 */
@Composable
fun CandleStickChart(
    data: CandleChartData,
    modifier: Modifier = Modifier,
    style: CandleChartStyle = CandleChartStyle(),
    xAxisConfig: XAxisConfig = XAxisConfig(),
    yAxisConfig: YAxisConfig = YAxisConfig(),
    yAxisConfigRight: YAxisConfig? = null,
    legendStyle: LegendStyle = LegendStyle(),
    touchHitSlop: Float = 50f,
    enableGestures: Boolean = false,
    highlightStyle: HighlightStyle = HighlightStyle.Default,
    emptyDataConfig: EmptyDataConfig = EmptyDataConfig.Default,
    animationProgress: Float = 1f,
    onValueSelected: (CandleEntry, Highlight) -> Unit = { _, _ -> },
    onValueDeselected: () -> Unit = {}
) {
    var chartSize by remember { mutableStateOf(IntSize.Zero) }
    var gestureState by remember { mutableStateOf(ChartGestureState()) }
    var selectedHighlight by remember { mutableStateOf<Highlight?>(null) }
    // Legend 真实高度（px），由 onSizeChanged 测量
    var legendHeightPx by remember { mutableStateOf(0f) }
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    // 检查数据是否为空
    val isEmpty = data.getDataSets().isEmpty() || data.getDataSets().all { it.getEntries().isEmpty() }
    if (isEmpty && emptyDataConfig.enabled) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(style.baseStyle.backgroundColor)
        ) {
            EmptyDataView(
                config = emptyDataConfig,
                modifier = Modifier.fillMaxSize()
            )
        }
        return
    }

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
        CandleDataRange(
            xMin = data.getX最小值(),
            xMax = data.getX最大值(),
            yMin = data.getY最小值(),
            yMax = data.getY最大值()
        )
    }

    // 缓存 Stroke 对象
    val candleStroke = remember { Stroke(width = 1f) }

    // 缓存K线数据
    val cachedCandles by remember(data, chartBounds, style, animationProgress) {
        derivedStateOf {
            if (chartBounds == null) {
                emptyList()
            } else {
                val result = mutableListOf<CachedCandleInfo>()
                val xRange = if (dataRange.xMax != dataRange.xMin) dataRange.xMax - dataRange.xMin else 1f
                val yRange = if (dataRange.yMax != dataRange.yMin) dataRange.yMax - dataRange.yMin else 1f

                // 使用分组模式计算，与 centerLabels 一致
                val groupCount = (dataRange.xMax - dataRange.xMin + 1).toInt()
                val groupWidth = chartBounds.width / groupCount

                for (dataSet in data.getDataSets()) {
                    val entries = dataSet.getEntries()
                    if (entries.isEmpty()) continue

                    val candleWidth = groupWidth * 0.7f

                    for (entry in entries) {
                        // 分组模式：x 坐标居中在组内
                        val groupIndex = entry.x.toInt() - dataRange.xMin.toInt()
                        val x = chartBounds.left + groupIndex * groupWidth + groupWidth / 2
                        val highY = chartBounds.bottom - ((entry.high - dataRange.yMin) / yRange) * chartBounds.height
                        val lowY = chartBounds.bottom - ((entry.low - dataRange.yMin) / yRange) * chartBounds.height
                        val openY = chartBounds.bottom - ((entry.open - dataRange.yMin) / yRange) * chartBounds.height
                        val closeY = chartBounds.bottom - ((entry.close - dataRange.yMin) / yRange) * chartBounds.height

                        val isIncreasing = entry.close >= entry.open
                        val candleColor = if (isIncreasing) style.increasingColor else style.decreasingColor

                        result.add(
                            CachedCandleInfo(
                                x = x,
                                highY = highY,
                                lowY = lowY,
                                openY = openY,
                                closeY = closeY,
                                candleWidth = candleWidth,
                                isIncreasing = isIncreasing,
                                candleColor = candleColor,
                                shadowColor = if (dataSet.shadowColorSameAsCandle) candleColor else dataSet.shadowColor,
                                shadowWidth = dataSet.shadowWidth,
                                paintStyle = if (isIncreasing) dataSet.increasingPaintStyle else dataSet.decreasingPaintStyle,
                                closeValue = if (style.isDrawValues) entry.close else null
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
                            var closestEntry: CandleEntry? = null
                            var closestDistance = Float.MAX_VALUE
                            val touchThreshold = touchHitSlop

                            for (dataSet in data.getDataSets()) {
                                val entries = dataSet.getEntries()
                                for (entry in entries) {
                                    val pointX = b.left + ((entry.x - dataXMin) / xRange) * chartWidth
                                    val midY = b.bottom - ((entry.high + entry.low) / 2 - dataYMin) / yRange * chartHeight

                                    val distance = kotlin.math.sqrt(
                                        (offset.x - pointX) * (offset.x - pointX) +
                                        (offset.y - midY) * (offset.y - midY)
                                    )

                                    if (distance < closestDistance && distance < touchThreshold) {
                                        closestDistance = distance
                                        closestEntry = entry
                                    }
                                }
                            }

                            if (closestEntry != null) {
                                val pointX = b.left + ((closestEntry.x - dataXMin) / xRange) * chartWidth
                                val midY = b.bottom - ((closestEntry.high + closestEntry.low) / 2 - dataYMin) / yRange * chartHeight

                                val highlight = Highlight(
                                    x = pointX,
                                    y = midY,
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

            // 绘制图表内容
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                drawCandleStickChartContentOptimized(
                    bounds = b,
                    style = style,
                    highlightStyle = highlightStyle,
                    animationProgress = animationProgress,
                    gestureState = gestureState,
                    highlight = selectedHighlight,
                    textMeasurer = textMeasurer,
                    cachedCandles = cachedCandles,
                    candleStroke = candleStroke
                )
            }
        }

        // 绘制图例 - 使用 Alignment.BottomStart 钉在底部
        if (legendStyle.isEnabled) {
            val legendEntries = data.getDataSets().map { dataSet ->
                LegendEntry(
                    label = dataSet.label,
                    color = dataSet.color,
                    form = LegendForm.SQUARE,
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
private data class CandleDataRange(
    val xMin: Float,
    val xMax: Float,
    val yMin: Float,
    val yMax: Float
)

/**
 * 缓存的K线信息
 */
private data class CachedCandleInfo(
    val x: Float,
    val highY: Float,
    val lowY: Float,
    val openY: Float,
    val closeY: Float,
    val candleWidth: Float,
    val isIncreasing: Boolean,
    val candleColor: Color,
    val shadowColor: Color,
    val shadowWidth: Float,
    val paintStyle: CandleDataSet.PaintStyle,
    val closeValue: Float?
)

/**
 * 优化后的绘制函数
 */
private fun DrawScope.drawCandleStickChartContentOptimized(
    bounds: ChartBounds,
    style: CandleChartStyle,
    highlightStyle: HighlightStyle,
    animationProgress: Float,
    gestureState: ChartGestureState,
    highlight: Highlight?,
    textMeasurer: TextMeasurer,
    cachedCandles: List<CachedCandleInfo>,
    candleStroke: Stroke
) {
    if (cachedCandles.isEmpty()) return

    // 应用手势变换
    translate(left = gestureState.translationX, top = gestureState.translationY) {
        scale(scaleX = gestureState.scaleX, scaleY = gestureState.scaleY, pivot = bounds.center) {

            for (candle in cachedCandles) {
                // 绘制上下影线
                drawLine(
                    color = candle.shadowColor,
                    start = Offset(candle.x, candle.highY),
                    end = Offset(candle.x, candle.lowY),
                    strokeWidth = candle.shadowWidth
                )

                // 绘制蜡烛体
                val bodyTop = min(candle.openY, candle.closeY)
                val bodyHeight = kotlin.math.abs(candle.openY - candle.closeY).coerceAtLeast(1f)

                if (candle.paintStyle == CandleDataSet.PaintStyle.FILL) {
                    drawRect(
                        color = candle.candleColor,
                        topLeft = Offset(candle.x - candle.candleWidth / 2, bodyTop),
                        size = Size(candle.candleWidth, bodyHeight * animationProgress)
                    )
                } else {
                    drawRect(
                        color = candle.candleColor,
                        topLeft = Offset(candle.x - candle.candleWidth / 2, bodyTop),
                        size = Size(candle.candleWidth, bodyHeight * animationProgress),
                        style = Stroke(width = candle.shadowWidth)
                    )
                }

                // 绘制数值标签
                candle.closeValue?.let { value ->
                    drawValueLabel(
                        textMeasurer = textMeasurer,
                        value = value,
                        x = candle.x,
                        y = candle.highY - 15f,
                        textColor = style.valueTextColor,
                        textSize = style.valueTextSize
                    )
                }
            }

            // 绘制高亮
            highlight?.let { h ->
                // 绘制水平高亮线
                if (highlightStyle.drawHorizontalHighlightLine) {
                    drawLine(
                        color = highlightStyle.highlightLineColor,
                        start = Offset(bounds.left, h.y),
                        end = Offset(bounds.right, h.y),
                        strokeWidth = highlightStyle.highlightLineWidth,
                        pathEffect = highlightStyle.highlightLineDashPathEffect
                    )
                }

                // 绘制垂直高亮线
                if (highlightStyle.drawVerticalHighlightLine) {
                    drawLine(
                        color = highlightStyle.highlightLineColor,
                        start = Offset(h.x, bounds.top),
                        end = Offset(h.x, bounds.bottom),
                        strokeWidth = highlightStyle.highlightLineWidth,
                        pathEffect = highlightStyle.highlightLineDashPathEffect
                    )
                }

                // 绘制高亮点
                if (highlightStyle.drawHighlightPoint) {
                    drawCircle(
                        color = highlightStyle.highlightPointOuterColor,
                        radius = highlightStyle.highlightPointOuterRadius,
                        center = Offset(h.x, h.y)
                    )
                    drawCircle(
                        color = highlightStyle.highlightPointInnerColor,
                        radius = highlightStyle.highlightPointInnerRadius,
                        center = Offset(h.x, h.y)
                    )
                }
            }
        }
    }
}
