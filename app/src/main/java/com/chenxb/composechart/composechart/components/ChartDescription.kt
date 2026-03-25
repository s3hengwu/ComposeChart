package com.chenxb.composechart.composechart.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 描述文字位置
 */
enum class DescriptionPosition {
    /** 左上角 */
    TOP_LEFT,
    /** 上方居中 */
    TOP_CENTER,
    /** 右上角 */
    TOP_RIGHT,
    /** 左下角 */
    BOTTOM_LEFT,
    /** 下方居中 */
    BOTTOM_CENTER,
    /** 右下角（默认） */
    BOTTOM_RIGHT,
    /** 左侧居中 */
    CENTER_LEFT,
    /** 正中心 */
    CENTER,
    /** 右侧居中 */
    CENTER_RIGHT
}

/**
 * 图表描述文字配置
 */
data class ChartDescription(
    /** 描述文字内容 */
    val text: String = "",

    /** 文字颜色 */
    val textColor: Color = Color.Gray,

    /** 文字大小（sp） */
    val textSize: Float = 8f,

    /** 文字位置 */
    val position: DescriptionPosition = DescriptionPosition.BOTTOM_RIGHT,

    /** X轴偏移量（dp） */
    val offsetX: Float = 0f,

    /** Y轴偏移量（dp） */
    val offsetY: Float = 0f,

    /** 是否启用描述文字 */
    val enabled: Boolean = false,

    /** 文字字重 */
    val fontWeight: FontWeight = FontWeight.Normal,

    /** 内边距（dp） */
    val padding: Float = 8f
) {
    companion object {
        /** 默认配置 - 禁用 */
        val Default = ChartDescription()

        /** 右下角描述 */
        fun bottomRight(text: String, textColor: Color = Color.Gray, textSize: Float = 8f) = ChartDescription(
            text = text,
            textColor = textColor,
            textSize = textSize,
            position = DescriptionPosition.BOTTOM_RIGHT,
            enabled = true
        )

        /** 左下角描述 */
        fun bottomLeft(text: String, textColor: Color = Color.Gray, textSize: Float = 8f) = ChartDescription(
            text = text,
            textColor = textColor,
            textSize = textSize,
            position = DescriptionPosition.BOTTOM_LEFT,
            enabled = true
        )

        /** 居中描述 */
        fun center(text: String, textColor: Color = Color.Gray, textSize: Float = 10f) = ChartDescription(
            text = text,
            textColor = textColor,
            textSize = textSize,
            position = DescriptionPosition.CENTER,
            enabled = true
        )

        /** 底部居中描述 */
        fun bottomCenter(text: String, textColor: Color = Color.Gray, textSize: Float = 8f) = ChartDescription(
            text = text,
            textColor = textColor,
            textSize = textSize,
            position = DescriptionPosition.BOTTOM_CENTER,
            enabled = true
        )
    }
}

/**
 * 描述文字视图
 */
@Composable
fun DescriptionView(
    description: ChartDescription,
    modifier: Modifier = Modifier
) {
    if (!description.enabled || description.text.isEmpty()) return

    val alignment = when (description.position) {
        DescriptionPosition.TOP_LEFT -> Alignment.TopStart
        DescriptionPosition.TOP_CENTER -> Alignment.TopCenter
        DescriptionPosition.TOP_RIGHT -> Alignment.TopEnd
        DescriptionPosition.BOTTOM_LEFT -> Alignment.BottomStart
        DescriptionPosition.BOTTOM_CENTER -> Alignment.BottomCenter
        DescriptionPosition.BOTTOM_RIGHT -> Alignment.BottomEnd
        DescriptionPosition.CENTER_LEFT -> Alignment.CenterStart
        DescriptionPosition.CENTER -> Alignment.Center
        DescriptionPosition.CENTER_RIGHT -> Alignment.CenterEnd
    }

    val textAlign = when (description.position) {
        DescriptionPosition.TOP_LEFT, DescriptionPosition.CENTER_LEFT, DescriptionPosition.BOTTOM_LEFT -> TextAlign.Start
        DescriptionPosition.TOP_CENTER, DescriptionPosition.CENTER, DescriptionPosition.BOTTOM_CENTER -> TextAlign.Center
        DescriptionPosition.TOP_RIGHT, DescriptionPosition.CENTER_RIGHT, DescriptionPosition.BOTTOM_RIGHT -> TextAlign.End
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(description.padding.dp),
        contentAlignment = alignment
    ) {
        Text(
            text = description.text,
            color = description.textColor,
            fontSize = description.textSize.sp,
            fontWeight = description.fontWeight,
            textAlign = textAlign,
            modifier = Modifier
                .offset(description.offsetX.dp, description.offsetY.dp)
        )
    }
}
