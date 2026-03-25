package com.chenxb.composechart.composechart.style

import androidx.compose.ui.graphics.Color

/**
 * 颜色模板
 * 对应 MPAndroidChart 的 ColorTemplate
 */
object ColorTemplates {
    // 预定义配色方案
    val VORDIPLOM_COLORS = listOf(
        Color(0xFF3E5DBA),  // 深蓝
        Color(0xFF8E24AA),  // 紫色
        Color(0xFF3944BC),  // 靛蓝
        Color(0xFFE91E63),  // 粉色
        Color(0xFFFF5722),  // 深橙
        Color(0xFF00BCD4),  // 青色
        Color(0xFF009688),  // 蓝绿色
        Color(0xFF8BC34A),  // 浅绿色
        Color(0xFFCDDC39),  // 酸橙色
        Color(0xFFFFEB3B),  // 黄色
        Color(0xFFFF9800),  // 橙色
        Color(0xFF795548),  // 棕色
        Color(0xFF607D8B),  // 蓝灰色
        Color(0xFF9E9E9E),  // 灰色
        Color(0xFF673AB7),  // 深紫色
        Color(0xFF03A9F4),  // 浅蓝色
        Color(0xFF4CAF50),  // 绿色
        Color(0xFFFFC107),  // 琥珀色
        Color(0xFF03DAC6),  // 青色
        Color(0xFFEF5350)   // 红色
    )

    val JOYFUL_COLORS = listOf(
        Color(0xFF00BCD4),
        Color(0xFFFFEB3B),
        Color(0xFFFF9800),
        Color(0xFFE91E63),
        Color(0xFF9C27B0),
        Color(0xFF673AB7),
        Color(0xFF3F51B5),
        Color(0xFF2196F3),
        Color(0xFF00BCD4),
        Color(0xFF009688),
        Color(0xFF4CAF50),
        Color(0xFF8BC34A),
        Color(0xFFCDDC39),
        Color(0xFFFFEB3B),
        Color(0xFFFFC107),
        Color(0xFFFF9800),
        Color(0xFFFF5722),
        Color(0xFFF44336),
        Color(0xFFE91E63),
        Color(0xFF9C27B0)
    )

    val COLORFUL_COLORS = listOf(
        Color(0xFF3F51B5),
        Color(0xFFE91E63),
        Color(0xFF9C27B0),
        Color(0xFF673AB7),
        Color(0xFF2196F3),
        Color(0xFF00BCD4),
        Color(0xFF009688),
        Color(0xFF4CAF50),
        Color(0xFFFFEB3B),
        Color(0xFFFF9800),
        Color(0xFFFF5722),
        Color(0xFFF44336)
    )

    val LIBERTY_COLORS = listOf(
        Color(0xFF2C3E50),
        Color(0xFF7F8C8D),
        Color(0xFF8E44AD),
        Color(0xFF2980B9),
        Color(0xFF27AE60),
        Color(0xFFE67E22),
        Color(0xFFE74C3C),
        Color(0xFF16A085),
        Color(0xFF3498DB),
        Color(0xFF9B59B6),
        Color(0xFFC0392B),
        Color(0xFFD35400)
    )

    val PASTEL_COLORS = listOf(
        Color(0xFFA8E6CF),
        Color(0xFFDCEDC1),
        Color(0xFFFFD3B6),
        Color(0xFFFFAAA5),
        Color(0xFFFFCCBC),
        Color(0xFFD4A5A5),
        Color(0xFFA8D8EA),
        Color(0xFFFFCBA4),
        Color(0xFFE2F0CB),
        Color(0xFFB5EAD7),
        Color(0xFFC7CEEA),
        Color(0xFFFF9AA2)
    )

    val MATERIAL_COLORS = listOf(
        Color(0xFFE91E63),  // Pink
        Color(0xFF9C27B0),  // Purple
        Color(0xFF673AB7),  // Deep Purple
        Color(0xFF3F51B5),  // Indigo
        Color(0xFF2196F3),  // Blue
        Color(0xFF03A9F4),  // Light Blue
        Color(0xFF00BCD4),  // Cyan
        Color(0xFF009688),  // Teal
        Color(0xFF4CAF50),  // Green
        Color(0xFF8BC34A),  // Light Green
        Color(0xFFCDDC39),  // Lime
        Color(0xFFFFEB3B),  // Yellow
        Color(0xFFFFC107),  // Amber
        Color(0xFFFF9800),  // Orange
        Color(0xFFFF5722),  // Deep Orange
        Color(0xFFF44336)   // Red
    )

    // HOLO 颜色
    val HOLO_BLUE = Color(0xFF33B5E5)
    val HOLO_ORANGE = Color(0xFFFFBB33)
    val HOLO_RED = Color(0xFFFF4444)
    val HOLO_GREEN = Color(0xFF99BB00)
    val HOLO_BLUE_LIGHT = Color(0xFF6680CC)
    val HOLO_BLUE_DARK = Color(0xFF0055AA)
    val HOLO_ORANGE_LIGHT = Color(0xFFFFAA33)
    val HOLO_ORANGE_DARK = Color(0xFFCC8800)
    val HOLO_GREEN_LIGHT = Color(0xFFAADD00)
    val HOLO_GREEN_DARK = Color(0xFF557700)
    val HOLO_RED_LIGHT = Color(0xFFFF6666)
    val HOLO_RED_DARK = Color(0xFFCC0000)

    /**
     * 获取渐变颜色列表
     */
    fun getVordiplomColors(): List<Color> = VORDIPLOM_COLORS

    fun getJoyfulColors(): List<Color> = JOYFUL_COLORS

    fun getColorfulColors(): List<Color> = COLORFUL_COLORS

    fun getLibertyColors(): List<Color> = LIBERTY_COLORS

    fun getPastelColors(): List<Color> = PASTEL_COLORS

    fun getMaterialColors(): List<Color> = MATERIAL_COLORS
}
