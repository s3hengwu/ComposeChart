package com.chenxb.composechart.composechart.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect

/**
 * 限制线标签位置
 */
enum class LimitLabelPosition {
    RIGHT_TOP, RIGHT_BOTTOM, LEFT_TOP, LEFT_BOTTOM
}

/**
 * 限制线
 * 对应 MPAndroidChart 的 LimitLine
 */
data class LimitLine(
    val limit: Float,
    val label: String = "",
    val lineWidth: Float = 1f,
    val lineColor: Color = Color.Red,
    val lineDashPathEffect: PathEffect? = null,
    val labelPosition: LimitLabelPosition = LimitLabelPosition.RIGHT_TOP,
    val labelColor: Color = Color.Red,
    val labelTextSize: Float = 10f,
    val isDrawLabelBehindData: Boolean = true
)

/**
 * 限制线列表
 */
data class LimitLines(
    val lines: List<LimitLine> = emptyList()
) {
    fun addLimitLine(limitLine: LimitLine): LimitLines {
        return LimitLines(lines + limitLine)
    }

    fun removeLimitLine(limitLine: LimitLine): LimitLines {
        return LimitLines(lines - limitLine)
    }

    fun getLimitLines(): List<LimitLine> = lines
}
