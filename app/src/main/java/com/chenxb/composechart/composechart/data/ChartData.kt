package com.chenxb.composechart.composechart.data

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

/**
 * 图表数据容器基类
 * 对应 MPAndroidChart 的 ChartData
 */
abstract class ChartData {
    abstract fun getDataSetCount(): Int
    abstract fun getEntryCount(): Int
    abstract fun getY最大值(): Float
    abstract fun getY最小值(): Float
    abstract fun getX最大值(): Float
    abstract fun getX最小值(): Float
}

/**
 * 折线图数据
 */
class LineChartData(
    private var _dataSets: List<LineDataSet> = emptyList()
) : ChartData() {

    private var _version: Int = 0
    val version: Int get() = _version

    var isHighlightEnabled: Boolean = true
    var is宽松模式: Boolean = false

    fun getDataSets(): List<LineDataSet> = _dataSets

    fun setDataSets(dataSets: List<LineDataSet>) {
        _dataSets = dataSets
        _version++
    }

    fun addEntry(entry: LineEntry, dataSetIndex: Int = 0) {
        if (dataSetIndex < 0 || dataSetIndex >= _dataSets.size) return
        val dataSet = _dataSets[dataSetIndex]
        val newEntries = dataSet.getEntries().toMutableList()
        newEntries.add(entry)
        val newDataSet = LineDataSet(dataSet.label, newEntries).apply {
            lineColor = dataSet.lineColor
            circleColor = dataSet.circleColor
            lineWidth = dataSet.lineWidth
        }
        _dataSets = _dataSets.toMutableList().apply { this[dataSetIndex] = newDataSet }
        _version++
    }

    fun notifyDataChanged() {
        _version++
    }

    override fun getDataSetCount(): Int = _dataSets.size

    override fun getEntryCount(): Int = _dataSets.sumOf { it.getEntryCount() }

    override fun getY最大值(): Float = _dataSets.maxOfOrNull { it.getY最大值() } ?: 0f

    override fun getY最小值(): Float = _dataSets.minOfOrNull { it.getY最小值() } ?: 0f

    override fun getX最大值(): Float = _dataSets.maxOfOrNull { it.getX最大值() } ?: 0f

    override fun getX最小值(): Float = _dataSets.minOfOrNull { it.getX最小值() } ?: 0f
}

/**
 * 柱状图数据
 */
class BarChartData(
    private var _dataSets: List<BarDataSet> = emptyList()
) : ChartData() {

    private var _version: Int = 0
    val version: Int get() = _version

    var isHighlightEnabled: Boolean = true
    var groupSpace: Float = 0.1f
    var barSpace: Float = 0.05f

    fun getDataSets(): List<BarDataSet> = _dataSets

    fun setDataSets(dataSets: List<BarDataSet>) {
        _dataSets = dataSets
        _version++
    }

    fun addEntry(entry: BarEntry, dataSetIndex: Int = 0) {
        if (dataSetIndex < 0 || dataSetIndex >= _dataSets.size) return
        val dataSet = _dataSets[dataSetIndex]
        val newEntries = dataSet.getEntries().toMutableList()
        newEntries.add(entry)
        val newDataSet = BarDataSet(dataSet.label, newEntries).apply {
            color = dataSet.color
        }
        _dataSets = _dataSets.toMutableList().apply { this[dataSetIndex] = newDataSet }
        _version++
    }

    fun notifyDataChanged() {
        _version++
    }

    override fun getDataSetCount(): Int = _dataSets.size

    override fun getEntryCount(): Int = _dataSets.sumOf { it.getEntryCount() }

    override fun getY最大值(): Float = _dataSets.maxOfOrNull { it.getY最大值() } ?: 0f

    override fun getY最小值(): Float = _dataSets.minOfOrNull { it.getY最小值() } ?: 0f

    override fun getX最大值(): Float = _dataSets.maxOfOrNull { it.getX最大值() } ?: 0f

    override fun getX最小值(): Float = _dataSets.minOfOrNull { it.getX最小值() } ?: 0f
}

