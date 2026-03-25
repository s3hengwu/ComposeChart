package com.chenxb.composechart.composechart.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chenxb.composechart.composechart.data.ChartEntry

/**
 * 标记视图接口
 * 对应 MPAndroidChart 的 IMarker
 */
interface Marker {
    fun getOffset(): Offset
    fun getOffsetForDrawing(atPoint: Offset): Offset
    fun onMarkerSelected(entry: MarkerEntry, highlight: Highlight)
    fun onDraw(context: DrawScope, offset: Offset, entry: MarkerEntry)
}

/**
 * 标记条目
 */
data class MarkerEntry(
    val entry: ChartEntry,
    val highlight: Highlight,
    val displayText: String = ""
)

/**
 * 高亮信息
 * 对应 MPAndroidChart 的 Highlight
 */
data class Highlight(
    val x: Float,
    val y: Float,
    val xIndex: Int = 0,
    val dataSetIndex: Int = 0,
    val stackIndex: Int = -1,
    val axis: AxisDependency = AxisDependency.LEFT,
    val isStacked: Boolean = false
)

/**
 * 默认标记视图
 */
class DefaultMarkerView(
    private val backgroundColor: Color = Color(0xFF303030),
    private val textColor: Color = Color.White,
    private val textSize: Float = 12f,
    private val padding: Dp = 4.dp,
    private val cornerRadius: Dp = 4.dp
) : Marker {

    override fun getOffset(): Offset = Offset(0f, -100f)

    override fun getOffsetForDrawing(atPoint: Offset): Offset {
        return getOffset()
    }

    override fun onMarkerSelected(entry: MarkerEntry, highlight: Highlight) {
        // 默认实现不需要额外处理
    }

    override fun onDraw(context: DrawScope, offset: Offset, entry: MarkerEntry) {
        // 默认实现由 MarkerView composable 处理
    }
}

/**
 * MarkerView Composable
 * 用于在图表上显示标记视图
 */
@Composable
fun MarkerView(
    markerEntry: MarkerEntry?,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF303030),
    textColor: Color = Color.White,
    textSize: Float = 12f,
    padding: Dp = 8.dp,
    cornerRadius: Dp = 4.dp
) {
    if (markerEntry == null) return

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = markerEntry.displayText,
            color = textColor,
            fontSize = textSize.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

/**
 * 创建默认的MarkerEntry
 */
fun createDefaultMarkerEntry(
    entry: ChartEntry,
    highlight: Highlight,
    xValue: String = "",
    yValue: String = ""
): MarkerEntry {
    val displayText = buildString {
        if (xValue.isNotEmpty()) {
            append("X: $xValue")
        }
        if (yValue.isNotEmpty()) {
            if (isNotEmpty()) append("\n")
            append("Y: $yValue")
        }
    }.ifEmpty { "Value: ${entry.y}" }

    return MarkerEntry(
        entry = entry,
        highlight = highlight,
        displayText = displayText
    )
}
