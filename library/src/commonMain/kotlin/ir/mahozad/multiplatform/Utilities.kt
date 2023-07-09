package ir.mahozad.multiplatform


/**
 * We want the first two factors to be `0f` (flattened)
 * and the last two factors to be `1f` (full height).
 * For example, if [count] is `7`, we want the following factors:
 *
 * ```
 * [0f, 0f, 0.25f, 0.5f, 0.75f, 1f, 1f]
 * ```
 *
 * Note that the first wave and the last wave are extra but required
 * which will be taken advantage of in edge slider values.
 */
internal fun generateHeightFactors(count: Int) = FloatArray(count) {
    // Subtracts count by 3:
    //  1 because there is an extra wave at the start
    //  1 because there is an extra wave at the end
    //  1 because we want index instead of count
    if (it < 2) 0f else ((it - 1) / (count - 3f)).coerceAtMost(1f)
}
