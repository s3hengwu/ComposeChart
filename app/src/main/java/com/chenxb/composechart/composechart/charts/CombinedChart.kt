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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.chenxb.composechart.composechart.ChartBounds
import com.chenxb.composechart.composechart.data.CombinedChartData
import com.chenxb.composechart.composechart.data.LineDataSet
import com.chenxb.composechart.composechart.data.BarDataSet
import com.chenxb.composechart.composechart.data.ScatterDataSet
import com.chenxb.composechart.composechart.data.CandleDataSet
import com.chenxb.composechart.composechart.data.BubbleDataSet
import com.chenxb.composechart.composechart.data.LineEntry
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
import com.chenxb.composechart.composechart.style.LineChartStyle
import com.chenxb.composechart.composechart.style.BarChartStyle
import com.chenxb.composechart.composechart.style.ScatterChartStyle
import com.chenxb.composechart.composechart.style.CandleChartStyle
import com.chenxb.composechart.composechart.style.BubbleChartStyle
import com.chenxb.composechart.composechart.components.HighlightStyle
import com.chenxb.composechart.composechart.components.EmptyDataConfig
import com.chenxb.composechart.composechart.components.EmptyDataView
import kotlin.math.ln
import kotlin.math.min

/**
 * 组合图
 * 支持组合多种图表类型（如折线图+柱状图）
 */
