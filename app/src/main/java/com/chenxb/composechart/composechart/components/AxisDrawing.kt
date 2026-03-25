package com.chenxb.composechart.composechart.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.toArgb

/**
 * X轴绘制组件
 */
@Composable
fun XAxisView(
    config: XAxisConfig,
    bounds: com.chenxb.composechart.composechart.ChartBounds,
    xRangeMin: Float,
    xRangeMax: Float,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier.fillMaxSize()) {
        if (!config.isEnabled) return@Canvas

        val chartWidth = bounds.width
        val chartHeight = bounds.height
        val y = bounds.bottom

        // 绘制轴线
        if (config.isDrawAxisLineEnabled) {
            drawLine(
                color = config.axisLineColor,
                start = Offset(bounds.left, y),
                end = Offset(bounds.right, y),
                strokeWidth = config.axisLineWidth
            )
        }

        // 计算标签数量和步长
        val range = xRangeMax - xRangeMin

        if (config.centerLabels) {
            // 分组模式：标签居中显示在每组中心
            val groupCount = (xRangeMax - xRangeMin + 1).toInt()
            val groupWidth = chartWidth / groupCount

            // 绘制网格线
            if (config.isDrawGridLinesEnabled && config.gridStyle.isDrawGridLinesEnabled) {
                for (i in 0..groupCount) {
                    val x = bounds.left + i * groupWidth
                    drawLine(
                        color = config.gridStyle.gridColor,
                        start = Offset(x, bounds.top),
                        end = Offset(x, y),
                        strokeWidth = config.gridStyle.gridLineWidth,
                        pathEffect = config.gridStyle.gridLineDashPathEffect
                    )
                }
            }

            // 绘制标签
            if (config.isDrawLabelsEnabled) {
                for (i in 0 until groupCount) {
                    val value = xRangeMin + i
                    // 标签居中在组内
                    val x = bounds.left + i * groupWidth + groupWidth / 2

                    val labelText = config.valueFormatter?.invoke(value) ?: value.toInt().toString()

                    val textLayoutResult = textMeasurer.measure(
                        text = labelText,
                        style = TextStyle(
                            fontSize = config.labelTextSize.sp,
                            color = config.labelTextColor,
                            fontFamily = config.labelFontFamily,
                            fontWeight = config.labelFontWeight,
                            fontStyle = config.labelFontStyle
                        )
                    )

                    // 应用标签旋转
                    if (config.labelRotationAngle != 0f) {
                        rotate(config.labelRotationAngle, pivot = Offset(x, y + 4f + textLayoutResult.size.height / 2)) {
                            drawText(
                                textLayoutResult = textLayoutResult,
                                topLeft = Offset(
                                    x - textLayoutResult.size.width / 2,
                                    y + 4f
                                )
                            )
                        }
                    } else {
                        drawText(
                            textLayoutResult = textLayoutResult,
                            topLeft = Offset(
                                x - textLayoutResult.size.width / 2,
                                y + 4f
                            )
                        )
                    }
                }
            }
        } else {
            // 连续模式：原有逻辑
            var labelCount = config.labelCount
            var step = range / (labelCount - 1)

            // 确保步长不小于粒度
            if (step < config.granularity && config.granularity > 0) {
                step = config.granularity
                labelCount = (range / step).toInt() + 1
            }

            // 绘制网格线
            if (config.isDrawGridLinesEnabled && config.gridStyle.isDrawGridLinesEnabled) {
                for (i in 0 until labelCount) {
                    val value = xRangeMin + step * i
                    if (value > xRangeMax) break
                    val x = bounds.left + (value - xRangeMin) / range * chartWidth

                    drawLine(
                        color = config.gridStyle.gridColor,
                        start = Offset(x, bounds.top),
                        end = Offset(x, y),
                        strokeWidth = config.gridStyle.gridLineWidth,
                        pathEffect = config.gridStyle.gridLineDashPathEffect
                    )
                }
            }

            // 绘制标签（支持旋转）
            if (config.isDrawLabelsEnabled) {
                for (i in 0 until labelCount) {
                    val value = xRangeMin + step * i
                    if (value > xRangeMax) break
                    val x = bounds.left + (value - xRangeMin) / range * chartWidth

                    val labelText = config.valueFormatter?.invoke(value) ?: value.toInt().toString()

                    val textLayoutResult = textMeasurer.measure(
                        text = labelText,
                        style = TextStyle(
                            fontSize = config.labelTextSize.sp,
                            color = config.labelTextColor,
                            fontFamily = config.labelFontFamily,
                            fontWeight = config.labelFontWeight,
                            fontStyle = config.labelFontStyle
                        )
                    )

                    // 应用标签旋转
                    if (config.labelRotationAngle != 0f) {
                        rotate(config.labelRotationAngle, pivot = Offset(x, y + 4f + textLayoutResult.size.height / 2)) {
                            drawText(
                                textLayoutResult = textLayoutResult,
                                topLeft = Offset(
                                    x - textLayoutResult.size.width / 2,
                                    y + 4f
                                )
                            )
                        }
                    } else {
                        drawText(
                            textLayoutResult = textLayoutResult,
                            topLeft = Offset(
                                x - textLayoutResult.size.width / 2,
                                y + 4f
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Y轴绘制组件
 */
@Composable
fun YAxisView(
    config: YAxisConfig,
    bounds: com.chenxb.composechart.composechart.ChartBounds,
    yRangeMin: Float,
    yRangeMax: Float,
    modifier: Modifier = Modifier,
    isLeft: Boolean = true,
    limitLines: LimitLines = LimitLines()
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier.fillMaxSize()) {
        if (!config.isEnabled) return@Canvas

        val chartWidth = bounds.width
        val chartHeight = bounds.height
        val x = if (isLeft) bounds.left else bounds.right

        // 绘制轴线
        if (config.isDrawAxisLineEnabled) {
            drawLine(
                color = config.axisLineColor,
                start = Offset(x, bounds.top),
                end = Offset(x, bounds.bottom),
                strokeWidth = config.axisLineWidth
            )
        }

        // 计算标签数量和步长（考虑粒度）
        val range = yRangeMax - yRangeMin
        var labelCount = config.labelCount
        var step = range / (labelCount - 1)

        // 确保步长不小于粒度
        if (step < config.granularity && config.granularity > 0) {
            step = config.granularity
            labelCount = (range / step).toInt() + 1
        }

        // 绘制网格线
        if (config.isDrawGridLinesEnabled && config.gridStyle.isDrawGridLinesEnabled) {
            for (i in 0 until labelCount) {
                val value = yRangeMin + step * i
                if (value > yRangeMax) break

                // 考虑反转
                val y = if (config.isInverted) {
                    bounds.top + (value - yRangeMin) / range * chartHeight
                } else {
                    bounds.bottom - (value - yRangeMin) / range * chartHeight
                }

                drawLine(
                    color = config.gridStyle.gridColor,
                    start = Offset(bounds.left, y),
                    end = Offset(bounds.right, y),
                    strokeWidth = config.gridStyle.gridLineWidth,
                    pathEffect = config.gridStyle.gridLineDashPathEffect
                )
            }
        }

        // 绘制标签
        if (config.isDrawLabelsEnabled) {
            for (i in 0 until labelCount) {
                val value = yRangeMin + step * i
                if (value > yRangeMax) break

                // 考虑反转
                val y = if (config.isInverted) {
                    bounds.top + (value - yRangeMin) / range * chartHeight
                } else {
                    bounds.bottom - (value - yRangeMin) / range * chartHeight
                }

                val labelText = config.valueFormatter?.invoke(value) ?: value.toInt().toString()

                val textLayoutResult = textMeasurer.measure(
                    text = labelText,
                    style = TextStyle(
                        fontSize = config.labelTextSize.sp,
                        color = config.labelTextColor,
                        fontFamily = config.labelFontFamily,
                        fontWeight = config.labelFontWeight,
                        fontStyle = config.labelFontStyle
                    )
                )

                val textX = if (isLeft) {
                    x - textLayoutResult.size.width - 4f
                } else {
                    x + 4f
                }

                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(textX, y - textLayoutResult.size.height / 2)
                )
            }
        }

        // 绘制限制线
        if (limitLines.getLimitLines().isNotEmpty()) {
            drawLimitLines(
                limitLines = limitLines,
                bounds = bounds,
                yRangeMin = yRangeMin,
                yRangeMax = yRangeMax,
                isInverted = config.isInverted,
                textMeasurer = textMeasurer
            )
        }
    }
}

/**
 * 限制线绘制
 */
fun DrawScope.drawLimitLines(
    limitLines: LimitLines,
    bounds: com.chenxb.composechart.composechart.ChartBounds,
    yRangeMin: Float,
    yRangeMax: Float,
    isInverted: Boolean = false,
    textMeasurer: androidx.compose.ui.text.TextMeasurer? = null
) {
    val chartHeight = bounds.height

    for (limitLine in limitLines.getLimitLines()) {
        val y = if (isInverted) {
            bounds.bottom - (limitLine.limit - yRangeMin) / (yRangeMax - yRangeMin) * chartHeight
        } else {
            bounds.bottom - (limitLine.limit - yRangeMin) / (yRangeMax - yRangeMin) * chartHeight
        }

        // 绘制线
        drawLine(
            color = limitLine.lineColor,
            start = Offset(bounds.left, y),
            end = Offset(bounds.right, y),
            strokeWidth = limitLine.lineWidth,
            pathEffect = limitLine.lineDashPathEffect
        )

        // 绘制标签
        if (limitLine.label.isNotEmpty() && textMeasurer != null) {
            val textLayoutResult = textMeasurer.measure(
                text = limitLine.label,
                style = androidx.compose.ui.text.TextStyle(
                    fontSize = limitLine.labelTextSize.sp,
                    color = limitLine.labelColor
                )
            )

            val labelX = when (limitLine.labelPosition) {
                LimitLabelPosition.LEFT_TOP, LimitLabelPosition.LEFT_BOTTOM -> bounds.left + 4f
                LimitLabelPosition.RIGHT_TOP, LimitLabelPosition.RIGHT_BOTTOM -> bounds.right - textLayoutResult.size.width - 4f
            }
            val labelY = when (limitLine.labelPosition) {
                LimitLabelPosition.LEFT_TOP, LimitLabelPosition.RIGHT_TOP -> y - textLayoutResult.size.height - 4f
                LimitLabelPosition.LEFT_BOTTOM, LimitLabelPosition.RIGHT_BOTTOM -> y + 4f
            }

            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(labelX, labelY)
            )
        }
    }
}
