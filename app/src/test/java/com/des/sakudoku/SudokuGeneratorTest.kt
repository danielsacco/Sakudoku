package com.des.sakudoku

import com.des.sakudoku.board.generator.Board
import com.des.sakudoku.board.generator.Cell
import com.des.sakudoku.board.generator.LinearBackTrackGenerator
import com.des.sakudoku.board.generator.RowCol
import com.des.sakudoku.board.generator.createCell
import com.des.sakudoku.board.generator.toRowCol
import org.junit.Assert.assertEquals
import org.junit.Test

class SudokuGeneratorTest {

    @Test
    fun testLinearFiller() {

        val sut = LinearBackTrackGenerator()

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

        board.print()

    }


    @Test
    fun testCoordinatesPositioning() {

        val board = Board()

        val cell0x2 = board.cells[2].also { it.value = 5 }
        val cell3x1 = board.cells[28].also { it.value = 7 }

        assertEquals(cell0x2.col, 2)
        assertEquals(cell0x2.row, 0)
        assertEquals(cell0x2.group, 0)
        assertEquals(cell0x2.value, 5)

        assertEquals(cell3x1.col, 1)
        assertEquals(cell3x1.row, 3)
        assertEquals(cell3x1.group, 3)
        assertEquals(cell3x1.value, 7)
    }

    @Test
    fun testGroupCalculator() {
        assertEquals(Cell(col = 0, row = 0).group, 0)
        assertEquals(Cell(col = 1, row = 0).group, 0)
        assertEquals(Cell(col = 2, row = 0).group, 0)
        assertEquals(Cell(col = 3, row = 0).group, 1)
        assertEquals(27.toRowCol().createCell().group, 3)
        assertEquals(50.toRowCol().createCell().group, 4)
        assertEquals(51.toRowCol().createCell().group, 5)
        assertEquals(80.toRowCol().createCell().group, 8)
    }

    @Test
    fun testRowColCalculator() {
        assertEquals(RowCol(0,0), 0.toRowCol())
        assertEquals(RowCol(0,1), 1.toRowCol())
        assertEquals(RowCol(2,3), 21.toRowCol())
        assertEquals(RowCol(8,8), 80.toRowCol())
    }

}




