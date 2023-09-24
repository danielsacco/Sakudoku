package com.des.sakudoku

import com.des.sakudoku.board.generator.CommandBackTrackGenerator
import com.des.sakudoku.board.generator.LinearBackTrackGenerator
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime
import kotlin.time.toDuration

class GeneratorsPerformanceTest {

    val times = 500
    @OptIn(ExperimentalTime::class)
    @Test
    fun measureCommandBackTrackGenerator() {
        val measures = mutableListOf<Duration>()

        repeat(times) {
            measures.add(
                measureTime {
                    CommandBackTrackGenerator().generateBoard()
                }
            )
        }

        printMin(measures)
        printAverage(measures)
        printMax(measures)
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun measureLinearBackTrackGenerator() {

        val measures = mutableListOf<Duration>()

        repeat(times) {
            measures.add(
                measureTime {
                    LinearBackTrackGenerator().generateBoard()
                }
            )
        }

        printMin(measures)
        printAverage(measures)
        printMax(measures)
    }

    private fun printMin(measures: MutableList<Duration>) {
        println("Min duration: ${measures.min()}")
    }

    private fun printMax(measures: MutableList<Duration>) {
        println("Max duration: ${measures.max()}")
    }

    private fun printAverage(measures: MutableList<Duration>) {
        val average = measures.sumOf { it.inWholeMicroseconds } / times
        val duration = average.toDuration(DurationUnit.MICROSECONDS)

        println("Avg duration: $duration")
    }

}