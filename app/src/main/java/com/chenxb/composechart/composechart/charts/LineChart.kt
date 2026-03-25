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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
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
import com.chenxb.composechart.composechart.data.LineChartData
import com.chenxb.composechart.composechart.data.LineDataSet
import com.chenxb.composechart.composechart.data.LineEntry
import com.chenxb.composechart.composechart.components.Highlight
import com.chenxb.composechart.composechart.components.HighlightStyle
import com.chenxb.composechart.composechart.components.LegendEntry
import com.chenxb.composechart.composechart.components.LegendForm
import com.chenxb.composechart.composechart.components.LegendStyle
import com.chenxb.composechart.composechart.components.LegendView
import com.chenxb.composechart.composechart.components.Marker
import com.chenxb.composechart.composechart.components.MarkerEntry
import com.chenxb.composechart.composechart.components.MarkerView
import com.chenxb.composechart.composechart.components.XAxisConfig
import com.chenxb.composechart.composechart.components.XAxisView
import com.chenxb.composechart.composechart.components.YAxisConfig
import com.chenxb.composechart.composechart.components.YAxisView
import com.chenxb.composechart.composechart.gestures.ChartGestureState
import com.chenxb.composechart.composechart.style.LineChartStyle
import com.chenxb.composechart.composechart.animation.ChartAnimation
import com.chenxb.composechart.composechart.animation.animateChartProgress
import com.chenxb.composechart.composechart.components.drawValueLabel
import com.chenxb.composechart.composechart.components.EmptyDataConfig
import com.chenxb.composechart.composechart.components.EmptyDataView
import com.chenxb.composechart.composechart.components.ChartDescription
import com.chenxb.composechart.composechart.components.DescriptionView

/**
 * 折线图
 */
