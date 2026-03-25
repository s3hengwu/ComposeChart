package com.chenxb.composechart.composechart.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chenxb.composechart.composechart.ChartBounds
import com.chenxb.composechart.composechart.data.PieChartData
import com.chenxb.composechart.composechart.data.PieDataSet
import com.chenxb.composechart.composechart.data.PieEntry
import com.chenxb.composechart.composechart.components.Highlight
import com.chenxb.composechart.composechart.components.LegendEntry
import com.chenxb.composechart.composechart.components.LegendForm
import com.chenxb.composechart.composechart.components.LegendStyle
import com.chenxb.composechart.composechart.components.LegendView
import com.chenxb.composechart.composechart.gestures.ChartGestureState
import com.chenxb.composechart.composechart.style.PieChartStyle
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 饼图
 */
@Composable
fun PieChart(
    data: PieChartData,
    modifier: Modifier = Modifier,
    style: PieChartStyle = PieChartStyle(),
    legendStyle: LegendStyle = LegendStyle(),
    enableGestures: Boolean = false,
    animationProgress: Float = 1f,
    onValueSelected: (PieEntry, Highlight) -> Unit = { _, _ -> },
    onValueDeselected: () -> Unit = {}
) {
    var chartSize by remember { mutableStateOf(IntSize.Zero) }
    var selectedIndex by remember { mutableStateOf(-1) }
    // Legend 真实高度（px），由 onSizeChanged 测量
    var legendHeightPx by remember { mutableStateOf(0f) }
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    // PieChart 的 bounds 需要为 Legend 留出底部空间
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

    // 缓存饼图切片计算
    val pieLayout by remember(data, bounds, style, animationProgress) {
        derivedStateOf {
            if (bounds == null) {
                null
            } else {
                val dataSet = data.getDataSets().firstOrNull()
                val entries = dataSet?.getEntries() ?: emptyList()
                val total = entries.sumOf { it.y.toDouble() }.toFloat()
                val radius = min(bounds.width, bounds.height) / 2 * 0.8f
                val holeRadius = radius * (style.holeRadiusPercent / 100f)
                val transparentCircleRadius = radius * (style.transparentCircleRadiusPercent / 100f)

                val slices = entries.mapIndexed { index, entry ->
                    val sweepAngle = (entry.y / total) * 360f * animationProgress
                    val color = ColorTemplates.VORDIPLOM_COLORS[index % ColorTemplates.VORDIPLOM_COLORS.size]
                    CachedPieSlice(
                        entry = entry,
                        sweepAngle = sweepAngle,
                        color = color,
                        index = index
                    )
                }

                PieLayout(
                    centerX = (bounds.left + bounds.right) / 2,
                    centerY = (bounds.top + bounds.bottom) / 2,
                    radius = radius,
                    holeRadius = holeRadius,
                    transparentCircleRadius = transparentCircleRadius,
                    total = total,
                    slices = slices
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
                        val radius = min(b.width, b.height) / 2 * 0.8f

                        val dx = offset.x - centerX
                        val dy = offset.y - centerY
                        val distance = sqrt(dx * dx + dy * dy)

                        if (distance <= radius) {
                            var angle = atan2(dy, dx) * (180f / Math.PI.toFloat())
                            angle = (angle + 360f) % 360f

                            // 找到点击的扇区
                            val dataSet = data.getDataSets().firstOrNull() ?: return@detectTapGestures
                            val entries = dataSet.getEntries()
                            val total = entries.sumOf { it.y.toDouble() }.toFloat()

                            var startAngle = -90f
                            for ((index, entry) in entries.withIndex()) {
                                val sweepAngle = (entry.y / total) * 360f
                                val endAngle = startAngle + sweepAngle

                                val normalizedAngle = (angle + 90f) % 360f
                                val normalizedStart = (startAngle + 90f) % 360f
                                val normalizedEnd = (endAngle + 90f) % 360f

                                val isInSlice = if (normalizedStart < normalizedEnd) {
                                    normalizedAngle >= normalizedStart && normalizedAngle <= normalizedEnd
                                } else {
                                    normalizedAngle >= normalizedStart || normalizedAngle <= normalizedEnd
                                }

                                if (isInSlice) {
                                    selectedIndex = index
                                    val highlight = Highlight(
                                        x = offset.x,
                                        y = offset.y,
                                        xIndex = index,
                                        dataSetIndex = 0
                                    )
                                    onValueSelected(entry, highlight)
                                    break
                                }

                                startAngle = endAngle
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
                drawPieChartContentOptimized(
                    style = style,
                    selectedIndex = selectedIndex,
                    textMeasurer = textMeasurer,
                    pieLayout = pieLayout
                )
            }
        }

        // 绘制图例 - 使用 Alignment.BottomStart 钉在底部
        if (legendStyle.isEnabled) {
            val dataSet = data.getDataSets().firstOrNull()
            val legendEntries = dataSet?.getEntries()?.mapIndexed { index, entry ->
                val color = ColorTemplates.VORDIPLOM_COLORS[index % ColorTemplates.VORDIPLOM_COLORS.size]
                LegendEntry(
                    label = entry.label ?: "",
                    color = color,
                    form = LegendForm.SQUARE,
                    formSize = 12f
                )
            } ?: emptyList()
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
 * 缓存的饼图切片
 */
private data class CachedPieSlice(
    val entry: PieEntry,
    val sweepAngle: Float,
    val color: Color,
    val index: Int
)

/**
 * 缓存的饼图布局
 */
private data class PieLayout(
    val centerX: Float,
    val centerY: Float,
    val radius: Float,
    val holeRadius: Float,
    val transparentCircleRadius: Float,
    val total: Float,
    val slices: List<CachedPieSlice>
)

/**
 * 优化后的绘制函数
 */
private fun DrawScope.drawPieChartContentOptimized(
    style: PieChartStyle,
    selectedIndex: Int,
    textMeasurer: TextMeasurer,
    pieLayout: PieLayout?
) {
    val layout = pieLayout ?: return
    if (layout.slices.isEmpty()) return

    val sliceSpaceRad = if (layout.slices.size > 1) style.sliceSpace * (180f / Math.PI).toFloat() / layout.radius else 0f

    var startAngle = -90f + style.rotationAngle

    for (slice in layout.slices) {
        val isSelected = slice.index == selectedIndex
        val selectionShift = if (isSelected) style.selectionShift else 0f

        val sliceCenterAngle = startAngle + slice.sweepAngle / 2
        val sliceCenterRad = sliceCenterAngle * (Math.PI.toFloat() / 180f)
        val shiftX = cos(sliceCenterRad) * selectionShift
        val shiftY = sin(sliceCenterRad) * selectionShift

        val sliceCenter = Offset(layout.centerX + shiftX, layout.centerY + shiftY)

        val adjustedStartAngle = startAngle + sliceSpaceRad / 2
        val adjustedSweepAngle = (slice.sweepAngle - sliceSpaceRad).coerceAtLeast(0f)

        // 绘制扇区
        if (adjustedSweepAngle > 0) {
            drawArc(
                color = slice.color,
                startAngle = adjustedStartAngle,
                sweepAngle = adjustedSweepAngle,
                useCenter = true,
                topLeft = Offset(sliceCenter.x - layout.radius, sliceCenter.y - layout.radius),
                size = Size(layout.radius * 2, layout.radius * 2)
            )
        }

        // 绘制标签和数值
        if (style.isDrawEntryLabelsEnabled || style.isDrawValues) {
            val labelAngle = startAngle + slice.sweepAngle / 2
            val labelRad = labelAngle * (Math.PI.toFloat() / 180f)
            val labelRadius = layout.radius + 70f
            val labelX = layout.centerX + cos(labelRad) * labelRadius
            val labelY = layout.centerY + sin(labelRad) * labelRadius

            val labelText = buildString {
                if (style.isDrawEntryLabelsEnabled && !slice.entry.label.isNullOrEmpty()) {
                    append(slice.entry.label)
                }
                if (style.isDrawValues) {
                    if (isNotEmpty()) append(" ")
                    append("${(slice.entry.y / layout.total * 100).toInt()}%")
                }
            }

            if (labelText.isNotEmpty()) {
                val textLayoutResult = textMeasurer.measure(
                    text = labelText,
                    style = TextStyle(
                        color = slice.color,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
                val textWidth = textLayoutResult.size.width
                val textHeight = textLayoutResult.size.height

                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(labelX - textWidth / 2, labelY - textHeight / 2)
                )
            }
        }

        startAngle += slice.sweepAngle
    }

    // 绘制透明圆环
    if (style.isDrawHoleEnabled && layout.transparentCircleRadius > layout.holeRadius) {
        drawCircle(
            color = style.transparentCircleColor,
            radius = layout.transparentCircleRadius,
            center = Offset(layout.centerX, layout.centerY)
        )
    }

    // 绘制中心空洞
    if (style.isDrawHoleEnabled) {
        drawCircle(
            color = style.holeColor,
            radius = layout.holeRadius,
            center = Offset(layout.centerX, layout.centerY)
        )
    }

    // 绘制中心文字
    if (style.isDrawCenterTextEnabled && style.centerText.isNotEmpty()) {
        val centerTextLayoutResult = textMeasurer.measure(
            text = style.centerText,
            style = TextStyle(
                color = style.centerTextColor,
                fontSize = style.centerTextSize.sp,
                fontWeight = FontWeight.Bold
            )
        )
        val textWidth = centerTextLayoutResult.size.width
        val textHeight = centerTextLayoutResult.size.height

        drawText(
            textLayoutResult = centerTextLayoutResult,
            topLeft = Offset(layout.centerX - textWidth / 2, layout.centerY - textHeight / 2)
        )
    }
}

/**
 * 颜色模板
 */
private object ColorTemplates {
    val VORDIPLOM_COLORS = listOf(
        Color(0xFF3F51B5), // Indigo
        Color(0xFFE91E63), // Pink
        Color(0xFF4CAF50), // Green
        Color(0xFFFFC107), // Amber
        Color(0xFF9C27B0), // Purple
        Color(0xFF03A9F4), // Light Blue
        Color(0xFFFF5722), // Deep Orange
        Color(0xFF795548), // Brown
        Color(0xFF607D8B), // Blue Grey
        Color(0xFF00BCD4)  // Cyan
    )
}
