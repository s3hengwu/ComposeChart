package com.chenxb.composechart.composechart.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 空数据配置
 */
data class EmptyDataConfig(
    /** 提示文字 */
    val text: String = "No chart data available",

    /** 文字颜色 */
    val textColor: Color = Color.Gray,

    /** 文字大小 */
    val textSize: Float = 14f,

    /** 图标（可选） */
    val icon: ImageVector? = null,

    /** 图标颜色 */
    val iconColor: Color = Color.Gray.copy(alpha = 0.5f),

    /** 图标大小 */
    val iconSize: Float = 48f,

    /** 是否启用空数据提示 */
    val enabled: Boolean = true
) {
    companion object {
        /** 默认配置 */
        val Default = EmptyDataConfig()

        /** 简单配置 - 只有文字 */
        val Simple = EmptyDataConfig(
            icon = null
        )

        /** 中文配置 */
        val Chinese = EmptyDataConfig(
            text = "暂无数据"
        )
    }
}

/**
 * 空数据视图
 * 当图表数据为空时显示此视图
 */
@Composable
fun EmptyDataView(
    config: EmptyDataConfig = EmptyDataConfig.Default,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 图标（如果配置了）
            config.icon?.let { icon ->
                // 使用 Icon 组件需要 material-icons-extended 依赖
                // 这里简化处理，不显示图标
            }

            if (config.icon != null) {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 提示文字
            Text(
                text = config.text,
                color = config.textColor,
                fontSize = config.textSize.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 检查数据是否为空
 */
fun <T> List<T>?.isEmptyData(): Boolean {
    return this == null || this.isEmpty()
}

/**
 * 检查嵌套列表数据是否为空
 */
fun <T> List<List<T>>?.isEmptyChartData(): Boolean {
    if (this == null || this.isEmpty()) return true
    return this.all { it.isEmpty() }
}
