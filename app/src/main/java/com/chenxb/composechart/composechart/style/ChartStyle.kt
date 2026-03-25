package com.chenxb.composechart.composechart.style

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// 定义颜色常量
private val HOLO_BLUE = Color(0xFF33B5E5)
private val HOLO_BLUE_LIGHT = Color(0xFF6680CC)
private val HOLO_ORANGE = Color(0xFFFFBB33)

/**
 * 图表基础样式
 */
data class ChartStyle(
    val backgroundColor: Color = Color.White,
    val descriptionEnabled: Boolean = false,
    val descriptionText: String = "",
    val descriptionColor: Color = Color.Gray,
    val descriptionTextSize: Float = 12f,
    val descriptionPosition: Offset = Offset(10f, 10f),

    val isDragEnabled: Boolean = true,
    val isScaleEnabled: Boolean = true,
    val isPinchZoomEnabled: Boolean = true,
    val isHighlightEnabled: Boolean = true,
    val isChartRotatable: Boolean = false,

    val maxVisibleValueCount: Int = 100,
    val autoScaleMinMaxEnabled: Boolean = true,

    val extraTopOffset: Dp = 0.dp,
    val extraBottomOffset: Dp = 0.dp,
    val extraLeftOffset: Dp = 0.dp,
    val extraRightOffset: Dp = 0.dp
)

/**
 * 折线图样式
 */
data class LineChartStyle(
    val baseStyle: ChartStyle = ChartStyle(),

    val lineWidth: Float = 1f,
    val circleRadius: Float = 4f,
    val circleHoleRadius: Float = 2f,
    val isDrawCircleHole: Boolean = true,
    val isDrawLine: Boolean = true,
    val isDrawCircles: Boolean = true,
    val isDrawFilled: Boolean = false,
    val isDrawCubic: Boolean = false,
    val isDrawStepped: Boolean = false,

    val fillColor: Color = HOLO_BLUE_LIGHT.copy(alpha = 0.3f),
    val fillAlpha: Float = 0.3f,
    val lineColor: Color = HOLO_BLUE,
    val circleColor: Color = HOLO_BLUE,

    val isDrawDashedLine: Boolean = false,
    val dashLength: Float = 10f,
    val dashSpaceLength: Float = 5f,

    val highlightColor: Color = Color.Gray,
    val highlightLineWidth: Float = 1f,

    val isDrawValues: Boolean = true,
    val valueTextColor: Color = Color.Black,
    val valueTextSize: Float = 10f
)

/**
 * 柱状图样式
 */
data class BarChartStyle(
    val baseStyle: ChartStyle = ChartStyle(),

    val barWidth: Float = 0.9f,
    val barSpace: Float = 0.05f,
    val groupSpace: Float = 0.1f,

    val isDrawBarShadow: Boolean = false,
    val isDrawValuesAboveBar: Boolean = true,
    val isDrawValues: Boolean = true,
    val shadowColor: Color = Color.LightGray,
    val shadowWidth: Float = 4f,

    val valueTextColor: Color = Color.Black,
    val valueTextSize: Float = 10f,

    val highlightColor: Color = HOLO_BLUE,
    val highlightAlpha: Float = 1f,

    val isDrawGradient: Boolean = false,
    val gradientStartColor: Color = HOLO_BLUE,
    val gradientEndColor: Color = HOLO_BLUE_LIGHT
)

/**
 * 饼图样式
 */
data class PieChartStyle(
    val baseStyle: ChartStyle = ChartStyle(),

    val isDrawHoleEnabled: Boolean = true,
    val holeColor: Color = Color.White,
    val holeRadiusPercent: Float = 50f,
    val transparentCircleColor: Color = Color.White.copy(alpha = 0.5f),
    val transparentCircleRadiusPercent: Float = 55f,

    val isDrawCenterTextEnabled: Boolean = false,
    val centerText: String = "",
    val centerTextColor: Color = Color.Black,
    val centerTextSize: Float = 16f,

    val isDrawEntryLabelsEnabled: Boolean = true,
    val entryLabelColor: Color = Color.White,
    val entryLabelTextSize: Float = 12f,

    val isUsePercentValues: Boolean = true,
    val isDrawValues: Boolean = true,
    val valueTextColor: Color = Color.White,
    val valueTextSize: Float = 12f,

    val isRotationEnabled: Boolean = true,
    val rotationAngle: Float = 0f,

    val sliceSpace: Float = 2f,
    val selectionShift: Float = 5f,

    val isDrawRoundedSlicesEnabled: Boolean = false,
    val minAngleForSlices: Float = 0f,

    // 内圆环样式
    val drawInnerArc: Boolean = false,
    val innerArcColor: Color = Color.Gray,
    val innerArcLineWidth: Float = 1f
)

/**
 * 散点图样式
 */
data class ScatterChartStyle(
    val baseStyle: ChartStyle = ChartStyle(),

    val scatterShapeSize: Float = 10f,
    val highlightColor: Color = Color.Gray,

    val isDrawValues: Boolean = false,
    val valueTextColor: Color = Color.Black,
    val valueTextSize: Float = 10f
)

/**
 * K线图样式
 */
data class CandleChartStyle(
    val baseStyle: ChartStyle = ChartStyle(),

    val shadowColor: Color = Color.DarkGray,
    val shadowWidth: Float = 1f,
    val decreasingColor: Color = Color.Red,
    val decreasingPaintStyle: String = "FILL",
    val increasingColor: Color = Color.Green,
    val increasingPaintStyle: String = "STROKE",
    val neutralColor: Color = Color.Blue,

    val isDrawValues: Boolean = false,
    val valueTextColor: Color = Color.Black,
    val valueTextSize: Float = 10f
)

/**
 * 雷达图样式
 */
data class RadarChartStyle(
    val baseStyle: ChartStyle = ChartStyle(),

    val webLineWidth: Float = 1f,
    val webColor: Color = Color(0xFFCCCCCC),
    val webLineWidthInner: Float = 0.5f,
    val webColorInner: Color = Color(0xFFCCCCCC),
    val webAlpha: Int = 100,

    val isDrawLabels: Boolean = true,
    val labelColor: Color = Color.Gray,
    val labelTextSize: Float = 10f,

    val isDrawFilled: Boolean = false,
    val fillColor: Color = HOLO_BLUE_LIGHT.copy(alpha = 0.3f),
    val fillAlpha: Float = 0.3f,

    val rotationEnabled: Boolean = true,
    val rotationAngle: Float = 0f,

    val isDrawHighlightCircleEnabled: Boolean = true,
    val highlightCircleFillColor: Color = Color.White,
    val highlightCircleStrokeColor: Color = Color.Gray,
    val highlightCircleStrokeWidth: Float = 1f,
    val highlightCircleInnerRadius: Float = 3f,
    val highlightCircleOuterRadius: Float = 4f,

    val isDrawValues: Boolean = false,
    val valueTextColor: Color = Color.Black,
    val valueTextSize: Float = 10f
)

/**
 * 气泡图样式
 */
data class BubbleChartStyle(
    val baseStyle: ChartStyle = ChartStyle(),

    val maxSize: Float = 30f,
    val normalizeSize: Boolean = true,

    val isDrawValues: Boolean = false,
    val valueTextColor: Color = Color.White,
    val valueTextSize: Float = 8f,

    val highlightColor: Color = Color.Gray,
    val highlightAlpha: Float = 1f
)
