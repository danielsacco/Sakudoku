package com.des.sakudoku

import org.junit.Test

class CellModelTest {

    val matrix = Array(9) { Array(9) { 0 } }

    @Test
    fun test1() {
        for (col in 0..8) {
            for (row in 0..8) {
                initCell(col = col, row = row)
            }
        }
        println(matrix.contentDeepToString())
    }

    private fun initCell(col: Int, row: Int) {

        val candidates = (1..9).toSet()
            .removeFromColumn(col)
            .removeFromRow(row)
            .removeFromGroup(col = col, row = row)


        matrix[col][row] = candidates.random()

    }

    /**
     * Remove candidates already used in the column
     */
    private fun Set<Int>.removeFromColumn(col: Int): Set<Int> {
        val alreadyInColumn = (0..8).map { matrix[col][it] }.filter { it != 0 }
        return this.minus(alreadyInColumn.toSet())
    }

    /**
     * Remove candidates already used in the row
     */
    private fun Set<Int>.removeFromRow(row: Int): Set<Int> {
        val alreadyInRow = (0..8).map { matrix[it][row] }.filter { it != 0 }
        return this.minus(alreadyInRow.toSet())
    }

    /**
     * Remove candidates already used in the group
     */
    @OptIn(ExperimentalStdlibApi::class)
    private fun Set<Int>.removeFromGroup(col: Int, row: Int): Set<Int> {
        //val groupNumber = ((row - 1) /3) + ((col - 1) / 3)
        val rowGroup = row / 3
        val columnGroup = col / 3

        val alreadyInGroup = mutableSetOf<Int>()
        val rows = (rowGroup * 3).rangeUntil((rowGroup + 1) * 3)
        val columns = (columnGroup * 3).rangeUntil((columnGroup + 1) * 3)
        for (rowIndex in rows) {
            for (columnIndex in columns) {
                alreadyInGroup.add(matrix[columnIndex][rowIndex])
            }
        }

        return this.minus(alreadyInGroup)

    }

}