/**
 * 饼图数据
 */
class PieChartData(
    private var _dataSets: List<PieDataSet> = emptyList()
) : ChartData() {

    private var _version: Int = 0
    val version: Int get() = _version

    var isHighlightEnabled: Boolean = true

    fun getDataSets(): List<PieDataSet> = _dataSets

    fun setDataSets(dataSets: List<PieDataSet>) {
        _dataSets = dataSets
        _version++
    }

    fun addEntry(entry: PieEntry, dataSetIndex: Int = 0) {
        if (dataSetIndex < 0 || dataSetIndex >= _dataSets.size) return
        val dataSet = _dataSets[dataSetIndex]
        val newEntries = dataSet.getEntries().toMutableList()
        newEntries.add(entry)
        val newDataSet = PieDataSet(dataSet.label, newEntries)
        _dataSets = _dataSets.toMutableList().apply { this[dataSetIndex] = newDataSet }
        _version++
    }

    fun notifyDataChanged() {
        _version++
    }

    override fun getDataSetCount(): Int = _dataSets.size

    override fun getEntryCount(): Int = _dataSets.sumOf { it.getEntryCount() }

    override fun getY最大值(): Float = _dataSets.maxOfOrNull { it.getY最大值() } ?: 0f

    override fun getY最小值(): Float = _dataSets.minOfOrNull { it.getY最小值() } ?: 0f

    override fun getX最大值(): Float = _dataSets.maxOfOrNull { it.getX最大值() } ?: 0f

    override fun getX最小值(): Float = _dataSets.minOfOrNull { it.getX最小值() } ?: 0f
}

/**
 * 散点图数据
 */
class ScatterChartData(
    private var _dataSets: List<ScatterDataSet> = emptyList()
) : ChartData() {

    private var _version: Int = 0
    val version: Int get() = _version

    var isHighlightEnabled: Boolean = true

    fun getDataSets(): List<ScatterDataSet> = _dataSets

    fun setDataSets(dataSets: List<ScatterDataSet>) {
        _dataSets = dataSets
        _version++
    }

    fun addEntry(entry: LineEntry, dataSetIndex: Int = 0) {
        if (dataSetIndex < 0 || dataSetIndex >= _dataSets.size) return
        val dataSet = _dataSets[dataSetIndex]
        val newEntries = dataSet.getEntries().toMutableList()
        newEntries.add(entry)
        val newDataSet = ScatterDataSet(dataSet.label, newEntries).apply {
            color = dataSet.color
        }
        _dataSets = _dataSets.toMutableList().apply { this[dataSetIndex] = newDataSet }
        _version++
    }

    fun notifyDataChanged() {
        _version++
    }

    override fun getDataSetCount(): Int = _dataSets.size

    override fun getEntryCount(): Int = _dataSets.sumOf { it.getEntryCount() }

    override fun getY最大值(): Float = _dataSets.maxOfOrNull { it.getY最大值() } ?: 0f

    override fun getY最小值(): Float = _dataSets.minOfOrNull { it.getY最小值() } ?: 0f

    override fun getX最大值(): Float = _dataSets.maxOfOrNull { it.getX最大值() } ?: 0f

    override fun getX最小值(): Float = _dataSets.minOfOrNull { it.getX最小值() } ?: 0f
}

/**
 * K线图数据
 */
