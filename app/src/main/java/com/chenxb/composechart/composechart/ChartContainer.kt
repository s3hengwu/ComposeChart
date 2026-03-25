package com.chenxb.composechart.composechart

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.chenxb.composechart.composechart.components.Highlight
import com.chenxb.composechart.composechart.gestures.ChartGestureState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * 图表绘制边界
 */
data class ChartBounds(
    val left: Float = 0f,
    val top: Float = 0f,
    val right: Float = 0f,
    val bottom: Float = 0f
) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top
    val centerX: Float get() = left + width / 2
    val centerY: Float get() = top + height / 2
    val center: Offset get() = Offset(centerX, centerY)
}

/**
 * 图表内容绘制接口
 */
interface ChartDrawContent {
    fun draw(context: DrawScope, bounds: ChartBounds, animationProgress: Float)
}

/**
 * 图表容器组件
 */
@Composable
fun ChartContainer(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    padding: Dp = 16.dp,
    animationProgress: Float = 1f,
    gestureState: ChartGestureState = ChartGestureState(),
    onChartSizeChanged: (Float, Float) -> Unit = { _, _ -> },
    onTap: (Offset) -> Unit = {},
    onDoubleTap: (Offset) -> Unit = {},
    content: @Composable (bounds: ChartBounds) -> Unit
) {
    var chartSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current
    val paddingPx = with(density) { padding.toPx() }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .onSizeChanged { size ->
                chartSize = size
                onChartSizeChanged(
                    size.width - paddingPx * 2,
                    size.height - paddingPx * 2
                )
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset -> onTap(offset) },
                    onDoubleTap = { offset -> onDoubleTap(offset) }
                )
            }
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    // 处理手势
                }
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { _ -> },
                    onDrag = { change, dragAmount ->
                        change.consume()
                    },
                    onDragEnd = { }
                )
            }
    ) {
        val bounds = ChartBounds(
            left = paddingPx,
            top = paddingPx,
            right = chartSize.width.toFloat() - paddingPx,
            bottom = chartSize.height.toFloat() - paddingPx
        )

        content(bounds)
    }
}

/**
 * 图表画布组件
 */
@Composable
fun ChartCanvas(
    modifier: Modifier = Modifier,
    bounds: ChartBounds,
    animationProgress: Float = 1f,
    gestureState: ChartGestureState = ChartGestureState(),
    drawContent: DrawScope.() -> Unit
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
    ) {
        // 应用变换
        translate(
            left = gestureState.translationX,
            top = gestureState.translationY
        ) {
            scale(
                scaleX = gestureState.scaleX,
                scaleY = gestureState.scaleY,
                pivot = Offset(bounds.width / 2, bounds.height / 2)
            ) {
                drawContent()
            }
        }
    }
}

/**
 * 高亮指示器绘制
 */
fun DrawScope.drawHighlight(
    highlight: Highlight,
    bounds: ChartBounds,
    color: Color = Color.Gray.copy(alpha = 0.3f),
    lineColor: Color = Color.Gray,
    lineWidth: Float = 1f
) {
    val x = highlight.x
    val y = highlight.y

    // 绘制背景高亮
    drawRect(
        color = color,
        topLeft = Offset(x - 20, bounds.top),
        size = Size(40f, bounds.height)
    )
    drawRect(
        color = color,
        topLeft = Offset(bounds.left, y - 20),
        size = Size(bounds.width, 40f)
    )

    // 绘制交叉线
    drawLine(
        color = lineColor,
        start = Offset(x, bounds.top),
        end = Offset(x, bounds.bottom),
        strokeWidth = lineWidth
    )
    drawLine(
        color = lineColor,
        start = Offset(bounds.left, y),
        end = Offset(bounds.right, y),
        strokeWidth = lineWidth
    )

    // 绘制中心点
    drawCircle(
        color = lineColor,
        radius = 5f,
        center = Offset(x, y),
        style = Stroke(width = 2f)
    )
}

/**
 * 图表保存工具类
 * 提供toBitmap()和toFile()功能
 */
object ChartSaver {
    /**
     * 将Bitmap保存到文件
     * @param bitmap 要保存的位图
     * @param file 目标文件
     * @param format 图片格式，默认PNG
     * @param quality 质量（0-100），对PNG无效
     */
    suspend fun saveBitmapToFile(
        bitmap: Bitmap,
        file: File,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
        quality: Int = 100
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(format, quality, out)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 创建空白位图
     * @param width 宽度
     * @param height 高度
     * @return 空白位图
     */
    fun createBitmap(width: Int, height: Int): Bitmap {
        return Bitmap.createBitmap(
            width.coerceAtLeast(1),
            height.coerceAtLeast(1),
            Bitmap.Config.ARGB_8888
        )
    }
}
