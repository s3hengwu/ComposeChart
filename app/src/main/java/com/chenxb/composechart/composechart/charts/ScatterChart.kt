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
import com.chenxb.composechart.composechart.data.ScatterChartData
import com.chenxb.composechart.composechart.data.ScatterDataSet
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
import com.chenxb.composechart.composechart.style.ScatterChartStyle
import com.chenxb.composechart.composechart.components.drawValueLabel
import com.chenxb.composechart.composechart.components.HighlightStyle
import com.chenxb.composechart.composechart.components.EmptyDataConfig
import com.chenxb.composechart.composechart.components.EmptyDataView

/**
 * 散点图
 */
@Composable
fun ScatterChart(
    data: ScatterChartData,
    modifier: Modifier = Modifier,
    style: ScatterChartStyle = ScatterChartStyle(),
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
        ScatterDataRange(
            xMin = data.getX最小值(),
            xMax = data.getX最大值(),
            yMin = data.getY最小值(),
            yMax = data.getY最大值()
        )
    }

    // 缓存 Path 对象（用于需要 Path 的形状）
    val reusablePath = remember { Path() }

    // 缓存 Stroke 对象
    val shapeStroke = remember { Stroke(width = 2f) }

    // 缓存点坐标计算
    val cachedPoints by remember(data, chartBounds, animationProgress) {
        derivedStateOf {
            if (chartBounds == null) {
                emptyList()
            } else {
                data.getDataSets().map { dataSet ->
                    val scatterDataSet = dataSet as? ScatterDataSet
                    dataSet.getEntries().map { entry ->
                        val x = chartBounds.left + ((entry.x - dataRange.xMin) / (dataRange.xMax - dataRange.xMin)) * chartBounds.width
                        val y = chartBounds.bottom - ((entry.y - dataRange.yMin) / (dataRange.yMax - dataRange.yMin)) * chartBounds.height * animationProgress
                        CachedScatterPoint(
                            x = x,
                            y = y,
                            entry = entry,
                            shape = scatterDataSet?.scatterShape ?: ScatterDataSet.ScatterShape.CIRCLE,
                            size = scatterDataSet?.scatterShapeSize ?: style.scatterShapeSize,
                            color = dataSet.color,
                            hasHole = scatterDataSet?.isDrawShapeHoleEnabled == true,
                            holeRadius = scatterDataSet?.shapeHoleRadius ?: (scatterDataSet?.scatterShapeSize ?: style.scatterShapeSize) * 0.5f,
                            holeColor = scatterDataSet?.shapeHoleColor ?: Color.White
                        )
                    }
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(style.baseStyle.backgroundColor)
            .onSizeChanged { chartSize = it }
            .pointerInput(data, chartBounds) {
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
            .pointerInput(data, chartBounds) {
                detectTapGestures(
                    onTap = { offset ->
                        if (!enableGestures) return@detectTapGestures
                        chartBounds?.let { b ->
                            val xRange = dataRange.xMax - dataRange.xMin
                            val yRange = dataRange.yMax - dataRange.yMin
                            val chartWidth = b.width
                            val chartHeight = b.height

                            var closestEntry: LineEntry? = null
                            var closestDistance = Float.MAX_VALUE

                            for (dataSet in data.getDataSets()) {
                                for (entry in dataSet.getEntries()) {
                                    val pointX = b.left + ((entry.x - dataRange.xMin) / xRange) * chartWidth
                                    val pointY = b.bottom - ((entry.y - dataRange.yMin) / yRange) * chartHeight

                                    val distance = kotlin.math.sqrt(
                                        (offset.x - pointX) * (offset.x - pointX) +
                                        (offset.y - pointY) * (offset.y - pointY)
                                    )

                                    if (distance < closestDistance && distance < touchHitSlop) {
                                        closestDistance = distance
                                        closestEntry = entry
                                    }
                                }
                            }

                            if (closestEntry != null) {
                                val pointX = b.left + ((closestEntry.x - dataRange.xMin) / xRange) * chartWidth
                                val pointY = b.bottom - ((closestEntry.y - dataRange.yMin) / yRange) * chartHeight

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
            YAxisView(
                config = yAxisConfig,
                bounds = b,
                yRangeMin = dataRange.yMin,
                yRangeMax = dataRange.yMax,
                modifier = Modifier.fillMaxSize(),
                isLeft = true,
                limitLines = yAxisConfig.limitLines
            )

            yAxisConfigRight?.let { rightConfig ->
                YAxisView(
                    config = rightConfig,
                    bounds = b,
                    yRangeMin = dataRange.yMin,
                    yRangeMax = dataRange.yMax,
                    modifier = Modifier.fillMaxSize(),
                    isLeft = false,
                    limitLines = rightConfig.limitLines
                )
            }

            XAxisView(
                config = xAxisConfig,
                bounds = b,
                xRangeMin = dataRange.xMin,
                xRangeMax = dataRange.xMax,
                modifier = Modifier.fillMaxSize()
            )
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            chartBounds?.let { b ->
                drawScatterChartContentOptimized(
                    bounds = b,
                    style = style,
                    highlightStyle = highlightStyle,
                    animationProgress = animationProgress,
                    gestureState = gestureState,
                    highlight = selectedHighlight,
                    textMeasurer = textMeasurer,
                    reusablePath = reusablePath,
                    shapeStroke = shapeStroke,
                    cachedPoints = cachedPoints
                )
            }
        }

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
private data class ScatterDataRange(
    val xMin: Float,
    val xMax: Float,
    val yMin: Float,
    val yMax: Float
)

/**
 * 缓存的散点数据
 */
private data class CachedScatterPoint(
    val x: Float,
    val y: Float,
    val entry: LineEntry,
    val shape: ScatterDataSet.ScatterShape,
    val size: Float,
    val color: Color,
    val hasHole: Boolean,
    val holeRadius: Float,
    val holeColor: Color
)

/**
 * 优化后的绘制函数
 */
private fun DrawScope.drawScatterChartContentOptimized(
    bounds: ChartBounds,
    style: ScatterChartStyle,
    highlightStyle: HighlightStyle,
    animationProgress: Float,
    gestureState: ChartGestureState,
    highlight: Highlight?,
    textMeasurer: TextMeasurer,
    reusablePath: Path,
    shapeStroke: Stroke,
    cachedPoints: List<List<CachedScatterPoint>>
) {
    translate(left = gestureState.translationX, top = gestureState.translationY) {
        scale(scaleX = gestureState.scaleX, scaleY = gestureState.scaleY, pivot = bounds.center) {

            for (dataSetPoints in cachedPoints) {
                for (point in dataSetPoints) {
                    val animatedSize = point.size * animationProgress

                    when (point.shape) {
                        ScatterDataSet.ScatterShape.CIRCLE -> {
                            drawCircle(
                                color = point.color,
                                radius = animatedSize,
                                center = Offset(point.x, point.y)
                            )
                        }
                        ScatterDataSet.ScatterShape.SQUARE -> {
                            drawRect(
                                color = point.color,
                                topLeft = Offset(point.x - animatedSize, point.y - animatedSize),
                                size = Size(animatedSize * 2, animatedSize * 2)
                            )
                        }
                        ScatterDataSet.ScatterShape.TRIANGLE -> {
                            reusablePath.reset()
                            reusablePath.moveTo(point.x, point.y - animatedSize)
                            reusablePath.lineTo(point.x - animatedSize, point.y + animatedSize)
                            reusablePath.lineTo(point.x + animatedSize, point.y + animatedSize)
                            reusablePath.close()
                            drawPath(path = reusablePath, color = point.color)
                        }
                        ScatterDataSet.ScatterShape.CROSS -> {
                            drawLine(
                                color = point.color,
                                start = Offset(point.x - animatedSize, point.y),
                                end = Offset(point.x + animatedSize, point.y),
                                strokeWidth = 2f
                            )
                            drawLine(
                                color = point.color,
                                start = Offset(point.x, point.y - animatedSize),
                                end = Offset(point.x, point.y + animatedSize),
                                strokeWidth = 2f
                            )
                        }
                        ScatterDataSet.ScatterShape.X -> {
                            drawLine(
                                color = point.color,
                                start = Offset(point.x - animatedSize, point.y - animatedSize),
                                end = Offset(point.x + animatedSize, point.y + animatedSize),
                                strokeWidth = 2f
                            )
                            drawLine(
                                color = point.color,
                                start = Offset(point.x - animatedSize, point.y + animatedSize),
                                end = Offset(point.x + animatedSize, point.y - animatedSize),
                                strokeWidth = 2f
                            )
                        }
                        ScatterDataSet.ScatterShape.CHEVRON_UP -> {
                            reusablePath.reset()
                            reusablePath.moveTo(point.x - animatedSize, point.y + animatedSize)
                            reusablePath.lineTo(point.x, point.y - animatedSize)
                            reusablePath.lineTo(point.x + animatedSize, point.y + animatedSize)
                            drawPath(path = reusablePath, color = point.color, style = shapeStroke)
                        }
                        ScatterDataSet.ScatterShape.CHEVRON_DOWN -> {
                            reusablePath.reset()
                            reusablePath.moveTo(point.x - animatedSize, point.y - animatedSize)
                            reusablePath.lineTo(point.x, point.y + animatedSize)
                            reusablePath.lineTo(point.x + animatedSize, point.y - animatedSize)
                            drawPath(path = reusablePath, color = point.color, style = shapeStroke)
                        }
                        null -> {
                            drawCircle(
                                color = point.color,
                                radius = animatedSize,
                                center = Offset(point.x, point.y)
                            )
                        }
                    }

                    // 绘制形状空洞
                    if (point.hasHole) {
                        drawCircle(
                            color = point.holeColor,
                            radius = point.holeRadius * animationProgress,
                            center = Offset(point.x, point.y)
                        )
                    }

                    // 绘制数值标签
                    if (style.isDrawValues) {
                        drawValueLabel(
                            textMeasurer = textMeasurer,
                            value = point.entry.y,
                            x = point.x,
                            y = point.y - point.size - 8f,
                            textColor = style.valueTextColor,
                            textSize = style.valueTextSize
                        )
                    }
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
