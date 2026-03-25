package com.chenxb.composechart.composechart.gestures

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.Modifier
import kotlin.math.roundToInt

/**
 * 图表手势监听器接口
 * 对应 MPAndroidChart 的 OnChartGestureListener
 */
interface OnChartGestureListener {
    fun onChartGestureStart() {}
    fun onChartGestureEnd() {}
    fun onChartLongPress(me: Offset) {}
    fun onChartDoubleTap(me: Offset) {}
    fun onChartSingleTap(me: Offset) {}
    fun onChartFling(me1: Offset, me2: Offset, velocityX: Float, velocityY: Float) {}
    fun onChartScale(scaleFactor: Float, focalPoint: Offset) {}
    fun onChartTranslate(dx: Float, dy: Float) {}
}

/**
 * 图表手势状态
 */
data class ChartGestureState(
    val isDragging: Boolean = false,
    val isScaling: Boolean = false,
    val isPinchZooming: Boolean = false,
    val translationX: Float = 0f,
    val translationY: Float = 0f,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f,
    val selectedOffset: Offset? = null,
    val maxScaleX: Float = 5f,
    val minScaleX: Float = 0.5f,
    val maxScaleY: Float = 5f,
    val minScaleY: Float = 0.5f,
    val maxVisibleXRange: Float? = null,
    val maxVisibleYRange: Float? = null
)

/**
 * 手势配置
 */
data class GestureConfig(
    val isDragEnabled: Boolean = true,
    val isScaleEnabled: Boolean = true,
    val isPinchZoomEnabled: Boolean = true,
    val isDoubleTapEnabled: Boolean = true,
    val isLongPressEnabled: Boolean = true,
    val isHighlightEnabled: Boolean = true,
    val isFlingEnabled: Boolean = true,
    val touchHitSlop: Float = 50f  // 可点击区域配置
)

/**
 * 图表手势处理器
 */
class ChartGesturesProcessor(
    private val config: GestureConfig = GestureConfig(),
    private val onGestureStateChanged: (ChartGestureState) -> Unit = {},
    private val onValueSelected: (Offset) -> Unit = {},
    private val onValueDeselected: () -> Unit = {},
    private val gestureListener: OnChartGestureListener? = null
) {
    private var gestureState = ChartGestureState()
    private var lastFocalPoint = Offset.Zero

    fun onDragStart(offset: Offset) {
        if (!config.isDragEnabled) return
        gestureState = gestureState.copy(isDragging = true)
        gestureListener?.onChartGestureStart()
        onGestureStateChanged(gestureState)
    }

    fun onDrag(offset: Offset, dragAmount: Offset) {
        if (!config.isDragEnabled || !gestureState.isDragging) return
        gestureState = gestureState.copy(
            translationX = gestureState.translationX + dragAmount.x,
            translationY = gestureState.translationY + dragAmount.y
        )
        gestureListener?.onChartTranslate(dragAmount.x, dragAmount.y)
        onGestureStateChanged(gestureState)
    }

    fun onDragEnd() {
        gestureState = gestureState.copy(isDragging = false)
        gestureListener?.onChartGestureEnd()
        onGestureStateChanged(gestureState)
    }

    fun onScaleStart(focalPoint: Offset) {
        lastFocalPoint = focalPoint
        gestureListener?.onChartGestureStart()
    }

    fun onScale(scaleFactor: Float, focalPoint: Offset) {
        if (!config.isScaleEnabled) return

        val newScaleX = (gestureState.scaleX * scaleFactor).coerceIn(gestureState.minScaleX, gestureState.maxScaleX)
        val newScaleY = (gestureState.scaleY * scaleFactor).coerceIn(gestureState.minScaleY, gestureState.maxScaleY)

        gestureState = gestureState.copy(
            isScaling = true,
            isPinchZooming = config.isPinchZoomEnabled,
            scaleX = if (config.isPinchZoomEnabled) newScaleX else gestureState.scaleX,
            scaleY = if (config.isPinchZoomEnabled) newScaleY else gestureState.scaleY,
            translationX = gestureState.translationX + (focalPoint.x - lastFocalPoint.x),
            translationY = gestureState.translationY + (focalPoint.y - lastFocalPoint.y)
        )
        lastFocalPoint = focalPoint
        gestureListener?.onChartScale(scaleFactor, focalPoint)
        onGestureStateChanged(gestureState)
    }

    fun onScaleEnd() {
        gestureState = gestureState.copy(isScaling = false, isPinchZooming = false)
        gestureListener?.onChartGestureEnd()
        onGestureStateChanged(gestureState)
    }

    fun onTap(offset: Offset) {
        if (!config.isHighlightEnabled) return
        gestureState = gestureState.copy(selectedOffset = offset)
        gestureListener?.onChartSingleTap(offset)
        onValueSelected(offset)
        onGestureStateChanged(gestureState)
    }

    fun onDoubleTap(offset: Offset) {
        if (!config.isDoubleTapEnabled) return
        gestureState = gestureState.copy(
            scaleX = 1f,
            scaleY = 1f,
            translationX = 0f,
            translationY = 0f
        )
        gestureListener?.onChartDoubleTap(offset)
        onGestureStateChanged(gestureState)
    }

    fun onLongPress(offset: Offset) {
        if (!config.isLongPressEnabled) return
        gestureState = gestureState.copy(selectedOffset = offset)
        gestureListener?.onChartLongPress(offset)
        onValueSelected(offset)
        onGestureStateChanged(gestureState)
    }

    fun reset() {
        gestureState = ChartGestureState()
        onGestureStateChanged(gestureState)
    }

    fun setVisibleXRangeMaximum(max: Float) {
        gestureState = gestureState.copy(maxVisibleXRange = max)
        onGestureStateChanged(gestureState)
    }

    fun setVisibleYRangeMaximum(max: Float) {
        gestureState = gestureState.copy(maxVisibleYRange = max)
        onGestureStateChanged(gestureState)
    }

    fun setMinMaxScale(minX: Float, maxX: Float, minY: Float, maxY: Float) {
        gestureState = gestureState.copy(
            minScaleX = minX,
            maxScaleX = maxX,
            minScaleY = minY,
            maxScaleY = maxY
        )
        onGestureStateChanged(gestureState)
    }

    fun getTouchHitSlop(): Float = config.touchHitSlop
}

/**
 * 选中状态管理
 */
data class SelectionState(
    val selectedEntry: com.chenxb.composechart.composechart.data.ChartEntry? = null,
    val selectedHighlight: com.chenxb.composechart.composechart.components.Highlight? = null,
    val selectedX: Float = 0f,
    val selectedY: Float = 0f
) {
    fun isSelected(): Boolean = selectedEntry != null

    fun clear() = SelectionState()
}
