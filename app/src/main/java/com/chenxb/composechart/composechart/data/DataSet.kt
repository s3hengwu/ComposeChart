package com.chenxb.composechart.composechart.data

import androidx.compose.ui.graphics.Color
import com.chenxb.composechart.composechart.style.ColorTemplates

/**
 * 数据集基类
 * 对应 MPAndroidChart 的 DataSet
 */
abstract class DataSet<T : ChartEntry>(
    val label: String,
    private val entries: List<T> = emptyList()
) {
    var color: Color = ColorTemplates.VORDIPLOM_COLORS[0]
    open var isDrawValuesEnabled: Boolean = true
    var isDrawIconsEnabled: Boolean = false
    var valueTextColor: Color = Color.Black
    var valueTextSize: Float = 10f

    fun getEntries(): List<T> = entries

    fun getEntryCount(): Int = entries.size

    fun getEntryForIndex(index: Int): T? = entries.getOrNull(index)

    fun getEntryForX(x: Float): T? {
        return entries.find { it.x == x }
    }

    fun getY最大值(): Float = entries.maxOfOrNull { it.y } ?: 0f

    fun getY最小值(): Float = entries.minOfOrNull { it.y } ?: 0f

    fun getX最大值(): Float = entries.maxOfOrNull { it.x } ?: 0f

    fun getX最小值(): Float = entries.minOfOrNull { it.x } ?: 0f
}

/**
 * 折线数据集
 */
class LineDataSet(
    label: String,
    entries: List<LineEntry> = emptyList()
) : DataSet<LineEntry>(label, entries) {

    var lineWidth: Float = 1f
    var circleRadius: Float = 4f
    var isDrawCircleHole: Boolean = true
    var circleHoleRadius: Float = 2f
    var circleColor: Color = color
    var circleHoleColor: Color = Color.White
    var lineColor: Color = color

    var isDrawFilledEnabled: Boolean = false
    var fillColor: Color = color.copy(alpha = 0.5f)
    var fillAlpha: Float = 0.5f

    var isDrawDashedLineEnabled: Boolean = false
    var dashLength: Float = 10f
    var dashSpaceLength: Float = 5f

    var isHighlightEnabled: Boolean = true
    var highlightColor: Color = Color.Gray
    var highlightLineWidth: Float = 1f

    enum class Mode { LINEAR, CUBIC_BEZIER, STEPPED, HORIZONTAL_BEZIER }
    var mode: Mode = Mode.LINEAR
}

/**
 * 柱状数据集
 */
class BarDataSet(
    label: String,
    entries: List<BarEntry> = emptyList()
) : DataSet<BarEntry>(label, entries) {

    var barWidth: Float = 0.9f
    var isDrawBarShadowEnabled: Boolean = false
    var isDrawValuesAboveBar: Boolean = true
    var stackLabels: List<String> = emptyList()
    var stackColors: List<Color>? = null
    var gradientColors: List<Color> = emptyList()

    var isHighlightEnabled: Boolean = true
    var highlightColor: Color = Color.Gray
    var highlightAlpha: Float = 1f
}

/**
 * 饼图数据集
 */
class PieDataSet(
    label: String,
    entries: List<PieEntry> = emptyList()
) : DataSet<PieEntry>(label, entries) {

    var sliceSpace: Float = 0f
    var selectionShift: Float = 5f
    override var isDrawValuesEnabled: Boolean = true
    var valueLinePart1OffsetPercentage: Float = 30f
    var valueLinePart1Length: Float = 0.5f
    var valueLinePart2Length: Float = 0.5f
    var xValuePosition: XValuePosition = XValuePosition.OUTSIDE_SLICE
    var yValuePosition: YValuePosition = YValuePosition.OUTSIDE_SLICE

    enum class XValuePosition { INSIDE_SLICE, OUTSIDE_SLICE }
    enum class YValuePosition { INSIDE_SLICE, OUTSIDE_SLICE }
}

/**
 * 散点数据集
 */
class ScatterDataSet(
    label: String,
    entries: List<LineEntry> = emptyList()
) : DataSet<LineEntry>(label, entries) {

    var scatterShapeSize: Float = 10f
    var scatterShape: ScatterShape = ScatterShape.CIRCLE
    var shapeRenderer: ShapeRenderer? = null

    // Shape Hole
    var isDrawShapeHoleEnabled: Boolean = false
    var shapeHoleRadius: Float = 5f
    var shapeHoleColor: Color = Color.White

    enum class ScatterShape {
        CIRCLE, SQUARE, TRIANGLE, CROSS, X, CHEVRON_UP, CHEVRON_DOWN
    }

    interface ShapeRenderer {
        fun renderShape(
            canvas: androidx.compose.ui.graphics.Canvas,
            center: androidx.compose.ui.geometry.Offset,
            size: Float
        )
    }
}

/**
 * K线数据集
 */
class CandleDataSet(
    label: String,
    entries: List<CandleEntry> = emptyList()
) : DataSet<CandleEntry>(label, entries) {

    var shadowColor: Color = Color.DarkGray
    var shadowWidth: Float = 0.7f
    var decreasingColor: Color = Color.Red
    var decreasingPaintStyle: PaintStyle = PaintStyle.FILL
    var increasingColor: Color = Color.Green
    var increasingPaintStyle: PaintStyle = PaintStyle.STROKE
    var neutralColor: Color = Color.Blue
    var shadowColorSameAsCandle: Boolean = false

    enum class PaintStyle { FILL, STROKE }
}

/**
 * 雷达数据集
 */
class RadarDataSet(
    label: String,
    entries: List<RadarEntry> = emptyList()
) : DataSet<RadarEntry>(label, entries) {

    var lineWidth: Float = 2f
    var fillColor: Color = color
    var fillAlpha: Float = 0.3f
    var isDrawFilledEnabled: Boolean = false
    var isDrawHighlightCircleEnabled: Boolean = true
    var highlightCircleStrokeWidth: Float = 1f
    var highlightCircleFillColor: Color = Color.White
    var highlightCircleStrokeColor: Color = Color.Gray
}

/**
 * 气泡数据集
 */
class BubbleDataSet(
    label: String,
    entries: List<BubbleEntry> = emptyList()
) : DataSet<BubbleEntry>(label, entries) {

    var bubbleRadius: Float = 25f
    override var isDrawValuesEnabled: Boolean = false
    var highlightColor: Color = Color.Gray
    var highlightAlpha: Float = 1f
    var maxSize: Float = 30f
    var normalizeSize: Boolean = true
}
