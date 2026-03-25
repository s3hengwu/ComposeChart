package com.chenxb.composechart.composechart.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.sp

/**
 * 数值标签绘制工具
 * 提供通用的数值标签绘制功能
 */

/**
 * 默认数值格式化器
 */
val DefaultValueFormatter: (Float) -> String = { value ->
    if (value == value.toLong().toFloat()) {
        value.toLong().toString()
    } else {
        String.format("%.1f", value)
    }
}

/**
 * 百分比格式化器
 */
val PercentValueFormatter: (Float) -> String = { value ->
    String.format("%.1f%%", value)
}

/**
 * 货币格式化器
 */
fun currencyValueFormatter(currencySymbol: String = "$"): (Float) -> String = { value ->
    "$currencySymbol${String.format("%.2f", value)}"
}

/**
 * 大数值格式化器（K, M, B）
 */
val LargeValueFormatter: (Float) -> String = { value ->
    when {
        value >= 1_000_000_000 -> String.format("%.1fB", value / 1_000_000_000)
        value >= 1_000_000 -> String.format("%.1fM", value / 1_000_000)
        value >= 1_000 -> String.format("%.1fK", value / 1_000)
        else -> if (value == value.toLong().toFloat()) {
            value.toLong().toString()
        } else {
            String.format("%.1f", value)
        }
    }
}

/**
 * 绘制数值标签
 *
 * @param textMeasurer 文本测量器
 * @param value 要显示的数值
 * @param x 标签中心 X 坐标
 * @param y 标签中心 Y 坐标
 * @param textColor 文字颜色
 * @param textSize 文字大小（sp）
 * @param formatter 可选的数值格式化器
 * @param offsetX X 轴偏移量
 * @param offsetY Y 轴偏移量
 */
fun androidx.compose.ui.graphics.drawscope.DrawScope.drawValueLabel(
    textMeasurer: TextMeasurer,
    value: Float,
    x: Float,
    y: Float,
    textColor: Color = Color.Black,
    textSize: Float = 10f,
    formatter: ((Float) -> String)? = null,
    offsetX: Float = 0f,
    offsetY: Float = 0f
) {
    val text = formatter?.invoke(value) ?: DefaultValueFormatter(value)

    val textLayoutResult = textMeasurer.measure(
        text = text,
        style = TextStyle(
            color = textColor,
            fontSize = textSize.sp
        )
    )

    val textWidth = textLayoutResult.size.width
    val textHeight = textLayoutResult.size.height

    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = Offset(
            x - textWidth / 2 + offsetX,
            y - textHeight / 2 + offsetY
        )
    )
}

/**
 * 绘制数值标签（带背景）
 *
 * @param textMeasurer 文本测量器
 * @param value 要显示的数值
 * @param x 标签中心 X 坐标
 * @param y 标签中心 Y 坐标
 * @param textColor 文字颜色
 * @param textSize 文字大小（sp）
 * @param backgroundColor 背景颜色
 * @param backgroundRadius 背景圆角半径
 * @param padding 内边距
 * @param formatter 可选的数值格式化器
 */
fun androidx.compose.ui.graphics.drawscope.DrawScope.drawValueLabelWithBackground(
    textMeasurer: TextMeasurer,
    value: Float,
    x: Float,
    y: Float,
    textColor: Color = Color.White,
    textSize: Float = 10f,
    backgroundColor: Color = Color.Gray,
    backgroundRadius: Float = 4f,
    padding: Float = 4f,
    formatter: ((Float) -> String)? = null
) {
    val text = formatter?.invoke(value) ?: DefaultValueFormatter(value)

    val textLayoutResult = textMeasurer.measure(
        text = text,
        style = TextStyle(
            color = textColor,
            fontSize = textSize.sp
        )
    )

    val textWidth = textLayoutResult.size.width
    val textHeight = textLayoutResult.size.height

    // 绘制背景
    drawRoundRect(
        color = backgroundColor,
        topLeft = Offset(
            x - textWidth / 2 - padding,
            y - textHeight / 2 - padding
        ),
        size = androidx.compose.ui.geometry.Size(
            textWidth + padding * 2,
            textHeight + padding * 2
        ),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(backgroundRadius, backgroundRadius)
    )

    // 绘制文字
    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = Offset(
            x - textWidth / 2,
            y - textHeight / 2
        )
    )
}

/**
 * 绘制旋转的数值标签
 *
 * @param textMeasurer 文本测量器
 * @param value 要显示的数值
 * @param x 标签中心 X 坐标
 * @param y 标签中心 Y 坐标
 * @param rotationAngle 旋转角度（度）
 * @param textColor 文字颜色
 * @param textSize 文字大小（sp）
 * @param formatter 可选的数值格式化器
 */
fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRotatedValueLabel(
    textMeasurer: TextMeasurer,
    value: Float,
    x: Float,
    y: Float,
    rotationAngle: Float,
    textColor: Color = Color.Black,
    textSize: Float = 10f,
    formatter: ((Float) -> String)? = null
) {
    val text = formatter?.invoke(value) ?: DefaultValueFormatter(value)

    val textLayoutResult = textMeasurer.measure(
        text = text,
        style = TextStyle(
            color = textColor,
            fontSize = textSize.sp
        )
    )

    val textWidth = textLayoutResult.size.width
    val textHeight = textLayoutResult.size.height

    // 使用 rotate 变换绘制旋转文字
    rotate(rotationAngle, pivot = Offset(x, y)) {
        drawText(
            textLayoutResult = textLayoutResult,
            topLeft = Offset(
                x - textWidth / 2,
                y - textHeight / 2
            )
        )
    }
}

/**
 * 绘制多行数值标签
 *
 * @param textMeasurer 文本测量器
 * @param lines 多行文本
 * @param x 标签中心 X 坐标
 * @param y 标签中心 Y 坐标
 * @param textColor 文字颜色
 * @param textSize 文字大小（sp）
 * @param lineSpacing 行间距
 */
fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMultiLineValueLabel(
    textMeasurer: TextMeasurer,
    lines: List<String>,
    x: Float,
    y: Float,
    textColor: Color = Color.Black,
    textSize: Float = 10f,
    lineSpacing: Float = 2f
) {
    val lineHeight = textSize * 1.2f
    val totalHeight = lines.size * lineHeight + (lines.size - 1) * lineSpacing
    var currentY = y - totalHeight / 2

    for (line in lines) {
        val textLayoutResult = textMeasurer.measure(
            text = line,
            style = TextStyle(
                color = textColor,
                fontSize = textSize.sp
            )
        )

        drawText(
            textLayoutResult = textLayoutResult,
            topLeft = Offset(
                x - textLayoutResult.size.width / 2,
                currentY
            )
        )

        currentY += lineHeight + lineSpacing
    }
}