class CandleChartData(
    private var _dataSets: List<CandleDataSet> = emptyList()
) : ChartData() {

    private var _version: Int = 0
    val version: Int get() = _version

    var isHighlightEnabled: Boolean = true

    fun getDataSets(): List<CandleDataSet> = _dataSets

    fun setDataSets(dataSets: List<CandleDataSet>) {
        _dataSets = dataSets
        _version++
    }

    fun addEntry(entry: CandleEntry, dataSetIndex: Int = 0) {
        if (dataSetIndex < 0 || dataSetIndex >= _dataSets.size) return
        val dataSet = _dataSets[dataSetIndex]
        val newEntries = dataSet.getEntries().toMutableList()
        newEntries.add(entry)
        val newDataSet = CandleDataSet(dataSet.label, newEntries).apply {
            decreasingColor = dataSet.decreasingColor
            increasingColor = dataSet.increasingColor
        }
        _dataSets = _dataSets.toMutableList().apply { this[dataSetIndex] = newDataSet }
        _version++
    }

    fun notifyDataChanged() {
        _version++
    }

    override fun getDataSetCount(): Int = _dataSets.size

    override fun getEntryCount(): Int = _dataSets.sumOf { it.getEntryCount() }

    override fun getY最大值(): Float = _dataSets.flatMap { it.getEntries() }.maxOfOrNull { it.high } ?: 0f

    override fun getY最小值(): Float = _dataSets.flatMap { it.getEntries() }.minOfOrNull { it.low } ?: 0f

    override fun getX最大值(): Float = _dataSets.maxOfOrNull { it.getX最大值() } ?: 0f

    override fun getX最小值(): Float = _dataSets.minOfOrNull { it.getX最小值() } ?: 0f
}

/**
 * 雷达图数据
 */
class RadarChartData(
    private var _dataSets: List<RadarDataSet> = emptyList(),
    private var _labels: List<String> = emptyList()
) : ChartData() {

    private var _version: Int = 0
    val version: Int get() = _version

    var isHighlightEnabled: Boolean = true

    fun getLabels(): List<String> = _labels

    fun getDataSets(): List<RadarDataSet> = _dataSets

    fun setDataSets(dataSets: List<RadarDataSet>) {
        _dataSets = dataSets
        _version++
    }

    fun addEntry(entry: RadarEntry, dataSetIndex: Int = 0) {
        if (dataSetIndex < 0 || dataSetIndex >= _dataSets.size) return
        val dataSet = _dataSets[dataSetIndex]
        val newEntries = dataSet.getEntries().toMutableList()
        newEntries.add(entry)
        val newDataSet = RadarDataSet(dataSet.label, newEntries).apply {
            fillColor = dataSet.fillColor
        }
        _dataSets = _dataSets.toMutableList().apply { this[dataSetIndex] = newDataSet }
        _version++
    }

    fun notifyDataChanged() {
        _version++
    }

    override fun getDataSetCount(): Int = _dataSets.size

    override fun getEntryCount(): Int = _dataSets.sumOf { it.getEntryCount() }

    override fun getY最大值(): Float = _dataSets.maxOfOrNull { it.getY最大值() } ?: 0f

    override fun getY最小值(): Float = _dataSets.minOfOrNull { it.getY最小值() } ?: 0f

    override fun getX最大值(): Float = _dataSets.maxOfOrNull { it.getX最大值() } ?: 0f

    override fun getX最小值(): Float = _dataSets.minOfOrNull { it.getX最小值() } ?: 0f
}

/**
 * 气泡图数据
 */
class BubbleChartData(
    private var _dataSets: List<BubbleDataSet> = emptyList()
) : ChartData() {

    private var _version: Int = 0
    val version: Int get() = _version

    var isHighlightEnabled: Boolean = true

    fun getDataSets(): List<BubbleDataSet> = _dataSets

    fun setDataSets(dataSets: List<BubbleDataSet>) {
        _dataSets = dataSets
        _version++
    }

    fun addEntry(entry: BubbleEntry, dataSetIndex: Int = 0) {
        if (dataSetIndex < 0 || dataSetIndex >= _dataSets.size) return
        val dataSet = _dataSets[dataSetIndex]
        val newEntries = dataSet.getEntries().toMutableList()
        newEntries.add(entry)
        val newDataSet = BubbleDataSet(dataSet.label, newEntries).apply {
            color = dataSet.color
        }
        _dataSets = _dataSets.toMutableList().apply { this[dataSetIndex] = newDataSet }
        _version++
    }

    fun notifyDataChanged() {
        _version++
    }

    override fun getDataSetCount(): Int = _dataSets.size

    override fun getEntryCount(): Int = _dataSets.sumOf { it.getEntryCount() }

    override fun getY最大值(): Float = _dataSets.maxOfOrNull { it.getY最大值() } ?: 0f

    override fun getY最小值(): Float = _dataSets.minOfOrNull { it.getY最小值() } ?: 0f

    override fun getX最大值(): Float = _dataSets.maxOfOrNull { it.getX最大值() } ?: 0f

    override fun getX最小值(): Float = _dataSets.minOfOrNull { it.getX最小值() } ?: 0f
}

