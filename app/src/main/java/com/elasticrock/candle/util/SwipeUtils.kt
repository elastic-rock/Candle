package com.elasticrock.candle.util

// Fraction of the screen height a swipe has to cover to go through the full brightness range
internal const val FULL_SWIPE_FRACTION = 0.5f

// Converts a vertical drag (negative dy = up) into a new brightness value, relative to current
internal fun applySwipe(
    current: Float,
    dyPx: Float,
    heightPx: Int,
    range: ClosedFloatingPointRange<Float>
): Float {
    if (heightPx <= 0) return current
    val span = range.endInclusive - range.start
    return (current - dyPx / (heightPx * FULL_SWIPE_FRACTION) * span).coerceIn(range)
}
