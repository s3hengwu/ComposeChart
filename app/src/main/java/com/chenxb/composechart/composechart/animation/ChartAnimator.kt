package com.chenxb.composechart.composechart.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

/**
 * 缓动函数
 * 对应 MPAndroidChart 的 Easing
 */
enum class Easing {
    LINEAR,
    EASE_IN_QUAD,
    EASE_OUT_QUAD,
    EASE_IN_OUT_QUAD,
    EASE_IN_CUBIC,
    EASE_OUT_CUBIC,
    EASE_IN_OUT_CUBIC,
    EASE_IN_QUART,
    EASE_OUT_QUART,
    EASE_IN_OUT_QUART,
    EASE_IN_SINE,
    EASE_OUT_SINE,
    EASE_IN_OUT_SINE,
    EASE_IN_EXPO,
    EASE_OUT_EXPO,
    EASE_IN_OUT_EXPO,
    EASE_IN_CIRC,
    EASE_OUT_CIRC,
    EASE_IN_OUT_CIRC,
    EASE_BOUNCE,
    EASE_IN_BOUNCE,
    EASE_OUT_BOUNCE,
    EASE_IN_OUT_BOUNCE;

    companion object {
        fun toEasingFunction(easing: Easing): androidx.compose.animation.core.Easing {
            return when (easing) {
                LINEAR -> LinearEasing
                EASE_IN_QUAD -> FastOutSlowInEasing
                EASE_OUT_QUAD -> FastOutSlowInEasing
                EASE_IN_OUT_QUAD -> FastOutSlowInEasing
                EASE_IN_CUBIC -> FastOutSlowInEasing
                EASE_OUT_CUBIC -> FastOutSlowInEasing
                EASE_IN_OUT_CUBIC -> FastOutSlowInEasing
                EASE_IN_QUART -> FastOutSlowInEasing
                EASE_OUT_QUART -> FastOutSlowInEasing
                EASE_IN_OUT_QUART -> FastOutSlowInEasing
                EASE_IN_SINE -> FastOutSlowInEasing
                EASE_OUT_SINE -> FastOutSlowInEasing
                EASE_IN_OUT_SINE -> FastOutSlowInEasing
                EASE_IN_EXPO -> FastOutSlowInEasing
                EASE_OUT_EXPO -> FastOutSlowInEasing
                EASE_IN_OUT_EXPO -> FastOutSlowInEasing
                EASE_IN_CIRC -> FastOutSlowInEasing
                EASE_OUT_CIRC -> FastOutSlowInEasing
                EASE_IN_OUT_CIRC -> FastOutSlowInEasing
                EASE_BOUNCE -> FastOutSlowInEasing
                EASE_IN_BOUNCE -> FastOutSlowInEasing
                EASE_OUT_BOUNCE -> FastOutSlowInEasing
                EASE_IN_OUT_BOUNCE -> FastOutSlowInEasing
            }
        }
    }
}

/**
 * 图表动画配置
 */
data class ChartAnimation(
    val durationMillis: Int = 1000,
    val easing: Easing = Easing.EASE_IN_OUT_QUAD,
    val delayMillis: Int = 0,
    val staggerDelayMillis: Int = 0
) {
    companion object {
        val DEFAULT = ChartAnimation()
        val FAST = ChartAnimation(durationMillis = 500)
        val SLOW = ChartAnimation(durationMillis = 2000)

        fun animateX(durationMillis: Int = 1000, easing: Easing = Easing.EASE_IN_OUT_QUAD) =
            ChartAnimation(durationMillis, easing)

        fun animateY(durationMillis: Int = 1000, easing: Easing = Easing.EASE_IN_OUT_QUAD) =
            ChartAnimation(durationMillis, easing)

        fun animateXY(
            xDurationMillis: Int = 1000,
            yDurationMillis: Int = 1000,
            easing: Easing = Easing.EASE_IN_OUT_QUAD
        ) = ChartAnimation(maxOf(xDurationMillis, yDurationMillis), easing)

        fun spin(durationMillis: Int = 1000) =
            ChartAnimation(durationMillis, Easing.LINEAR)
    }

    fun toAnimationSpec(): AnimationSpec<Float> {
        return tween(
            durationMillis = durationMillis,
            easing = Easing.toEasingFunction(easing)
        )
    }
}

/**
 * 单个数据点的动画状态
 */
data class EntryAnimationState(
    val entryIndex: Int,
    val progress: Float = 0f,
    val isNew: Boolean = false,
    val isRemoving: Boolean = false
)

/**
 * 图表进度动画状态
 */
class ChartAnimationState {
    private val _progress = Animatable(0f)
    val progress: Float get() = _progress.value
    val isAnimating: Boolean get() = _progress.value < 1f
}

/**
 * 记住图表动画状态
 */