/**
 * 组合图数据
 */
class CombinedChartData : ChartData() {

    var lineData: LineChartData? = null
    var barData: BarChartData? = null
    var scatterData: ScatterChartData? = null
    var candleData: CandleChartData? = null
    var bubbleData: BubbleChartData? = null

    var isHighlightEnabled: Boolean = true

    fun notifyDataChanged() {
        lineData?.notifyDataChanged()
        barData?.notifyDataChanged()
        scatterData?.notifyDataChanged()
        candleData?.notifyDataChanged()
        bubbleData?.notifyDataChanged()
    }

    override fun getDataSetCount(): Int {
        var count = 0
        lineData?.let { count += it.getDataSetCount() }
        barData?.let { count += it.getDataSetCount() }
        scatterData?.let { count += it.getDataSetCount() }
        candleData?.let { count += it.getDataSetCount() }
        bubbleData?.let { count += it.getDataSetCount() }
        return count
    }

    override fun getEntryCount(): Int {
        var count = 0
        lineData?.let { count += it.getEntryCount() }
        barData?.let { count += it.getEntryCount() }
        scatterData?.let { count += it.getEntryCount() }
        candleData?.let { count += it.getEntryCount() }
        bubbleData?.let { count += it.getEntryCount() }
        return count
    }

    override fun getY最大值(): Float {
        var max = Float.MIN_VALUE
        lineData?.let { max = maxOf(max, it.getY最大值()) }
        barData?.let { max = maxOf(max, it.getY最大值()) }
        scatterData?.let { max = maxOf(max, it.getY最大值()) }
        candleData?.let { max = maxOf(max, it.getY最大值()) }
        bubbleData?.let { max = maxOf(max, it.getY最大值()) }
        return if (max == Float.MIN_VALUE) 0f else max
    }

    override fun getY最小值(): Float {
        var min = Float.MAX_VALUE
        lineData?.let { min = minOf(min, it.getY最小值()) }
        barData?.let { min = minOf(min, it.getY最小值()) }
        scatterData?.let { min = minOf(min, it.getY最小值()) }
        candleData?.let { min = minOf(min, it.getY最小值()) }
        bubbleData?.let { min = minOf(min, it.getY最小值()) }
        return if (min == Float.MAX_VALUE) 0f else min
    }

    override fun getX最大值(): Float {
        var max = Float.MIN_VALUE
        lineData?.let { max = maxOf(max, it.getX最大值()) }
        barData?.let { max = maxOf(max, it.getX最大值()) }
        scatterData?.let { max = maxOf(max, it.getX最大值()) }
        candleData?.let { max = maxOf(max, it.getX最大值()) }
        bubbleData?.let { max = maxOf(max, it.getX最大值()) }
        return if (max == Float.MIN_VALUE) 0f else max
    }

    override fun getX最小值(): Float {
        var min = Float.MAX_VALUE
        lineData?.let { min = minOf(min, it.getX最小值()) }
        barData?.let { min = minOf(min, it.getX最小值()) }
        scatterData?.let { min = minOf(min, it.getX最小值()) }
        candleData?.let { min = minOf(min, it.getX最小值()) }
        bubbleData?.let { min = minOf(min, it.getX最小值()) }
        return if (min == Float.MAX_VALUE) 0f else min
    }
}
