package com.chenxb.composechart.composechart

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.chenxb.composechart.composechart.animation.ChartAnimationState
import com.chenxb.composechart.composechart.animation.ChartAnimation
import com.chenxb.composechart.composechart.components.AxisData
import com.chenxb.composechart.composechart.components.AxisDependency
import com.chenxb.composechart.composechart.gestures.ChartGestureState
import com.chenxb.composechart.composechart.components.Highlight
import com.chenxb.composechart.composechart.components.LegendStyle
import com.chenxb.composechart.composechart.components.LimitLines
import com.chenxb.composechart.composechart.data.ChartData
import com.chenxb.composechart.composechart.gestures.ChartGesturesProcessor
import com.chenxb.composechart.composechart.gestures.GestureConfig
import com.chenxb.composechart.composechart.gestures.SelectionState
import com.chenxb.composechart.composechart.style.ChartStyle

/**
 * 图表状态
 * 管理图表的所有状态：数据、样式、动画、选择等
 */
@Stable
class ChartState {
    // 数据相关
    internal var chartData: ChartData? by mutableStateOf(null)

    // 样式相关
    internal var chartStyle: ChartStyle by mutableStateOf(ChartStyle())
    internal var legendStyle: LegendStyle by mutableStateOf(LegendStyle())
    internal var axisData: AxisData by mutableStateOf(AxisData())
    internal var limitLines: LimitLines by mutableStateOf(LimitLines())

    // 手势状态
    internal var gestureState: ChartGestureState by mutableStateOf(ChartGestureState())
    internal var selectionState: SelectionState by mutableStateOf(SelectionState())

    // 动画状态
    internal val animationState = ChartAnimationState()

    // 可见范围
    internal var xRangeMin: Float by mutableStateOf(0f)
    internal var xRangeMax: Float by mutableStateOf(1f)
    internal var yRangeMin: Float by mutableStateOf(0f)
    internal var yRangeMax: Float by mutableStateOf(1f)

    // 图表尺寸
    internal var chartWidth: Float by mutableStateOf(0f)
    internal var chartHeight: Float by mutableStateOf(0f)

    // 内部处理器
    internal val gesturesProcessor: ChartGesturesProcessor by lazy {
        ChartGesturesProcessor(
            config = GestureConfig(
                isDragEnabled = chartStyle.isDragEnabled,
                isScaleEnabled = chartStyle.isScaleEnabled,
                isPinchZoomEnabled = chartStyle.isPinchZoomEnabled
            ),
            onGestureStateChanged = { gestureState = it },
            onValueSelected = { offset -> handleValueSelected(offset) },
            onValueDeselected = { handleValueDeselected() }
        )
    }

    private fun handleValueSelected(offset: Offset) {
        // 计算选中点的数据
        val data = chartData ?: return

        val x = offset.x
        val y = offset.y

        // 简单的选择处理 - 实际需要更精确的计算
        val xIndex = ((x / chartWidth) * (xRangeMax - xRangeMin) + xRangeMin).toInt()
        val selectedY = y

        val highlight = Highlight(
            x = x,
            y = selectedY,
            xIndex = xIndex,
            dataSetIndex = 0
        )

        selectionState = SelectionState(
            selectedX = x,
            selectedY = selectedY,
            selectedHighlight = highlight
        )
    }

    private fun handleValueDeselected() {
        selectionState = selectionState.clear()
    }

    fun setData(data: ChartData) {
        chartData = data
        updateRanges()
    }

    fun setStyle(style: ChartStyle) {
        chartStyle = style
    }

    fun setLegendStyle(style: LegendStyle) {
        legendStyle = style
    }

    fun setAxisData(axis: AxisData) {
        axisData = axis
    }

    fun setLimitLines(lines: LimitLines) {
        limitLines = lines
    }

    private fun updateRanges() {
        val data = chartData ?: return
        xRangeMin = data.getX最小值()
        xRangeMax = data.getX最大值()
        yRangeMin = data.getY最小值()
        yRangeMax = data.getY最大值()
    }

    fun updateChartSize(width: Float, height: Float) {
        chartWidth = width
        chartHeight = height
    }

    fun highlightValue(highlight: Highlight) {
        selectionState = selectionState.copy(selectedHighlight = highlight)
    }

    fun clearSelection() {
        selectionState = selectionState.clear()
    }

    fun resetZoom() {
        gestureState = gestureState.copy(
            scaleX = 1f,
            scaleY = 1f,
            translationX = 0f,
            translationY = 0f
        )
    }
}

/**
 * 创建并记住图表状态
 */
@Composable
fun rememberChartState(): ChartState {
    return remember { ChartState() }
}
