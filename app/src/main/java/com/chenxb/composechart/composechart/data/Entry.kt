package com.chenxb.composechart.composechart.data

/**
 * 图表数据条目基类
 * 对应 MPAndroidChart 的 Entry
 */
open class ChartEntry(
    open val x: Float,
    open val y: Float,
    open val data: Any? = null
) {
    companion object {
        fun <T : ChartEntry> create(
            x: Float,
            y: Float,
            data: Any? = null
        ): ChartEntry = ChartEntry(x, y, data)
    }
}

/**
 * 折线图/散点图数据条目
 */
data class LineEntry(
    override val x: Float,
    override val y: Float,
    val icon: Any? = null,
    override val data: Any? = null
) : ChartEntry(x, y, data)

/**
 * 柱状图数据条目
 */
data class BarEntry(
    override val x: Float,
    override val y: Float,
    val yStack: FloatArray? = null,
    val icon: Any? = null,
    override val data: Any? = null
) : ChartEntry(x, y, data) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BarEntry

        if (x != other.x) return false
        if (y != other.y) return false
        if (yStack != null) {
            if (other.yStack == null) return false
            if (!yStack.contentEquals(other.yStack)) return false
        } else if (other.yStack != null) return false
        return true
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        result = 31 * result + (yStack?.contentHashCode() ?: 0)
        return result
    }
}

/**
 * 饼图数据条目
 */
data class PieEntry(
    override val x: Float,
    override val y: Float,
    val label: String? = null,
    val icon: Any? = null,
    override val data: Any? = null
) : ChartEntry(x, y, data)

/**
 * K线图数据条目 (CandleStick)
 */
data class CandleEntry(
    override val x: Float,
    val high: Float,
    val low: Float,
    val open: Float,
    val close: Float,
    val icon: Any? = null,
    override val data: Any? = null
) : ChartEntry(x, (high + low) / 2, data)

/**
 * 雷达图数据条目
 */
data class RadarEntry(
    override val x: Float,
    override val y: Float,
    override val data: Any? = null
) : ChartEntry(x, y, data)

/**
 * 气泡图数据条目
 */
data class BubbleEntry(
    override val x: Float,
    override val y: Float,
    val size: Float = 1f,
    val icon: Any? = null,
    override val data: Any? = null
) : ChartEntry(x, y, data)
