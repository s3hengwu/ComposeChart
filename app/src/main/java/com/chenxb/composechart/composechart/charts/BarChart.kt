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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
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
import com.chenxb.composechart.composechart.data.BarChartData
import com.chenxb.composechart.composechart.data.BarDataSet
import com.chenxb.composechart.composechart.data.BarEntry
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
import com.chenxb.composechart.composechart.style.BarChartStyle
import com.chenxb.composechart.composechart.components.drawValueLabel
import com.chenxb.composechart.composechart.components.HighlightStyle
import com.chenxb.composechart.composechart.components.EmptyDataConfig
import com.chenxb.composechart.composechart.components.EmptyDataView
import com.chenxb.composechart.composechart.components.ChartDescription
import com.chenxb.composechart.composechart.components.DescriptionView

/**
 * 柱状图
 */
@Composable
fun BarChart(
    data: BarChartData,
    modifier: Modifier = Modifier,
    style: BarChartStyle = BarChartStyle(),
    xAxisConfig: XAxisConfig = XAxisConfig(),
    yAxisConfig: YAxisConfig = YAxisConfig(),
    yAxisConfigRight: YAxisConfig? = null,
    legendStyle: LegendStyle = LegendStyle(),
    touchHitSlop: Float = 50f,
    enableGestures: Boolean = false,
    highlightStyle: HighlightStyle = HighlightStyle.Default,
    emptyDataConfig: EmptyDataConfig = EmptyDataConfig.Default,
    description: ChartDescription = ChartDescription.Default,
    animationProgress: Float = 1f,
    onValueSelected: (BarEntry, Highlight) -> Unit = { _, _ -> },
    onValueDeselected: () -> Unit = {}
) {
    var chartSize by remember { mutableStateOf(IntSize.Zero) }
    var gestureState by remember { mutableStateOf(ChartGestureState()) }
    var selectedHighlight by remember { mutableStateOf<Highlight?>(null) }
    var selectedBarInfo by remember { mutableStateOf<Pair<Float, Float>?>(null) } // pair of (barX, barWidth)
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
        BarChartDataRange(
            xMin = data.getX最小值(),
            xMax = data.getX最大值(),
            yMin = 0f,
            yMax = data.getY最大值() * 1.1f
        )
    }

    // 缓存柱状图尺寸计算
    val barLayout by remember(data, chartBounds, style) {
        derivedStateOf {
            if (chartBounds == null) {
                null
            } else {
                val groupSpace = style.groupSpace
                val barSpace = style.barSpace
                val dataSetCount = data.getDataSetCount()
                val groupWidth = chartBounds.width / (dataRange.xMax - dataRange.xMin + 1)
                val barItemWidth = groupWidth * (1 - groupSpace - barSpace * (dataSetCount - 1)) / dataSetCount
                BarLayout(
                    groupWidth = groupWidth,
                    barItemWidth = barItemWidth,
                    barSpace = barSpace,
                    dataSetCount = dataSetCount
                )
            }
        }
    }

    // 缓存柱状图坐标
    val cachedBars by remember(data, chartBounds, barLayout, animationProgress) {
        derivedStateOf {
            val layout = barLayout
            if (chartBounds == null || layout == null) {
                emptyList()
            } else {
                val result = mutableListOf<CachedBarInfo>()
                var currentGroupIndex = 0

                for ((dataSetIndex, dataSet) in data.getDataSets().withIndex()) {
                    val entries = dataSet.getEntries()
                    for ((entryIndex, entry) in entries.withIndex()) {
                        val x = chartBounds.left + currentGroupIndex * layout.groupWidth +
                                entryIndex * layout.barItemWidth * layout.dataSetCount +
                                dataSetIndex * layout.barItemWidth

                        // 检查是否是堆叠柱状图
                        if (entry.yStack != null && entry.yStack.isNotEmpty()) {
                            var stackBottom = chartBounds.bottom
                            for ((stackIndex, stackValue) in entry.yStack.withIndex()) {
                                val stackHeight = (stackValue / dataRange.yMax) * chartBounds.height * animationProgress
                                val stackColor = dataSet.stackColors?.getOrNull(stackIndex) ?: dataSet.color
                                result.add(
                                    CachedBarInfo(
                                        x = x,
                                        y = stackBottom - stackHeight,
                                        width = layout.barItemWidth - layout.barSpace,
                                        height = stackHeight,
                                        color = stackColor,
                                        cornerRadius = if (stackIndex == entry.yStack.size - 1) 4f else 0f,
                                        value = if (stackIndex == entry.yStack.size - 1) entry.y else null,
                                        entry = entry
                                    )
                                )
                                stackBottom -= stackHeight
                            }
                        } else {
                            val barHeight = (entry.y / dataRange.yMax) * chartBounds.height * animationProgress
                            result.add(
                                CachedBarInfo(
                                    x = x,
                                    y = chartBounds.bottom - barHeight,
                                    width = layout.barItemWidth - layout.barSpace,
                                    height = barHeight,
                                    color = dataSet.color,
                                    cornerRadius = 4f,
                                    value = entry.y,
                                    entry = entry
                                )
                            )
                        }
                        currentGroupIndex++
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
                            val dataYMin = 0f
                            val dataYMax = data.getY最大值() * 1.1f

                            val xRange = dataXMax - dataXMin
                            val yRange = dataYMax - dataYMin
                            val chartWidth = b.width
                            val chartHeight = b.height

                            // 查找最近的数据点
                            var closestEntry: BarEntry? = null
                            var closestGroupIndex = 0
                            var closestEntryIndex = 0
                            var closestDataSetIndex = 0
                            var closestDistance = Float.MAX_VALUE
                            val touchThreshold = touchHitSlop

                            val groupSpace = style.groupSpace
                            val barSpace = style.barSpace
                            val dataSetCount = data.getDataSetCount()
                            val groupWidth = chartWidth / (xRange + 1)
                            val barItemWidth = groupWidth * (1 - groupSpace - barSpace * (dataSetCount - 1)) / dataSetCount

                            var currentGroupIndex = 0
                            for ((dataSetIndex, dataSet) in data.getDataSets().withIndex()) {
                                val entries = dataSet.getEntries()
                                for ((entryIndex, entry) in entries.withIndex()) {
                                    val barHeight = (entry.y / dataYMax) * chartHeight
                                    val x = b.left + currentGroupIndex * groupWidth +
                                            entryIndex * barItemWidth * dataSetCount +
                                            dataSetIndex * barItemWidth
                                    val centerX = x + barItemWidth / 2
                                    val centerY = b.bottom - barHeight / 2

                                    val distance = kotlin.math.sqrt(
                                        (offset.x - centerX) * (offset.x - centerX) +
                                        (offset.y - centerY) * (offset.y - centerY)
                                    )

                                    if (distance < closestDistance && distance < touchThreshold) {
                                        closestDistance = distance
                                        closestEntry = entry
                                        closestGroupIndex = currentGroupIndex
                                        closestEntryIndex = entryIndex
                                        closestDataSetIndex = dataSetIndex
                                    }
                                    currentGroupIndex++
                                }
                            }

                            if (closestEntry != null) {
                                // 使用与绘制时完全相同的计算公式
                                val barX = b.left + closestGroupIndex * groupWidth +
                                        closestEntryIndex * barItemWidth * dataSetCount +
                                        closestDataSetIndex * barItemWidth
                                val barHeight = (closestEntry.y / dataYMax) * chartHeight

                                selectedBarInfo = Pair(barX, barItemWidth - barSpace)

                                val highlight = Highlight(
                                    x = barX + (barItemWidth - barSpace) / 2,
                                    y = b.bottom - barHeight,
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
        // 绘制Y轴
        chartBounds?.let { b ->
            YAxisView(
                config = yAxisConfig,
                bounds = b,
                yRangeMin = 0f,
                yRangeMax = data.getY最大值() * 1.1f,
                modifier = Modifier.fillMaxSize(),
                isLeft = true,
                limitLines = yAxisConfig.limitLines
            )

            // 绘制右侧Y轴（如果配置了）
            yAxisConfigRight?.let { rightConfig ->
                YAxisView(
                    config = rightConfig,
                    bounds = b,
                    yRangeMin = 0f,
                    yRangeMax = data.getY最大值() * 1.1f,
                    modifier = Modifier.fillMaxSize(),
                    isLeft = false,
                    limitLines = rightConfig.limitLines
                )
            }

            // 绘制X轴
            XAxisView(
                config = xAxisConfig,
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
                drawBarChartContentOptimized(
                    bounds = b,
                    style = style,
                    highlightStyle = highlightStyle,
                    gestureState = gestureState,
                    highlight = selectedHighlight,
                    selectedBarInfo = selectedBarInfo,
                    textMeasurer = textMeasurer,
                    cachedBars = cachedBars,
                    dataRange = dataRange
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

/**
 * 缓存的数据范围
 */
private data class BarChartDataRange(
    val xMin: Float,
    val xMax: Float,
    val yMin: Float,
    val yMax: Float
)

/**
 * 缓存的柱状图布局信息
 */
private data class BarLayout(
    val groupWidth: Float,
    val barItemWidth: Float,
    val barSpace: Float,
    val dataSetCount: Int
)

/**
 * 缓存的柱状图信息
 */
private data class CachedBarInfo(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val color: Color,
    val cornerRadius: Float,
    val value: Float?,
    val entry: BarEntry
)

/**
 * 优化后的绘制函数 - 使用缓存的坐标
 */
private fun DrawScope.drawBarChartContentOptimized(
    bounds: ChartBounds,
    style: BarChartStyle,
    highlightStyle: HighlightStyle,
    gestureState: ChartGestureState,
    highlight: Highlight?,
    selectedBarInfo: Pair<Float, Float>?,
    textMeasurer: TextMeasurer,
    cachedBars: List<CachedBarInfo>,
    dataRange: BarChartDataRange
) {
    if (cachedBars.isEmpty()) return

    // 应用手势变换
    translate(left = gestureState.translationX, top = gestureState.translationY) {
        scale(scaleX = gestureState.scaleX, scaleY = gestureState.scaleY, pivot = bounds.center) {

            // 绘制所有柱子
            for (bar in cachedBars) {
                drawRoundRect(
                    color = bar.color,
                    topLeft = Offset(bar.x, bar.y),
                    size = Size(bar.width, bar.height),
                    cornerRadius = CornerRadius(bar.cornerRadius, bar.cornerRadius)
                )

                // 绘制数值标签
                if (style.isDrawValues || style.isDrawValuesAboveBar) {
                    bar.value?.let { value ->
                        drawValueLabel(
                            textMeasurer = textMeasurer,
                            value = value,
                            x = bar.x + bar.width / 2,
                            y = bar.y - 10f,
                            textColor = style.valueTextColor,
                            textSize = style.valueTextSize
                        )
                    }
                }
            }

            // 绘制高亮
            highlight?.let { h ->
                // 绘制高亮背景（柱状图特有）
                if (highlightStyle.drawHighlightBackground) {
                    selectedBarInfo?.let { barInfo: Pair<Float, Float> ->
                        val (barX, barWidth) = barInfo
                        drawRoundRect(
                            color = highlightStyle.highlightBackgroundColor,
                            topLeft = Offset(barX, bounds.top),
                            size = Size(barWidth, bounds.height),
                            cornerRadius = CornerRadius(0f, 0f)
                        )
                    }
                }

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
