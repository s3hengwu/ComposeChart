package com.chenxb.composechart.composechart.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import com.chenxb.composechart.composechart.ChartBounds
import com.chenxb.composechart.composechart.data.RadarChartData
import com.chenxb.composechart.composechart.data.RadarDataSet
import com.chenxb.composechart.composechart.data.RadarEntry
import com.chenxb.composechart.composechart.components.Highlight
import com.chenxb.composechart.composechart.components.LegendEntry
import com.chenxb.composechart.composechart.components.LegendForm
import com.chenxb.composechart.composechart.components.LegendStyle
import com.chenxb.composechart.composechart.components.LegendView
import com.chenxb.composechart.composechart.gestures.ChartGestureState
import com.chenxb.composechart.composechart.style.RadarChartStyle
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * 雷达图
 */
@Composable
fun RadarChart(
    data: RadarChartData,
    modifier: Modifier = Modifier,
    style: RadarChartStyle = RadarChartStyle(),
    legendStyle: LegendStyle = LegendStyle(),
    enableGestures: Boolean = false,
    animationProgress: Float = 1f,
    onValueSelected: (RadarEntry, Highlight) -> Unit = { _, _ -> },
    onValueDeselected: () -> Unit = {}
) {
    var chartSize by remember { mutableStateOf(IntSize.Zero) }
    var selectedIndex by remember { mutableStateOf(-1) }
    // Legend 真实高度（px），由 onSizeChanged 测量
    var legendHeightPx by remember { mutableStateOf(0f) }
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    // RadarChart 的 bounds 需要为 Legend 留出底部空间
    val bounds = remember(chartSize.width, chartSize.height, legendStyle.isEnabled, legendHeightPx) {
        if (chartSize.width > 0 && chartSize.height > 0) {
            ChartBounds(
                left = 0f,
                top = 0f,
                right = chartSize.width.toFloat(),
                bottom = if (legendStyle.isEnabled) chartSize.height - legendHeightPx else chartSize.height.toFloat()
            )
        } else null
    }

    val centerX = bounds?.let { (it.left + it.right) / 2 } ?: 0f
    val centerY = bounds?.let { (it.top + it.bottom) / 2 } ?: 0f

    // ========== 性能优化：缓存计算结果 ==========

    // 缓存 Path 对象
    val webPath = remember { Path() }
    val dataPath = remember { Path() }

    // 缓存 Stroke 对象
    val webStroke = remember { Stroke(width = style.webLineWidth) }
    val dataStroke = remember { Stroke(width = 2f) }

    // 缓存雷达图坐标计算
    val radarLayout by remember(data, bounds, animationProgress) {
        derivedStateOf {
            if (bounds == null) {
                null
            } else {
                val radius = min(bounds.width, bounds.height) / 2 * 0.7f
                val dataSet = data.getDataSets().firstOrNull()
                val entries = dataSet?.getEntries() ?: emptyList()
                val entryCount = entries.size
                val sliceAngle = 360f / entryCount.coerceAtLeast(1)

                val yMin = data.getY最小值()
                val yMax = data.getY最大值()
                val yRange = if (yMax != yMin) yMax - yMin else 1f

                RadarLayout(
                    centerX = (bounds.left + bounds.right) / 2,
                    centerY = (bounds.top + bounds.bottom) / 2,
                    radius = radius,
                    entryCount = entryCount,
                    sliceAngle = sliceAngle,
                    yMin = yMin,
                    yRange = yRange,
                    entries = entries
                )
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(style.baseStyle.backgroundColor)
            .onSizeChanged { chartSize = it }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    if (!enableGestures) return@detectTapGestures
                    bounds?.let { b ->
                        val centerX = (b.left + b.right) / 2
                        val centerY = (b.top + b.bottom) / 2
                        val radius = min(b.width, b.height) / 2 * 0.7f

                        val dx = offset.x - centerX
                        val dy = offset.y - centerY
                        val distance = kotlin.math.sqrt(dx * dx + dy * dy)

                        if (distance <= radius) {
                            val angle = kotlin.math.atan2(dy, dx) * (180f / PI.toFloat())

                            // 计算点击的索引
                            val dataSet = data.getDataSets().firstOrNull() ?: return@detectTapGestures
                            val entryCount = dataSet.getEntryCount()
                            if (entryCount > 0) {
                                val sliceAngle = 360f / entryCount
                                var index = ((angle + 90f + 360f) % 360f / sliceAngle).toInt()
                                index = index % entryCount

                                selectedIndex = index
                                val entry = dataSet.getEntryForIndex(index)
                                if (entry != null) {
                                    val highlight = Highlight(
                                        x = offset.x,
                                        y = offset.y,
                                        xIndex = index,
                                        dataSetIndex = 0
                                    )
                                    onValueSelected(entry, highlight)
                                }
                            }
                        } else {
                            selectedIndex = -1
                            onValueDeselected()
                        }
                    }
                }
            }
    ) {
        bounds?.let { b ->
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                drawRadarChartContentOptimized(
                    data = data,
                    bounds = b,
                    style = style,
                    animationProgress = animationProgress,
                    selectedIndex = selectedIndex,
                    textMeasurer = textMeasurer,
                    webPath = webPath,
                    dataPath = dataPath,
                    webStroke = webStroke,
                    dataStroke = dataStroke,
                    radarLayout = radarLayout
                )
            }
        }

        // 绘制图例 - 使用 Alignment.BottomStart 钉在底部
        if (legendStyle.isEnabled) {
            val legendEntries = data.getDataSets().map { dataSet ->
                LegendEntry(
                    label = dataSet.label,
                    color = dataSet.fillColor,
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
    }
}

/**
 * 缓存的雷达图布局信息
 */
private data class RadarLayout(
    val centerX: Float,
    val centerY: Float,
    val radius: Float,
    val entryCount: Int,
    val sliceAngle: Float,
    val yMin: Float,
    val yRange: Float,
    val entries: List<RadarEntry>
)

/**
 * 优化后的绘制函数
 */
private fun DrawScope.drawRadarChartContentOptimized(
    data: RadarChartData,
    bounds: ChartBounds,
    style: RadarChartStyle,
    animationProgress: Float,
    selectedIndex: Int,
    textMeasurer: TextMeasurer,
    webPath: Path,
    dataPath: Path,
    webStroke: Stroke,
    dataStroke: Stroke,
    radarLayout: RadarLayout?
) {
    val layout = radarLayout ?: return
    if (layout.entries.isEmpty()) return

    val dataSet = data.getDataSets().firstOrNull() ?: return

    // 绘制蜘蛛网（背景网格）
    val webLineCount = 5
    for (i in 1..webLineCount) {
        val webRadius = layout.radius * i / webLineCount
        webPath.reset()

        for (j in 0 until layout.entryCount) {
            val angle = (layout.sliceAngle * j - 90f) * (PI.toFloat() / 180f)
            val x = layout.centerX + cos(angle) * webRadius
            val y = layout.centerY + sin(angle) * webRadius

            if (j == 0) {
                webPath.moveTo(x, y)
            } else {
                webPath.lineTo(x, y)
            }
        }
        webPath.close()

        drawPath(
            path = webPath,
            color = style.webColor.copy(alpha = style.webAlpha / 255f),
            style = webStroke
        )
    }

    // 绘制轴线
    for (i in 0 until layout.entryCount) {
        val angle = (layout.sliceAngle * i - 90f) * (PI.toFloat() / 180f)
        val x = layout.centerX + cos(angle) * layout.radius
        val y = layout.centerY + sin(angle) * layout.radius

        drawLine(
            color = style.webColor.copy(alpha = style.webAlpha / 255f),
            start = Offset(layout.centerX, layout.centerY),
            end = Offset(x, y),
            strokeWidth = style.webLineWidth
        )
    }

    // 绘制数据
    dataPath.reset()
    for ((index, entry) in layout.entries.withIndex()) {
        val valueRatio = (entry.y - layout.yMin) / layout.yRange
        val valueRadius = layout.radius * valueRatio * animationProgress

        val angle = (layout.sliceAngle * index - 90f) * (PI.toFloat() / 180f)
        val x = layout.centerX + cos(angle) * valueRadius
        val y = layout.centerY + sin(angle) * valueRadius

        if (index == 0) {
            dataPath.moveTo(x, y)
        } else {
            dataPath.lineTo(x, y)
        }
    }
    dataPath.close()

    // 绘制填充
    if (style.isDrawFilled) {
        drawPath(
            path = dataPath,
            color = dataSet.fillColor.copy(alpha = dataSet.fillAlpha)
        )
    }

    // 绘制边框
    drawPath(
        path = dataPath,
        color = dataSet.fillColor,
        style = dataStroke
    )

    // 绘制数据点
    for ((index, entry) in layout.entries.withIndex()) {
        val valueRatio = (entry.y - layout.yMin) / layout.yRange
        val valueRadius = layout.radius * valueRatio * animationProgress

        val angle = (layout.sliceAngle * index - 90f) * (PI.toFloat() / 180f)
        val x = layout.centerX + cos(angle) * valueRadius
        val y = layout.centerY + sin(angle) * valueRadius

        val isSelected = index == selectedIndex
        val pointRadius = if (isSelected) 6f else 4f

        drawCircle(
            color = if (isSelected) style.highlightCircleFillColor else dataSet.fillColor,
            radius = pointRadius,
            center = Offset(x, y)
        )
    }

    // 绘制轴标签
    if (style.isDrawLabels) {
        val labels = data.getLabels()
        for ((index, entry) in layout.entries.withIndex()) {
            val angle = (layout.sliceAngle * index - 90f) * (PI.toFloat() / 180f)
            val labelRadius = layout.radius + 30f
            val x = layout.centerX + cos(angle) * labelRadius
            val y = layout.centerY + sin(angle) * labelRadius

            val labelText = labels.getOrNull(index) ?: entry.y.toInt().toString()

            val textLayoutResult = textMeasurer.measure(
                text = labelText,
                style = TextStyle(
                    color = style.labelColor,
                    fontSize = style.labelTextSize.sp
                )
            )

            val textWidth = textLayoutResult.size.width
            val textHeight = textLayoutResult.size.height

            val offsetX = when {
                cos(angle) > 0.1f -> 0f
                cos(angle) < -0.1f -> -textWidth.toFloat()
                else -> -textWidth / 2f
            }
            val offsetY = -textHeight / 2f

            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(x + offsetX, y + offsetY)
            )
        }
    }
}
