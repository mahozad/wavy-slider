package ir.mahozad.multiplatform.wavyslider

import kotlin.test.Test
import kotlin.test.assertEquals

class UtilitiesTest {
    @Test
    fun for_0_total_wave_count_result_should_be_correct() {
        val result = generateHeightFactors(0)
        assertEquals(emptyArray<Float?>().joinToString(), result.joinToString())
    }

    @Test
    fun for_1_total_wave_count_result_should_be_correct() {
        val result = generateHeightFactors(1)
        assertEquals(arrayOf(0f).joinToString(), result.joinToString())
    }

    @Test
    fun for_2_total_wave_count_result_should_be_correct() {
        val result = generateHeightFactors(2)
        assertEquals(arrayOf(0f, 0f).joinToString(), result.joinToString())
    }

    @Test
    fun for_3_total_wave_count_result_should_be_correct() {
        val result = generateHeightFactors(3)
        assertEquals(arrayOf(0f, 0f, 1f).joinToString(), result.joinToString())
    }

    @Test
    fun for_4_total_wave_count_result_should_be_correct() {
        val result = generateHeightFactors(4)
        assertEquals(arrayOf(0f, 0f, 1f, 1f).joinToString(), result.joinToString())
    }

    @Test
    fun for_5_total_wave_count_result_should_be_correct() {
        val result = generateHeightFactors(5)
        assertEquals(arrayOf(0f, 0f, 0.5f, 1f, 1f).joinToString(), result.joinToString())
    }

    @Test
    fun for_6_total_wave_count_result_should_be_correct() {
        val result = generateHeightFactors(6)
        assertEquals(arrayOf(0f, 0f, 0.33333334f, 0.6666667f, 1f, 1f).joinToString(), result.joinToString())
    }

    @Test
    fun for_7_total_wave_count_result_should_be_correct() {
        val result = generateHeightFactors(7)
        assertEquals(arrayOf(0f, 0f, 0.25f, 0.5f, 0.75f, 1f, 1f).joinToString(), result.joinToString())
    }
}