@Composable
fun CombinedChart(
    data: CombinedChartData,
    modifier: Modifier = Modifier,
    lineStyle: LineChartStyle = LineChartStyle(),
    barStyle: BarChartStyle = BarChartStyle(),
    scatterStyle: ScatterChartStyle = ScatterChartStyle(),
    candleStyle: CandleChartStyle = CandleChartStyle(),
    bubbleStyle: BubbleChartStyle = BubbleChartStyle(),
    xAxisConfig: XAxisConfig = XAxisConfig(),
    yAxisConfig: YAxisConfig = YAxisConfig(),
    yAxisConfigRight: YAxisConfig? = null,
    legendStyle: LegendStyle = LegendStyle(),
    touchHitSlop: Float = 50f,
    enableGestures: Boolean = false,
    highlightStyle: HighlightStyle = HighlightStyle.Default,
    emptyDataConfig: EmptyDataConfig = EmptyDataConfig.Default,
    animationProgress: Float = 1f,
    onValueSelected: (LineEntry, Highlight) -> Unit = { _, _ -> },
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
    val isEmpty = data.getEntryCount() == 0
    if (isEmpty && emptyDataConfig.enabled) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(lineStyle.baseStyle.backgroundColor)
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
        CombinedDataRange(
            xMin = data.getX最小值(),
            xMax = data.getX最大值(),
            yMin = data.getY最小值(),
            yMax = data.getY最大值()
        )
    }

    // 缓存 Path 对象
    val linePath = remember { Path() }
    val fillPath = remember { Path() }
    val scatterPath = remember { Path() }

    // 缓存 Stroke 对象
    val solidStroke = remember { Stroke(cap = StrokeCap.Round) }

    // 缓存坐标转换函数
    val coordinateConverter by remember(data, chartBounds) {
        derivedStateOf {
            if (chartBounds == null) {
                null
            } else {
                val xRange = dataRange.xMax - dataRange.xMin
                val yRange = if (dataRange.yMax != dataRange.yMin) dataRange.yMax - dataRange.yMin else 1f
                val xStep = if (xRange > 0) chartBounds.width / (xRange + 1) else chartBounds.width / 2

                CoordinateConverter(
                    xMin = dataRange.xMin,
                    yMin = dataRange.yMin,
                    xStep = xStep,
                    yRange = yRange,
                    boundsLeft = chartBounds.left,
                    boundsBottom = chartBounds.bottom,
                    chartHeight = chartBounds.height
                )
            }
        }
    }

    // 缓存所有图表数据点的坐标
    val cachedChartData by remember(data, chartBounds, animationProgress, coordinateConverter) {
        derivedStateOf {
            if (chartBounds == null || coordinateConverter == null) {
                null
            } else {
                CachedChartData(
                    barPoints = cacheBarData(data.barData, coordinateConverter, dataRange.yMax, chartBounds.height, animationProgress),
                    linePoints = cacheLineData(data.lineData, coordinateConverter, dataRange, chartBounds.height, animationProgress),
                    scatterPoints = cacheScatterData(data.scatterData, coordinateConverter, dataRange, chartBounds.height, animationProgress),
                    candlePoints = cacheCandleData(data.candleData, coordinateConverter, dataRange, chartBounds.height, animationProgress),
                    bubblePoints = cacheBubbleData(data.bubbleData, bubbleStyle, coordinateConverter, dataRange, chartBounds.height, animationProgress)
                )
            }
        }
    }

    LaunchedEffect(animationProgress) {
        // 动画效果处理
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(lineStyle.baseStyle.backgroundColor)
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
                            var closestEntry: LineEntry? = null
                            var closestDistance = Float.MAX_VALUE
                            val touchThreshold = touchHitSlop

                            // 检查折线数据
                            data.lineData?.let { lineData ->
                                for (dataSet in lineData.getDataSets()) {
                                    val entries = dataSet.getEntries()
                                    for (entry in entries) {
                                        val pointX = b.left + (entry.x - dataXMin + 0.5f) / (xRange + 1) * chartWidth
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
                            }

                            // 检查柱状数据
                            data.barData?.let { barData ->
                                val groupSpace = barStyle.groupSpace
                                val barSpace = barStyle.barSpace
                                val dataSetCount = barData.getDataSetCount()
                                val groupWidth = chartWidth / (xRange + 1)
                                val barItemWidth = groupWidth * (1 - groupSpace - barSpace * (dataSetCount - 1)) / dataSetCount

                                var currentGroupIndex = 0
                                for ((dataSetIndex, dataSet) in barData.getDataSets().withIndex()) {
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
                                            closestEntry = LineEntry(entry.x, entry.y)
                                        }
                                        currentGroupIndex++
                                    }
                                }
                            }

                            // 检查散点数据
                            data.scatterData?.let { scatterData ->
                                for (dataSet in scatterData.getDataSets()) {
                                    val entries = dataSet.getEntries()
                                    for (entry in entries) {
                                        val pointX = b.left + (entry.x - dataXMin + 0.5f) / (xRange + 1) * chartWidth
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
                            }

                            // 检查K线数据
                            data.candleData?.let { candleData ->
                                for (dataSet in candleData.getDataSets()) {
                                    val entries = dataSet.getEntries()
                                    for (entry in entries) {
                                        val pointX = b.left + (entry.x - dataXMin + 0.5f) / (xRange + 1) * chartWidth
                                        val midY = b.bottom - ((entry.high + entry.low) / 2 - dataYMin) / yRange * chartHeight

                                        val distance = kotlin.math.sqrt(
                                            (offset.x - pointX) * (offset.x - pointX) +
                                            (offset.y - midY) * (offset.y - midY)
                                        )

                                        if (distance < closestDistance && distance < touchThreshold) {
                                            closestDistance = distance
                                            closestEntry = LineEntry(entry.x, (entry.high + entry.low) / 2)
                                        }
                                    }
                                }
                            }

                            // 检查气泡数据
                            data.bubbleData?.let { bubbleData ->
                                for (dataSet in bubbleData.getDataSets()) {
                                    val entries = dataSet.getEntries()
                                    for (entry in entries) {
                                        val pointX = b.left + (entry.x - dataXMin + 0.5f) / (xRange + 1) * chartWidth
                                        val pointY = b.bottom - ((entry.y - dataYMin) / yRange) * chartHeight

                                        val distance = kotlin.math.sqrt(
                                            (offset.x - pointX) * (offset.x - pointX) +
                                            (offset.y - pointY) * (offset.y - pointY)
                                        )

                                        if (distance < closestDistance && distance < touchThreshold) {
                                            closestDistance = distance
                                            closestEntry = LineEntry(entry.x, entry.y)
                                        }
                                    }
                                }
                            }

                            if (closestEntry != null) {
                                val pointX = b.left + (closestEntry.x - dataXMin + 0.5f) / (xRange + 1) * chartWidth
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
                config = xAxisConfig,
                bounds = b,
                xRangeMin = data.getX最小值(),
                xRangeMax = data.getX最大值(),
                modifier = Modifier.fillMaxSize()
            )

            // 绘制图表内容
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                drawCombinedChartContentOptimized(
                    data = data,
                    bounds = b,
                    lineStyle = lineStyle,
                    barStyle = barStyle,
                    scatterStyle = scatterStyle,
                    candleStyle = candleStyle,
                    bubbleStyle = bubbleStyle,
                    highlightStyle = highlightStyle,
                    gestureState = gestureState,
                    highlight = selectedHighlight,
                    linePath = linePath,
                    fillPath = fillPath,
                    scatterPath = scatterPath,
                    solidStroke = solidStroke,
                    textMeasurer = textMeasurer,
                    cachedData = cachedChartData,
                    dataRange = dataRange
                )
            }
        }

        // 绘制图例 - 使用 Alignment.BottomStart 钉在底部
        if (legendStyle.isEnabled) {
            val legendEntries = mutableListOf<LegendEntry>()
            data.lineData?.getDataSets()?.forEach { dataSet ->
                legendEntries.add(LegendEntry(
                    label = dataSet.label,
                    color = dataSet.lineColor,
                    form = LegendForm.LINE,
                    formSize = 12f
                ))
            }
            data.barData?.getDataSets()?.forEach { dataSet ->
                legendEntries.add(LegendEntry(
                    label = dataSet.label,
                    color = dataSet.color,
                    form = LegendForm.SQUARE,
                    formSize = 12f
                ))
            }
            data.scatterData?.getDataSets()?.forEach { dataSet ->
                legendEntries.add(LegendEntry(
                    label = dataSet.label,
                    color = dataSet.color,
                    form = LegendForm.SQUARE,
                    formSize = 12f
                ))
            }
            data.candleData?.getDataSets()?.forEach { dataSet ->
                legendEntries.add(LegendEntry(
                    label = dataSet.label,
                    color = dataSet.color,
                    form = LegendForm.SQUARE,
                    formSize = 12f
                ))
            }
            data.bubbleData?.getDataSets()?.forEach { dataSet ->
                legendEntries.add(LegendEntry(
                    label = dataSet.label,
                    color = dataSet.color,
                    form = LegendForm.CIRCLE,
                    formSize = 12f
                ))
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
private data class CombinedDataRange(
    val xMin: Float,
    val xMax: Float,
    val yMin: Float,
    val yMax: Float
)

/**
 * 坐标转换器
 */
private data class CoordinateConverter(
    val xMin: Float,
    val yMin: Float,
    val xStep: Float,
    val yRange: Float,
    val boundsLeft: Float,
    val boundsBottom: Float,
    val chartHeight: Float
) {
    fun toScreenX(dataX: Float): Float = boundsLeft + (dataX - xMin + 0.5f) * xStep
    fun toScreenY(dataY: Float, animationProgress: Float = 1f): Float =
        boundsBottom - ((dataY - yMin) / yRange) * chartHeight * animationProgress
}

/**
 * 缓存的柱状图点
 */
private data class CachedBarPoint(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val color: Color
)

/**
 * 缓存的折线图点
 */
private data class CachedLinePoint(
    val x: Float,
    val y: Float,
    val color: Color,
    val circleRadius: Float,
    val circleColor: Color
)

/**
 * 缓存的散点图点
 */
private data class CombinedCachedScatterPoint(
    val x: Float,
    val y: Float,
    val color: Color,
    val shape: ScatterDataSet.ScatterShape?,
    val size: Float
)

/**
 * 缓存的K线图点
 */
private data class CachedCandlePoint(
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
    val animationProgress: Float
)

/**
 * 缓存的气泡图点
 */
private data class CachedBubblePoint(
    val x: Float,
    val y: Float,
    val radius: Float,
    val color: Color
)

/**
 * 缓存的所有图表数据
 */
private data class CachedChartData(
    val barPoints: List<CachedBarPoint>,
    val linePoints: List<List<CachedLinePoint>>,
    val scatterPoints: List<CombinedCachedScatterPoint>,
    val candlePoints: List<CachedCandlePoint>,
    val bubblePoints: List<CachedBubblePoint>
)

// ========== 缓存函数 ==========

private fun cacheBarData(
    barData: com.chenxb.composechart.composechart.data.BarChartData?,
    converter: CoordinateConverter?,
    yMax: Float,
    chartHeight: Float,
    animationProgress: Float
): List<CachedBarPoint> {
    if (barData == null || converter == null) return emptyList()

    val result = mutableListOf<CachedBarPoint>()
    for (dataSet in barData.getDataSets()) {
        val entries = dataSet.getEntries()
        if (entries.isEmpty()) continue

        val barWidth = chartHeight * 0.35f / entries.size.coerceAtLeast(1)

        for (entry in entries) {
            val x = converter.toScreenX(entry.x)
            val barHeight = (entry.y / yMax) * chartHeight * animationProgress
            result.add(
                CachedBarPoint(
                    x = x - barWidth / 2,
                    y = converter.boundsBottom - barHeight,
                    width = barWidth,
                    height = barHeight,
                    color = dataSet.color
                )
            )
        }
    }
    return result
}

private fun cacheLineData(
    lineData: com.chenxb.composechart.composechart.data.LineChartData?,
    converter: CoordinateConverter?,
    dataRange: CombinedDataRange,
    chartHeight: Float,
    animationProgress: Float
): List<List<CachedLinePoint>> {
    if (lineData == null || converter == null) return emptyList()

    return lineData.getDataSets().map { dataSet ->
        dataSet.getEntries().map { entry ->
            CachedLinePoint(
                x = converter.toScreenX(entry.x),
                y = converter.toScreenY(entry.y, animationProgress),
                color = dataSet.lineColor,
                circleRadius = dataSet.circleRadius,
                circleColor = dataSet.circleColor
            )
        }
    }
}

private fun cacheScatterData(
    scatterData: com.chenxb.composechart.composechart.data.ScatterChartData?,
    converter: CoordinateConverter?,
    dataRange: CombinedDataRange,
    chartHeight: Float,
    animationProgress: Float
): List<CombinedCachedScatterPoint> {
    if (scatterData == null || converter == null) return emptyList()

    val result = mutableListOf<CombinedCachedScatterPoint>()
    for (dataSet in scatterData.getDataSets()) {
        val scatterDataSet = dataSet as? ScatterDataSet
        val scatterSize = scatterDataSet?.scatterShapeSize ?: 8f
        val shape = scatterDataSet?.scatterShape

        for (entry in dataSet.getEntries()) {
            result.add(
                CombinedCachedScatterPoint(
                    x = converter.toScreenX(entry.x),
                    y = converter.toScreenY(entry.y, animationProgress),
                    color = dataSet.color,
                    shape = shape,
                    size = scatterSize * animationProgress
                )
            )
        }
    }
    return result
}

private fun cacheCandleData(
    candleData: com.chenxb.composechart.composechart.data.CandleChartData?,
    converter: CoordinateConverter?,
    dataRange: CombinedDataRange,
    chartHeight: Float,
    animationProgress: Float
): List<CachedCandlePoint> {
    if (candleData == null || converter == null) return emptyList()

    val result = mutableListOf<CachedCandlePoint>()
    for (dataSet in candleData.getDataSets()) {
        val entries = dataSet.getEntries()
        if (entries.isEmpty()) continue

        val candleWidth = chartHeight * 0.7f / entries.size.coerceAtLeast(1)

        for (entry in entries) {
            val isIncreasing = entry.close >= entry.open
            result.add(
                CachedCandlePoint(
                    x = converter.toScreenX(entry.x),
                    highY = converter.toScreenY(entry.high),
                    lowY = converter.toScreenY(entry.low),
                    openY = converter.toScreenY(entry.open),
                    closeY = converter.toScreenY(entry.close),
                    candleWidth = candleWidth,
                    isIncreasing = isIncreasing,
                    candleColor = if (isIncreasing) Color(0xFF4CAF50) else Color(0xFFF44336),
                    shadowColor = if (dataSet.shadowColorSameAsCandle) {
                        if (isIncreasing) Color(0xFF4CAF50) else Color(0xFFF44336)
                    } else dataSet.shadowColor,
                    shadowWidth = dataSet.shadowWidth,
                    paintStyle = if (isIncreasing) dataSet.increasingPaintStyle else dataSet.decreasingPaintStyle,
                    animationProgress = animationProgress
                )
            )
        }
    }
    return result
}

private fun cacheBubbleData(
    bubbleData: com.chenxb.composechart.composechart.data.BubbleChartData?,
    bubbleStyle: BubbleChartStyle,
    converter: CoordinateConverter?,
    dataRange: CombinedDataRange,
    chartHeight: Float,
    animationProgress: Float
): List<CachedBubblePoint> {
    if (bubbleData == null || converter == null) return emptyList()

    val result = mutableListOf<CachedBubblePoint>()
    val maxSize = bubbleStyle.maxSize

    for (dataSet in bubbleData.getDataSets()) {
        for (entry in dataSet.getEntries()) {
            val normalizedSize = if (bubbleStyle.normalizeSize && maxSize > 0) {
                ln(entry.size + 1) / ln(maxSize + 1) * maxSize
            } else {
                entry.size.coerceIn(5f, maxSize)
            }
            result.add(
                CachedBubblePoint(
                    x = converter.toScreenX(entry.x),
                    y = converter.toScreenY(entry.y, animationProgress),
                    radius = normalizedSize * animationProgress,
                    color = dataSet.color
                )
            )
        }
    }
    return result
}

// ========== 优化后的绘制函数 ==========

private fun DrawScope.drawCombinedChartContentOptimized(
    data: CombinedChartData,
    bounds: ChartBounds,
    lineStyle: LineChartStyle,
    barStyle: BarChartStyle,
    scatterStyle: ScatterChartStyle,
    candleStyle: CandleChartStyle,
    bubbleStyle: BubbleChartStyle,
    highlightStyle: HighlightStyle,
    gestureState: ChartGestureState,
    highlight: Highlight?,
    linePath: Path,
    fillPath: Path,
    scatterPath: Path,
    solidStroke: Stroke,
    textMeasurer: TextMeasurer,
    cachedData: CachedChartData?,
    dataRange: CombinedDataRange
) {
    if (cachedData == null) return

    // 应用手势变换
    translate(left = gestureState.translationX, top = gestureState.translationY) {
        scale(scaleX = gestureState.scaleX, scaleY = gestureState.scaleY, pivot = bounds.center) {

            // 1. 绘制柱状图（底层）
            for (bar in cachedData.barPoints) {
                drawRoundRect(
                    color = bar.color,
                    topLeft = Offset(bar.x, bar.y),
                    size = Size(bar.width, bar.height),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                )
            }

            // 2. 绘制折线图
            data.lineData?.getDataSets()?.forEachIndexed { dataSetIndex, dataSet ->
                val points = cachedData.linePoints.getOrNull(dataSetIndex) ?: return@forEachIndexed
                if (points.isEmpty()) return@forEachIndexed

                linePath.reset()
                fillPath.reset()

                points.forEachIndexed { index, point ->
                    if (index == 0) {
                        linePath.moveTo(point.x, point.y)
                        fillPath.moveTo(point.x, point.y)
                    } else {
                        linePath.lineTo(point.x, point.y)
                        fillPath.lineTo(point.x, point.y)
                    }
                }

                // 绘制填充
                if (lineStyle.isDrawFilled && points.isNotEmpty()) {
                    fillPath.lineTo(points.last().x, bounds.bottom)
                    fillPath.lineTo(points.first().x, bounds.bottom)
                    fillPath.close()
                    drawPath(
                        path = fillPath,
                        color = dataSet.fillColor.copy(alpha = dataSet.fillAlpha)
                    )
                }

                // 绘制线
                if (lineStyle.isDrawLine) {
                    val stroke = if (dataSet.isDrawDashedLineEnabled) {
                        Stroke(
                            width = dataSet.lineWidth,
                            pathEffect = PathEffect.dashPathEffect(
                                floatArrayOf(dataSet.dashLength, dataSet.dashSpaceLength)
                            ),
                            cap = StrokeCap.Round
                        )
                    } else {
                        Stroke(width = dataSet.lineWidth, cap = StrokeCap.Round)
                    }
                    drawPath(path = linePath, color = dataSet.lineColor, style = stroke)
                }

                // 绘制圆点
                if (lineStyle.isDrawCircles) {
                    for (point in points) {
                        drawCircle(
                            color = point.circleColor,
                            radius = point.circleRadius,
                            center = Offset(point.x, point.y)
                        )
                    }
                }
            }

            // 3. 绘制散点图
            for (point in cachedData.scatterPoints) {
                when (point.shape) {
                    ScatterDataSet.ScatterShape.CIRCLE -> {
                        drawCircle(
                            color = point.color,
                            radius = point.size,
                            center = Offset(point.x, point.y)
                        )
                    }
                    ScatterDataSet.ScatterShape.SQUARE -> {
                        drawRect(
                            color = point.color,
                            topLeft = Offset(point.x - point.size, point.y - point.size),
                            size = Size(point.size * 2, point.size * 2)
                        )
                    }
                    ScatterDataSet.ScatterShape.TRIANGLE -> {
                        scatterPath.reset()
                        scatterPath.moveTo(point.x, point.y - point.size)
                        scatterPath.lineTo(point.x - point.size, point.y + point.size)
                        scatterPath.lineTo(point.x + point.size, point.y + point.size)
                        scatterPath.close()
                        drawPath(path = scatterPath, color = point.color)
                    }
                    else -> {
                        drawCircle(
                            color = point.color,
                            radius = point.size,
                            center = Offset(point.x, point.y)
                        )
                    }
                }
            }

            // 4. 绘制K线图
            for (point in cachedData.candlePoints) {
                // 绘制上下影线
                drawLine(
                    color = point.shadowColor,
                    start = Offset(point.x, point.highY),
                    end = Offset(point.x, point.lowY),
                    strokeWidth = point.shadowWidth
                )

                // 绘制蜡烛体
                val bodyTop = min(point.openY, point.closeY)
                val bodyHeight = kotlin.math.abs(point.openY - point.closeY).coerceAtLeast(1f)

                if (point.paintStyle == CandleDataSet.PaintStyle.FILL) {
                    drawRect(
                        color = point.candleColor,
                        topLeft = Offset(point.x - point.candleWidth / 2, bodyTop),
                        size = Size(point.candleWidth, bodyHeight * point.animationProgress)
                    )
                } else {
                    drawRect(
                        color = point.candleColor,
                        topLeft = Offset(point.x - point.candleWidth / 2, bodyTop),
                        size = Size(point.candleWidth, bodyHeight * point.animationProgress),
                        style = Stroke(width = point.shadowWidth)
                    )
                }
            }

            // 5. 绘制气泡图
            for (point in cachedData.bubblePoints) {
                drawCircle(
                    color = point.color,
                    radius = point.radius,
                    center = Offset(point.x, point.y)
                )
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
