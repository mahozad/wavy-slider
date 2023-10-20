The [logo-styles.css](logo-styles.css) and [logo-icon.svg](logo-icon.svg) are used for Kdoc (Dokka).

To generate the [demo-movie.gif](demo-movie.gif) did as follows:

1. Downloaded AutoMouse Pro 1.0.5
2. Launched Clipper
3. Hovered cursor on the desired points on Clipper and determined their coordinated in AutoMouse (it shows live coordinates)
4. Generated cursor movements and actions with this Kotlin script:
    ```kotlin
    import kotlin.math.roundToInt
    
    // Target cursor points
    val center = 960 to 434
    val right = 1210 to 626
    val left = 724 to 626
    
    val waitAtCenter = interpolate(center, center, 30).map(::round)
    val waitAtRight = interpolate(right, right, 15).map(::round)
    val waitAtLeft = interpolate(left, left, 15).map(::round)
    val moveFromCenterToRight = interpolate(center, right, 30).map(::round)
    val moveFromRightToCenter = interpolate(right, center, 30).map(::round)
    val moveFromRightToLeft = interpolate(right, left, 120).map(::round)
    val moveFromLeftToRight = interpolate(left, right, 120).map(::round)
    
    (
        waitAtCenter.map(::move) +
        moveFromCenterToRight.map(::move) +
        moveFromCenterToRight.last().let(::press) +
        waitAtRight.map(::move) +
        moveFromRightToLeft.map(::move) +
        moveFromLeftToRight.map(::move) +
        moveFromLeftToRight.last().let(::release) +
        moveFromRightToCenter.map(::move) +
        waitAtCenter.map(::move)
    )
        .forEach(::println)
    
    fun interpolate(
        a: Pair<Int, Int>,
        b: Pair<Int, Int>,
        count: Int
    ): List<Pair<Float, Float>> {
        val (x1, y1) = a
        val (x2, y2) = b
        val deltaX = x2 - x1
        val deltaY = y2 - y1
        val slope = if (deltaX == 0) 0f else deltaY.toFloat() / deltaX
        val xIncrement = deltaX.toFloat() / count
        val yIncrement = xIncrement * slope
        return buildList {
            for (i in 0..count) {
                val x = x1 + i * xIncrement
                val y = y1 + i * yIncrement
                add(x to y)
            }
        }
    }
    
    fun round(point: Pair<Float, Float>) = point.first.roundToInt() to point.second.roundToInt()
    
    fun move(point: Pair<Int, Int>) = "${point.first}|${point.second}|mov"
    
    fun press(point: Pair<Int, Int>) = "${point.first}|${point.second}|ltd"
    
    fun release(point: Pair<Int, Int>) = "${point.first}|${point.second}|ltu"
    ```
5. Saved the output in a *file.arf* file
6. Loaded the *file.arf* into AutoMouse using its *Load* button
7. Clicked AutoMouse *Replay* button
8. Took the video with Bandicam
9. Converted the video to GIF using FFmpeg (https://superuser.com/q/556029):
    ```shell
    ffmpeg \
    -y \
    -ss 2800ms \
    -to 14800ms \
    -i demo.mp4 \
    -vf "fps=25,split[s0][s1];[s0]palettegen=max_colors=32[p];[s1][p]paletteuse" \
    -loop 0 \
    output.gif
    ```
10. Optimized the GIF with https://ezgif.com/optimize
