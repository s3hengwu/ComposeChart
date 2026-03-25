package com.chenxb.composechart.composechart.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect

/**
 * 高亮样式配置
 * 控制选中数据点时的高亮线显示
 */
data class HighlightStyle(
    /** 是否绘制水平高亮线 */
    val drawHorizontalHighlightLine: Boolean = true,

    /** 是否绘制垂直高亮线 */
    val drawVerticalHighlightLine: Boolean = true,

    /** 高亮线宽度 */
    val highlightLineWidth: Float = 1f,

    /** 高亮线颜色 */
    val highlightLineColor: Color = Color.Gray.copy(alpha = 0.5f),

    /** 高亮线虚线效果 */
    val highlightLineDashPathEffect: PathEffect? = null,

    /** 是否绘制高亮点 */
    val drawHighlightPoint: Boolean = true,

    /** 高亮点外圈半径 */
    val highlightPointOuterRadius: Float = 8f,

    /** 高亮点外圈颜色 */
    val highlightPointOuterColor: Color = Color.Gray,

    /** 高亮点内圈半径 */
    val highlightPointInnerRadius: Float = 4f,

    /** 高亮点内圈颜色 */
    val highlightPointInnerColor: Color = Color.White,

    /** 是否绘制高亮背景 */
    val drawHighlightBackground: Boolean = false,

    /** 高亮背景颜色 */
    val highlightBackgroundColor: Color = Color.Gray.copy(alpha = 0.1f),

    /** 高亮背景宽度（用于柱状图等） */
    val highlightBackgroundWidth: Float = 40f
) {
    companion object {
        /** 默认样式 - 十字线 */
        val Default = HighlightStyle()

        /** 只有垂直线 */
        val VerticalOnly = HighlightStyle(
            drawHorizontalHighlightLine = false,
            drawVerticalHighlightLine = true
        )

        /** 只有水平线 */
        val HorizontalOnly = HighlightStyle(
            drawHorizontalHighlightLine = true,
            drawVerticalHighlightLine = false
        )

        /** 十字线带虚线效果 */
        val Dashed = HighlightStyle(
            highlightLineDashPathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f))
        )

        /** 简单样式 - 只显示高亮点 */
        val Simple = HighlightStyle(
            drawHorizontalHighlightLine = false,
            drawVerticalHighlightLine = false,
            drawHighlightPoint = true
        )

        /** 完整样式 - 十字线 + 高亮点 + 背景 */
        val Full = HighlightStyle(
            drawHorizontalHighlightLine = true,
            drawVerticalHighlightLine = true,
            drawHighlightPoint = true,
            drawHighlightBackground = true
        )
    }
}
