package com.des.sakudoku

import com.des.sakudoku.board.generator.CommandBackTrackGenerator
import org.junit.Assert.assertEquals
import org.junit.Test

class CommandBackTrackGeneratorTest {

    @Test
    fun testPrefill() {

        val sut = CommandBackTrackGenerator

        val board = sut.generateBoard()

        board.cellsByCol.forEach {
            val cells = it.value
            assertEquals(cells.distinctBy { cell -> cell.value }.size, 9)
        }
        board.cellsByRow.forEach {
            val cells = it.value
            assertEquals(cells.distinctBy { cell -> cell.value }.size, 9)
        }
        board.cellsByGroup.forEach {
            val cells = it.value
            assertEquals(cells.distinctBy { cell -> cell.value }.size, 9)
        }

        //board.print()

    }

    @Test
    fun repeatTest() {
        repeat(10) {
            testPrefill()
        }
    }

}

