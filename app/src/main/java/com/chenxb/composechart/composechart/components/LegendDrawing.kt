package com.chenxb.composechart.composechart.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 图例绘制组件
 * @param modifier 传入的 modifier（在 LineChart 中已用 padding 精确定位）
 */
@Composable
fun LegendView(
    legendStyle: LegendStyle,
    legendEntries: List<LegendEntry>,
    modifier: Modifier = Modifier
) {
    if (!legendStyle.isEnabled || legendEntries.isEmpty()) return

    // 外层 Box - 设置对齐方式
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = when {
            legendStyle.verticalAlignment == LegendVerticalAlignment.TOP &&
                    legendStyle.horizontalAlignment == LegendHorizontalAlignment.LEFT -> Alignment.TopStart
            legendStyle.verticalAlignment == LegendVerticalAlignment.TOP &&
                    legendStyle.horizontalAlignment == LegendHorizontalAlignment.CENTER -> Alignment.TopCenter
            legendStyle.verticalAlignment == LegendVerticalAlignment.TOP &&
                    legendStyle.horizontalAlignment == LegendHorizontalAlignment.RIGHT -> Alignment.TopEnd
            legendStyle.verticalAlignment == LegendVerticalAlignment.CENTER &&
                    legendStyle.horizontalAlignment == LegendHorizontalAlignment.LEFT -> Alignment.CenterStart
            legendStyle.verticalAlignment == LegendVerticalAlignment.CENTER &&
                    legendStyle.horizontalAlignment == LegendHorizontalAlignment.CENTER -> Alignment.Center
            legendStyle.verticalAlignment == LegendVerticalAlignment.CENTER &&
                    legendStyle.horizontalAlignment == LegendHorizontalAlignment.RIGHT -> Alignment.CenterEnd
            legendStyle.verticalAlignment == LegendVerticalAlignment.BOTTOM &&
                    legendStyle.horizontalAlignment == LegendHorizontalAlignment.LEFT -> Alignment.BottomStart
            legendStyle.verticalAlignment == LegendVerticalAlignment.BOTTOM &&
                    legendStyle.horizontalAlignment == LegendHorizontalAlignment.CENTER -> Alignment.BottomCenter
            legendStyle.verticalAlignment == LegendVerticalAlignment.BOTTOM &&
                    legendStyle.horizontalAlignment == LegendHorizontalAlignment.RIGHT -> Alignment.BottomEnd
            else -> Alignment.Center
        }
    ) {
        Box(modifier = Modifier.padding(8.dp)) {
            when (legendStyle.orientation) {
                LegendOrientation.HORIZONTAL -> {
                    HorizontalLegend(
                        legendStyle = legendStyle,
                        legendEntries = legendEntries
                    )
                }
                LegendOrientation.VERTICAL -> {
                    VerticalLegend(
                        legendStyle = legendStyle,
                        legendEntries = legendEntries
                    )
                }
            }
        }
    }
}

@Composable
private fun HorizontalLegend(
    legendStyle: LegendStyle,
    legendEntries: List<LegendEntry>
) {
    val totalWidth = legendEntries.sumOf {
        (legendStyle.formSize + legendStyle.xEntrySpace.value * 4 + it.label.length * legendStyle.textSize * 0.6f).toInt()
    }.dp

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color.White.copy(alpha = 0.9f))
            .padding(8.dp)
    ) {
        androidx.compose.foundation.layout.Row(
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (entry in legendEntries) {
                LegendEntryItem(
                    entry = entry,
                    style = legendStyle
                )
                if (entry != legendEntries.last()) {
                    androidx.compose.foundation.layout.Spacer(
                        modifier = Modifier.width(legendStyle.xEntrySpace)
                    )
                }
            }
        }
    }
}

@Composable
private fun VerticalLegend(
    legendStyle: LegendStyle,
    legendEntries: List<LegendEntry>
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color.White.copy(alpha = 0.9f))
            .padding(8.dp)
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Top
        ) {
            for (entry in legendEntries) {
                androidx.compose.foundation.layout.Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendEntryItem(
                        entry = entry,
                        style = legendStyle
                    )
                }
                if (entry != legendEntries.last()) {
                    androidx.compose.foundation.layout.Spacer(
                        modifier = Modifier.height(legendStyle.yEntrySpace)
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendEntryItem(
    entry: LegendEntry,
    style: LegendStyle
) {
    androidx.compose.foundation.layout.Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 绘制图例形状
        Canvas(
            modifier = Modifier.size(style.formSize.dp)
        ) {
            drawLegendForm(
                form = entry.form,
                color = entry.color,
                size = style.formSize
            )
        }

        // 标签
        Text(
            text = entry.label,
            style = TextStyle(
                fontSize = style.textSize.sp,
                fontWeight = style.textFontWeight,
                color = style.textColor
            ),
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

/**
 * 绘制图例形状
 * 使用 DrawScope 的实际像素尺寸 (this.size) 进行绘制，
 * 而不是外部传入的 dp 值，确保坐标计算正确。
 */
private fun DrawScope.drawLegendForm(
    form: LegendForm,
    color: Color,
    @Suppress("UNUSED_PARAMETER") size: Float
) {
    val w = this.size.width   // 实际渲染宽度（px）
    val h = this.size.height  // 实际渲染高度（px）

    when (form) {
        LegendForm.SQUARE -> {
            drawRect(
                color = color,
                size = this.size
            )
        }
        LegendForm.CIRCLE -> {
            drawCircle(
                color = color,
                radius = minOf(w, h) / 2,
                center = Offset(w / 2, h / 2)
            )
        }
        LegendForm.LINE -> {
            drawLine(
                color = color,
                start = Offset(0f, h / 2),
                end = Offset(w, h / 2),
                strokeWidth = 2f
            )
        }
        LegendForm.EMPTY -> {
            // 不绘制任何形状
        }
    }
}

/**
 * 简单图例
 */
@Composable
fun SimpleLegend(
    labels: List<String>,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    val entries = labels.zip(colors).map { (label, color) ->
        LegendEntry(
            label = label,
            color = color,
            form = LegendForm.SQUARE
        )
    }

    LegendView(
        legendStyle = LegendStyle(),
        legendEntries = entries,
        modifier = modifier
    )
}