@Composable
fun LineChart(
    data: LineChartData,
    modifier: Modifier = Modifier,
    style: LineChartStyle = LineChartStyle(),
    xAxisConfig: XAxisConfig = XAxisConfig(),
    yAxisConfig: YAxisConfig = YAxisConfig(),
    yAxisConfigRight: YAxisConfig? = null,
    legendStyle: LegendStyle = LegendStyle(),
    marker: Marker? = null,
    highlightStyle: HighlightStyle = HighlightStyle.Default,
    emptyDataConfig: EmptyDataConfig = EmptyDataConfig.Default,
    description: ChartDescription = ChartDescription.Default,
    touchHitSlop: Float = 50f,
    enableGestures: Boolean = false,
    animation: ChartAnimation = ChartAnimation.DEFAULT,
    onValueSelected: (LineEntry, Highlight) -> Unit = { _, _ -> },
    onValueDeselected: () -> Unit = {}
) {
    var chartSize by remember { mutableStateOf(IntSize.Zero) }
    var gestureState by remember { mutableStateOf(ChartGestureState()) }
    var selectedHighlight by remember { mutableStateOf<Highlight?>(null) }
    var markerEntry by remember { mutableStateOf<MarkerEntry?>(null) }
    var legendHeightPx by remember { mutableStateOf(0f) }

    val animationProgress = animateChartProgress(animation)
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
        LineDataRange(
            xMin = data.getX最小值(),
            xMax = data.getX最大值(),
            yMin = data.getY最小值(),
            yMax = data.getY最大值()
        )
    }

    // 缓存 Path 对象
    val linePath = remember { Path() }
    val fillPath = remember { Path() }

    // 缓存 Stroke 对象
    val solidStroke = remember { Stroke(cap = StrokeCap.Round) }

    // 使用 derivedStateOf 缓存点坐标计算
    val cachedDataSetPoints by remember(data, chartBounds, animationProgress) {
        derivedStateOf {
            if (chartBounds == null) {
                emptyList()
            } else {
                data.getDataSets().map { dataSet ->
                    calculatePoints(
                        entries = dataSet.getEntries(),
                        bounds = chartBounds,
                        dataRange = dataRange,
                        animationProgress = animationProgress
                    )
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(style.baseStyle.backgroundColor)
            .onSizeChanged { chartSize = it }
            .then(
                if (enableGestures) {
                    Modifier.pointerInput(data, chartBounds) {
                        detectTransformGestures { centroid, pan, zoom, _ ->
                            gestureState = gestureState.copy(
                                scaleX = (gestureState.scaleX * zoom).coerceIn(0.5f, 5f),
                                scaleY = (gestureState.scaleY * zoom).coerceIn(0.5f, 5f),
                                translationX = gestureState.translationX + pan.x,
                                translationY = gestureState.translationY + pan.y
                            )
                        }
                    }
                } else Modifier
            )
            .then(
                if (enableGestures) {
                    Modifier.pointerInput(data, chartBounds) {
                        detectTapGestures(
                            onTap = { offset ->
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
                                        markerEntry = marker?.let {
                                            MarkerEntry(
                                                entry = closestEntry,
                                                highlight = highlight,
                                                displayText = "X: ${closestEntry.x.toInt()}, Y: ${closestEntry.y.toInt()}"
                                            )
                                        }
                                        onValueSelected(closestEntry, highlight)
                                    } else {
                                        selectedHighlight = null
                                        markerEntry = null
                                        onValueDeselected()
                                    }
                                }
                            }
                        )
                    }
                } else Modifier
            )
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
                drawLineChartContentOptimized(
                    data = data,
                    bounds = b,
                    style = style,
                    highlightStyle = highlightStyle,
                    animationProgress = animationProgress,
                    gestureState = gestureState,
                    highlight = selectedHighlight,
                    textMeasurer = textMeasurer,
                    linePath = linePath,
                    fillPath = fillPath,
                    solidStroke = solidStroke,
                    cachedPoints = cachedDataSetPoints,
                    dataRange = dataRange
                )
            }
        }

        if (legendStyle.isEnabled) {
            val legendEntries = data.getDataSets().map { dataSet ->
                LegendEntry(
                    label = dataSet.label,
                    color = dataSet.color,
                    form = LegendForm.LINE,
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

        markerEntry?.let { entry ->
            selectedHighlight?.let { highlight ->
                Box(
                    modifier = Modifier.padding(start = highlight.x.dp - 30.dp, top = highlight.y.dp - 40.dp)
                ) {
                    MarkerView(markerEntry = entry)
                }
            }
        }

        // 描述文字
        DescriptionView(
            description = description,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * 缓存的数据范围
 */
private data class LineDataRange(
    val xMin: Float,
    val xMax: Float,
    val yMin: Float,
    val yMax: Float
)

/**
 * 计算数据点的屏幕坐标
 */
private fun calculatePoints(
    entries: List<LineEntry>,
    bounds: ChartBounds,
    dataRange: LineDataRange,
    animationProgress: Float
): List<Offset> {
    val chartWidth = bounds.width
    val chartHeight = bounds.height
    val xRange = if (dataRange.xMax != dataRange.xMin) dataRange.xMax - dataRange.xMin else 1f
    val yRange = if (dataRange.yMax != dataRange.yMin) dataRange.yMax - dataRange.yMin else 1f

    return entries.map { entry ->
        val pointX = bounds.left + ((entry.x - dataRange.xMin) / xRange) * chartWidth
        val pointY = bounds.bottom - ((entry.y - dataRange.yMin) / yRange) * chartHeight * animationProgress
        Offset(pointX, pointY)
    }
}

/**
 * 优化后的绘制函数 - 使用缓存的 Path 和预计算的点
 */
private fun DrawScope.drawLineChartContentOptimized(
    data: LineChartData,
    bounds: ChartBounds,
    style: LineChartStyle,
    highlightStyle: HighlightStyle,
    animationProgress: Float,
    gestureState: ChartGestureState,
    highlight: Highlight?,
    textMeasurer: TextMeasurer,
    linePath: Path,
    fillPath: Path,
    solidStroke: Stroke,
    cachedPoints: List<List<Offset>>,
    dataRange: LineDataRange
) {
    val chartWidth = bounds.width
    val chartHeight = bounds.height

    translate(left = gestureState.translationX, top = gestureState.translationY) {
        scale(scaleX = gestureState.scaleX, scaleY = gestureState.scaleY, pivot = bounds.center) {

            data.getDataSets().forEachIndexed { dataSetIndex, dataSet ->
                val entries = dataSet.getEntries()
                if (entries.isEmpty()) return@forEachIndexed

                val points = cachedPoints.getOrNull(dataSetIndex) ?: return@forEachIndexed

                // 重置 Path
                linePath.reset()
                fillPath.reset()

                when (dataSet.mode) {
                    LineDataSet.Mode.LINEAR -> {
                        buildLinearPath(points, linePath, fillPath, bounds.bottom)
                    }
                    LineDataSet.Mode.CUBIC_BEZIER -> {
                        buildCubicPath(points, linePath, fillPath, bounds.bottom)
                    }
                    LineDataSet.Mode.STEPPED -> {
                        buildSteppedPath(points, linePath, fillPath, bounds.bottom)
                    }
                    LineDataSet.Mode.HORIZONTAL_BEZIER -> {
                        buildHorizontalBezierPath(points, linePath, fillPath, bounds.bottom)
                    }
                }

                // 绘制填充
                if (style.isDrawFilled && points.isNotEmpty()) {
                    drawPath(
                        path = fillPath,
                        color = dataSet.fillColor.copy(alpha = dataSet.fillAlpha)
                    )
                }

                // 绘制线
                if (style.isDrawLine) {
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

                    drawPath(
                        path = linePath,
                        color = dataSet.lineColor,
                        style = stroke
                    )
                }

                // 绘制圆点和数值标签
                points.forEachIndexed { index, point ->
                    if (style.isDrawCircles) {
                        drawCircle(
                            color = dataSet.circleColor,
                            radius = dataSet.circleRadius * animationProgress,
                            center = point
                        )

                        if (dataSet.isDrawCircleHole) {
                            drawCircle(
                                color = dataSet.circleHoleColor,
                                radius = dataSet.circleHoleRadius * animationProgress,
                                center = point
                            )
                        }
                    }

                    if (style.isDrawValues) {
                        drawValueLabel(
                            textMeasurer = textMeasurer,
                            value = entries[index].y,
                            x = point.x,
                            y = point.y - dataSet.circleRadius - 10f,
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

/**
 * 构建线性路径
 */
private fun DrawScope.buildLinearPath(
    points: List<Offset>,
    linePath: Path,
    fillPath: Path,
    bottom: Float
) {
    if (points.isEmpty()) return

    for ((index, point) in points.withIndex()) {
        if (index == 0) {
            linePath.moveTo(point.x, point.y)
            fillPath.moveTo(point.x, bottom)
            fillPath.lineTo(point.x, point.y)
        } else {
            linePath.lineTo(point.x, point.y)
            fillPath.lineTo(point.x, point.y)
        }
    }

    if (points.isNotEmpty()) {
        fillPath.lineTo(points.last().x, bottom)
        fillPath.close()
    }
}

/**
 * 构建三次贝塞尔路径
 */
private fun DrawScope.buildCubicPath(
    points: List<Offset>,
    linePath: Path,
    fillPath: Path,
    bottom: Float
) {
    if (points.isEmpty()) return

    if (points.size == 1) {
        linePath.moveTo(points[0].x, points[0].y)
        fillPath.moveTo(points[0].x, bottom)
        fillPath.lineTo(points[0].x, points[0].y)
        fillPath.lineTo(points[0].x, bottom)
        fillPath.close()
        return
    }

    linePath.moveTo(points[0].x, points[0].y)
    fillPath.moveTo(points[0].x, bottom)
    fillPath.lineTo(points[0].x, points[0].y)

    val tension = 0.3f
    for (i in 1 until points.size) {
        val p0 = points.getOrElse(i - 2) { points[0] }
        val p1 = points[i - 1]
        val p2 = points[i]
        val p3 = points.getOrElse(i + 1) { points.last() }

        val cp1x = p1.x + (p2.x - p0.x) * tension / 3
        val cp1y = p1.y + (p2.y - p0.y) * tension / 3
        val cp2x = p2.x - (p3.x - p1.x) * tension / 3
        val cp2y = p2.y - (p3.y - p1.y) * tension / 3

        linePath.cubicTo(cp1x, cp1y, cp2x, cp2y, p2.x, p2.y)
        fillPath.cubicTo(cp1x, cp1y, cp2x, cp2y, p2.x, p2.y)
    }

    fillPath.lineTo(points.last().x, bottom)
    fillPath.close()
}

/**
 * 构建阶梯路径
 */
private fun DrawScope.buildSteppedPath(
    points: List<Offset>,
    linePath: Path,
    fillPath: Path,
    bottom: Float
) {
    if (points.isEmpty()) return

    for ((index, point) in points.withIndex()) {
        if (index == 0) {
            linePath.moveTo(point.x, point.y)
            fillPath.moveTo(point.x, bottom)
            fillPath.lineTo(point.x, point.y)
        } else {
            val prevPoint = points[index - 1]
            linePath.lineTo(point.x, prevPoint.y)
            linePath.lineTo(point.x, point.y)
            fillPath.lineTo(point.x, prevPoint.y)
            fillPath.lineTo(point.x, point.y)
        }
    }

    if (points.isNotEmpty()) {
        fillPath.lineTo(points.last().x, bottom)
        fillPath.close()
    }
}

/**
 * 构建水平贝塞尔路径
 */
private fun DrawScope.buildHorizontalBezierPath(
    points: List<Offset>,
    linePath: Path,
    fillPath: Path,
    bottom: Float
) {
    if (points.isEmpty()) return

    linePath.moveTo(points[0].x, points[0].y)
    fillPath.moveTo(points[0].x, bottom)
    fillPath.lineTo(points[0].x, points[0].y)

    for (i in 1 until points.size) {
        val prevPoint = points[i - 1]
        val currPoint = points[i]
        val midX = (prevPoint.x + currPoint.x) / 2

        linePath.cubicTo(
            midX, prevPoint.y,
            midX, currPoint.y,
            currPoint.x, currPoint.y
        )
        fillPath.cubicTo(
            midX, prevPoint.y,
            midX, currPoint.y,
            currPoint.x, currPoint.y
        )
    }

    fillPath.lineTo(points.last().x, bottom)
    fillPath.close()
}
