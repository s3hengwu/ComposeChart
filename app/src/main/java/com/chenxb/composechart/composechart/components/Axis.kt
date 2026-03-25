package com.chenxb.composechart.composechart.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 坐标轴位置
 */
enum class AxisPosition {
    LEFT, RIGHT, BOTTOM, TOP, BOTH_SIDED
}

/**
 * 坐标轴标签位置
 */
enum class YAxisLabelPosition {
    OUTSIDE_CHART, INSIDE_CHART
}

/**
 * 网格线样式
 */
data class GridStyle(
    val isDrawGridLinesEnabled: Boolean = true,
    val gridColor: Color = Color.LightGray.copy(alpha = 0.7f),
    val gridLineWidth: Float = 0.5f,
    val gridLineDashPathEffect: PathEffect? = null,
    val drawGridLinesBehindData: Boolean = true
)

/**
 * X轴配置
 */
data class XAxisConfig(
    val isEnabled: Boolean = true,
    val position: AxisPosition = AxisPosition.BOTTOM,

    val labelRotationAngle: Float = 0f,
    val labelCount: Int = 6,
    val labelTextColor: Color = Color.Black,
    val labelTextSize: Float = 10f,
    val labelFontFamily: FontFamily = FontFamily.Default,

    val gridStyle: GridStyle = GridStyle(),

    val axisLineColor: Color = Color.Gray,
    val axisLineWidth: Float = 1f,

    val isDrawAxisLineEnabled: Boolean = true,
    val isDrawLabelsEnabled: Boolean = true,
    val isDrawGridLinesEnabled: Boolean = true,

    val granularity: Float = 1f,
    val axisMinimum: Float? = null,
    val axisMaximum: Float? = null,

    val valueFormatter: ((Float) -> String)? = null,
    val labelFontWeight: FontWeight = FontWeight.Normal,
    val labelFontStyle: FontStyle = FontStyle.Normal,

    // 是否将标签居中显示在分组中（用于柱状图等离散数据）
    val centerLabels: Boolean = false
)

/**
 * Y轴配置
 */
data class YAxisConfig(
    val isEnabled: Boolean = true,
    val position: AxisPosition = AxisPosition.LEFT,

    val labelCount: Int = 6,
    val labelTextColor: Color = Color.Black,
    val labelTextSize: Float = 10f,
    val labelFontFamily: FontFamily = FontFamily.Default,
    val labelPosition: YAxisLabelPosition = YAxisLabelPosition.OUTSIDE_CHART,

    val gridStyle: GridStyle = GridStyle(),

    val axisLineColor: Color = Color.Gray,
    val axisLineWidth: Float = 1f,

    val isDrawAxisLineEnabled: Boolean = true,
    val isDrawLabelsEnabled: Boolean = true,
    val isDrawGridLinesEnabled: Boolean = true,

    val granularity: Float = 1f,
    val axisMinimum: Float? = null,
    val axisMaximum: Float? = null,
    val spaceTop: Float = 0.1f,
    val spaceBottom: Float = 0.1f,

    val isInverted: Boolean = false,
    val isForceLabelsEnabled: Boolean = false,

    val valueFormatter: ((Float) -> String)? = null,
    val labelFontWeight: FontWeight = FontWeight.Normal,
    val labelFontStyle: FontStyle = FontStyle.Normal,

    val limitLines: LimitLines = LimitLines()
)

/**
 * 轴依赖关系
 */
enum class AxisDependency {
    LEFT, RIGHT
}

/**
 * 坐标轴数据类
 */
data class AxisData(
    val xAxis: XAxisConfig = XAxisConfig(),
    val yLeftAxis: YAxisConfig = YAxisConfig(),
    val yRightAxis: YAxisConfig = YAxisConfig()
) {
    fun getAxis(isLeft: Boolean): YAxisConfig = if (isLeft) yLeftAxis else yRightAxis
}
