package ir.mahozad.multiplatform.wavyslider

import kotlin.math.abs

/**
 * We want the first two factors to be `0f` (flattened)
 * and the last two factors to be `1f` (full height).
 * For example, if [count] is `7`, we want the following factors:
 *
 * ```
 * [0f, 0f, 0.25f, 0.5f, 0.75f, 1f, 1f]
 * ```
 *
 * Note that the first two waves and the last two waves are extra but required
 * which will be taken advantage of in edge slider values.
 */
internal fun generateHeightFactors(count: Int) = FloatArray(count) {
    // Subtracts count by 3:
    //  1 because there is an extra wave at the start
    //  1 because there is an extra wave at the end
    //  1 because we want index instead of count
    if (it < 2) 0f else ((it - 1) / (count - 3f)).coerceAtMost(1f)
}

internal fun snapValueToTick(
    current: Float,
    tickFractions: FloatArray,
    minPx: Float,
    maxPx: Float
): Float {
    // target is a closest anchor to the `current`, if exists
    return tickFractions
        .minByOrNull { abs(lerp(minPx, maxPx, it) - current) }
        ?.run { lerp(minPx, maxPx, this) }
        ?: current
}

// Scale x1 from a1..b1 range to a2..b2 range
internal fun scale(a1: Float, b1: Float, x1: Float, a2: Float, b2: Float) =
    lerp(a2, b2, calcFraction(a1, b1, x1))

internal fun lerp(start: Float, stop: Float, fraction: Float) =
    (start * (1 - fraction) + stop * fraction)

// Calculate the 0..1 fraction that `pos` value represents between `a` and `b`
internal fun calcFraction(a: Float, b: Float, pos: Float) =
    (if (b - a == 0f) 0f else (pos - a) / (b - a)).coerceIn(0f, 1f)