@Composable
fun rememberChartAnimationState(
    animation: ChartAnimation,
    enabled: Boolean = true
): Float {
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(animation, enabled) {
        if (!enabled) {
            animatedProgress.snapTo(1f)
            return@LaunchedEffect
        }

        if (animation.delayMillis > 0) {
            delay(animation.delayMillis.toLong())
        }

        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = animation.toAnimationSpec()
        )
    }

    return animatedProgress.value
}

/**
 * 带动画的图表绘制
 * 返回动画进度值 (0f 到 1f)
 */
@Composable
fun animateChartProgress(
    animation: ChartAnimation = ChartAnimation.DEFAULT,
    enabled: Boolean = true
): Float {
    return rememberChartAnimationState(animation, enabled)
}

/**
 * 数据条目动画状态管理器
 * 用于管理单个数据条的添加、删除动画
 */
class EntryAnimationManager {
    private val _entryStates = mutableStateMapOf<Int, EntryAnimationState>()
    val entryStates: Map<Int, EntryAnimationState> = _entryStates

    private var _isAnimating = mutableStateOf(false)
    val isAnimating: Boolean get() = _isAnimating.value

    fun setEntryCount(count: Int) {
        val currentKeys = _entryStates.keys.toSet()
        val newKeys = (0 until count).toSet()

        // 移除不存在的条目
        currentKeys.filter { it !in newKeys }.forEach { key ->
            _entryStates[key] = EntryAnimationState(key, 1f, isRemoving = true)
        }

        // 添加新条目
        newKeys.filter { it !in currentKeys }.forEach { key ->
            _entryStates[key] = EntryAnimationState(key, 0f, isNew = true)
        }
    }

    fun updateEntryProgress(index: Int, progress: Float) {
        val current = _entryStates[index]
        if (current != null) {
            _entryStates[index] = current.copy(progress = progress)
        } else if (progress > 0f) {
            _entryStates[index] = EntryAnimationState(index, progress)
        }
    }

    fun getEntryProgress(index: Int, defaultProgress: Float = 1f): Float {
        return _entryStates[index]?.progress ?: defaultProgress
    }

    fun removeEntry(index: Int) {
        _entryStates[index] = EntryAnimationState(index, 1f, isRemoving = true)
    }

    fun addEntry(index: Int) {
        _entryStates[index] = EntryAnimationState(index, 0f, isNew = true)
    }

    fun setAnimating(animating: Boolean) {
        _isAnimating.value = animating
    }
}

/**
 * 记住数据条目动画状态管理器
 */
@Composable
fun rememberEntryAnimationManager(): EntryAnimationManager {
    return remember { EntryAnimationManager() }
}

/**
 * 计算单个条目的交错动画进度
 * @param index 条目索引
 * @param totalEntries 總條目數量
 * @param baseProgress 基础动画进度 (0-1)
 * @param staggerDelay 交错延迟（毫秒）
 * @param duration 单个条目动画时长（毫秒）
 * @return 调整后的动画进度
 */
fun calculateStaggeredProgress(
    index: Int,
    totalEntries: Int,
    baseProgress: Float,
    staggerDelay: Int = 50,
    duration: Int = 1000
): Float {
    if (totalEntries <= 1 || staggerDelay <= 0) return baseProgress

    // 计算每个条目的交错延迟比例
    val maxStaggerDelay = (totalEntries - 1) * staggerDelay.toFloat()
    val entryStaggerDelay = index * staggerDelay.toFloat()
    val staggerOffset = entryStaggerDelay / (maxStaggerDelay + duration)

    // 调整进度以实现交错效果
    val adjustedProgress = (baseProgress - staggerOffset).coerceIn(0f, 1f) / (1f - staggerOffset)
    return adjustedProgress.coerceIn(0f, 1f)
}

/**
 * 动画化添加条目
 * @param manager 条目动画管理器
 * @param index 新条目索引
 * @param duration 动画时长（毫秒）
 * @param onComplete 完成回调
 */
@Composable
fun animateAddEntry(
    manager: EntryAnimationManager,
    index: Int,
    duration: Int = 500,
    onComplete: () -> Unit = {}
) {
    val animatable = remember { Animatable(0f) }

    LaunchedEffect(index) {
        manager.addEntry(index)
        animatable.snapTo(0f)
        animatable.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = duration)
        )
        manager.updateEntryProgress(index, 1f)
        onComplete()
    }
}

/**
 * 动画化删除条目
 * @param manager 条目动画管理器
 * @param index 要删除的条目索引
 * @param duration 动画时长（毫秒）
 * @param onComplete 完成回调
 */
@Composable
fun animateRemoveEntry(
    manager: EntryAnimationManager,
    index: Int,
    duration: Int = 500,
    onComplete: () -> Unit = {}
) {
    val animatable = remember { Animatable(1f) }

    LaunchedEffect(index) {
        manager.removeEntry(index)
        animatable.snapTo(1f)
        animatable.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = duration)
        )
        onComplete()
    }
}
