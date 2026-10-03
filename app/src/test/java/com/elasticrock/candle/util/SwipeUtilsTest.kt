package com.elasticrock.candle.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SwipeUtilsTest {

    private val range = 0f..1f
    private val height = 2000

    @Test
    fun swipeUpIncreasesBrightness() {
        assertTrue(applySwipe(0.5f, -100f, height, range) > 0.5f)
    }

    @Test
    fun swipeDownDecreasesBrightness() {
        assertTrue(applySwipe(0.5f, 100f, height, range) < 0.5f)
    }

    @Test
    fun clampsAtMaximum() {
        assertEquals(1f, applySwipe(0.9f, -5000f, height, range), 0f)
    }

    @Test
    fun clampsAtMinimum() {
        assertEquals(0f, applySwipe(0.1f, 5000f, height, range), 0f)
    }

    @Test
    fun fullSwipeCoversFullRange() {
        val dy = height * FULL_SWIPE_FRACTION
        assertEquals(1f, applySwipe(0f, -dy, height, range), 1e-6f)
        assertEquals(0f, applySwipe(1f, dy, height, range), 1e-6f)
    }

    @Test
    fun respectsNonUnitRange() {
        val dy = height * FULL_SWIPE_FRACTION / 2
        assertEquals(15f, applySwipe(10f, -dy, height, 10f..20f), 1e-4f)
    }

    @Test
    fun zeroHeightChangesNothing() {
        assertEquals(0.5f, applySwipe(0.5f, -100f, 0, range), 0f)
    }
}
