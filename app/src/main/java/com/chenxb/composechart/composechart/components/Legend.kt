package com.chenxb.composechart.composechart.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 图例垂直对齐方式
 */
enum class LegendVerticalAlignment {
    TOP, CENTER, BOTTOM
}

/**
 * 图例水平对齐方式
 */
enum class LegendHorizontalAlignment {
    LEFT, CENTER, RIGHT
}

/**
 * 图例方向
 */
enum class LegendOrientation {
    HORIZONTAL, VERTICAL
}

/**
 * 图例形状
 */
enum class LegendForm {
    SQUARE, CIRCLE, LINE, EMPTY
}

/**
 * 图例样式
 */
data class LegendStyle(
    val isEnabled: Boolean = true,

    val verticalAlignment: LegendVerticalAlignment = LegendVerticalAlignment.BOTTOM,
    val horizontalAlignment: LegendHorizontalAlignment = LegendHorizontalAlignment.CENTER,
    val orientation: LegendOrientation = LegendOrientation.HORIZONTAL,

    val isDrawInside: Boolean = false,

    val textColor: Color = Color.Black,
    val textSize: Float = 12f,
    val textFontFamily: FontFamily = FontFamily.Default,
    val textFontWeight: FontWeight = FontWeight.Normal,
    val textFontStyle: FontStyle = FontStyle.Normal,

    val form: LegendForm = LegendForm.SQUARE,
    val formSize: Float = 12f,
    val formLineWidth: Float = 1f,
    val formLineDashEffect: List<Float>? = null,

    val xEntrySpace: Dp = 4.dp,
    val yEntrySpace: Dp = 0.dp,
    val xOffset: Dp = 0.dp,
    val yOffset: Dp = 0.dp,

    val isWordWrapEnabled: Boolean = true,
    val maxSizePercent: Float = 0.95f,

    val isDrawLegend: Boolean = true
)

/**
 * 图例条目
 */
data class LegendEntry(
    val label: String,
    val color: Color,
    val form: LegendForm = LegendForm.SQUARE,
    val formSize: Float = 12f
)
