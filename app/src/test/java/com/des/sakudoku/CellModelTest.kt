package com.des.sakudoku

import org.junit.Assert.assertTrue
import org.junit.Test

private const val MAX_TRIES = 1000

class CellModelTest {

    //private val matrix = Array(9) { Array<Int?>(9) {null} }

    @Test
    fun testFillByRows() {
        var failureCount = 0
        var success = false

        val matrix = Array(9) { Array<Int?>(9) {null} }
        do {
            try {
                fillByRows(matrix)
                success = true
            } catch (e: Exception) {
                failureCount ++
                clearMatrix(matrix)
            }
        } while (failureCount < MAX_TRIES && !success)

        println("FillByRows failure count: $failureCount")
        println(matrix.contentDeepToString())
        assertTrue(success)
    }

    @Test
    fun testFillByGroups() {
        var failureCount = 0
        var success = false

        val matrix = Array(9) { Array<Int?>(9) {null} }
        do {
            try {
                fillByGroups(matrix = matrix)
                success = true
            } catch (e: Exception) {
                failureCount ++
                clearMatrix(matrix)
            }
        } while (failureCount < MAX_TRIES && !success)

        println("FillByGroups failure count: $failureCount")
        println(matrix.contentDeepToString())
        assertTrue(success)    }

    @Test
    fun testFillByDistributedGroups() {

        var failureCount = 0
        var success = false

        val matrix = Array(9) { Array<Int?>(9) {null} }
        do {
            try {
                fillByDistributedGroups(matrix)
                success = true
            } catch (e: Exception) {
                failureCount ++
                clearMatrix(matrix = matrix)
            }
        } while (failureCount < MAX_TRIES && !success)

        println("FillByDistributedGroups failure count: $failureCount")
        println(matrix.contentDeepToString())
        assertTrue(success)    }

    @Test
    fun testFillExponential() {
        var failureCount = 0
        var success = false

        val matrix = Array(9) { Array<Int?>(9) {null} }
        do {
            try {
                fillExponential(matrix)
                success = true
            } catch (e: Exception) {
                failureCount ++
                clearMatrix(matrix = matrix)
            }
        } while (failureCount < MAX_TRIES && !success)

        println("FillExponential failure count: $failureCount")
        println(matrix.contentDeepToString())
        assertTrue(success)
    }


    private fun fillByRows(matrix : Array<Array<Int?>>) {
        for (col in 0..8) {
            for (row in 0..8) {
                initCell(matrix = matrix, col = col, row = row)
            }
        }
    }

    private fun fillByGroups(matrix : Array<Array<Int?>>) {
        for (group in 0..8) {
            fillGroup(matrix = matrix, group)
        }
    }


    fun fillByDistributedGroups(matrix : Array<Array<Int?>>) {

        //listOf(0, 1, 3, 4, 2, 6, 5, 7, 8)
        //listOf(0, 1, 3, 2, 6, 4, 5, 7, 8)
        listOf(0, 4, 8, 1, 3, 2, 6, 5, 7)
            .forEach { fillGroup(matrix, it) }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun fillExponential(matrix : Array<Array<Int?>>) {

        for (limit in (0..8)) {
            initCell(matrix, limit, limit)
            for(i in (0..<limit)) {
                initCell(matrix, i, limit)
                initCell(matrix, limit, i)
            }
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun fillGroup(matrix : Array<Array<Int?>>, group: Int) {
        val col = group % 3
        val row = group / 3

        val rows = (row * 3).rangeUntil((row + 1) * 3)
        val columns = (col * 3).rangeUntil((col + 1) * 3)
        for (rowIndex in rows) {
            for (columnIndex in columns) {
                initCell(matrix = matrix, col = columnIndex, row = rowIndex)
            }
        }
    }

    private fun clearMatrix(matrix : Array<Array<Int?>>) {
        for(col in 0..8)
            for(row in 0..8)
                matrix[col][row] = null
    }

    private fun initCell(matrix : Array<Array<Int?>>, col: Int, row: Int) {

        val candidates = (1..9).toSet()
            .removeFromColumn(matrix, col)
            .removeFromRow(matrix, row)
            .removeFromGroup(matrix = matrix, col = col, row = row)

        matrix[col][row] = candidates.random()
    }

    /**
     * Remove candidates already used in the column
     */
    private fun Set<Int>.removeFromColumn(matrix : Array<Array<Int?>>, col: Int): Set<Int> {
        val alreadyInColumn = (0..8).mapNotNull { matrix[col][it] }
        return this.minus(alreadyInColumn.toSet())
    }

    /**
     * Remove candidates already used in the row
     */
    private fun Set<Int>.removeFromRow(matrix : Array<Array<Int?>>, row: Int): Set<Int> {
        val alreadyInRow = (0..8).mapNotNull { matrix[it][row] }
        return this.minus(alreadyInRow.toSet())
    }

    /**
     * Remove candidates already used in the group
     */
    @OptIn(ExperimentalStdlibApi::class)
    private fun Set<Int>.removeFromGroup(matrix : Array<Array<Int?>>, col: Int, row: Int): Set<Int> {
        val rowGroup = row / 3
        val columnGroup = col / 3

        val alreadyInGroup = mutableSetOf<Int>()
        val rows = (rowGroup * 3).rangeUntil((rowGroup + 1) * 3)
        val columns = (columnGroup * 3).rangeUntil((columnGroup + 1) * 3)
        for (rowIndex in rows) {
            for (columnIndex in columns) {
                matrix[columnIndex][rowIndex]?.let {
                    alreadyInGroup.add(it)
                }
            }
        }

        return this.minus(alreadyInGroup)
    }
}





