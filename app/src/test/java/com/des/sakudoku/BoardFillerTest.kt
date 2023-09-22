package com.des.sakudoku

import org.junit.Test

class BoardFillerTest {

    private val matrix = Array(9) { Array<Int?>(9) {null} }


    @Test
    fun testFill1() {
        try {
            listOf(0, 4, 8).forEach { freeFillGroup(matrix, it) }
            printMatrix()

            solveRemaining()

        } finally {
            printMatrix()
        }

    }

    private val availableValuesByColumn: Array<MutableSet<Int>> = Array(9) { (1..9).toMutableSet() }
    private val availableValuesByRow: Array<MutableSet<Int>> = Array(9) { (1..9).toMutableSet() }
    private val availableValuesByGroup: Array<MutableSet<Int>> = Array(9) { (1..9).toMutableSet() }
    private val cellCandidatesByNumberOfCandidates: MutableMap<Int, MutableSet<CellCandidates>> = mutableMapOf()
    private val cellCandidates: MutableList<CellCandidates> = mutableListOf()

    private fun solveRemaining() {

        for (col in 0..8) {
            for (row in 0..8) {
                matrix[col][row]?.let { availableValuesByColumn[col].remove(it) }
            }
        }

        for (row in 0..8) {
            for (col in 0..8) {
                matrix[col][row]?.let { availableValuesByRow[row].remove(it) }
            }
        }

        // Calculate available values by group: We assume that groups 0, 4 and 8 are already filled
        availableValuesByGroup[0].clear()
        availableValuesByGroup[4].clear()
        availableValuesByGroup[8].clear()


        // For each unset cell:
        for (col in 0..8) {
            for (row in 0..8) {
                if(matrix[col][row] == null) {
                    // Calculate candidates at that col/row
                    val candidates = availableValuesByColumn[col] intersect availableValuesByRow[row]

                    val cell = CellCandidates(
                        col = col,
                        row = row,
                        groupNumber = getGroupNumber(col = col, row = row),
                        candidates = candidates.toMutableSet()
                    )

                    cellCandidates.add(cell)

                    // Group the cell with others with the same possibilities
                    cellCandidatesByNumberOfCandidates
                        //.getOrDefault(candidates.size, mutableSetOf())
                        .getOrPut(candidates.size) { mutableSetOf() }
                        .add(cell)
                }
            }
        }

        assignValues()
    }

    private fun assignValues() {

        while(cellCandidates.isNotEmpty()) {

            val leastChances = cellCandidatesByNumberOfCandidates.keys.min()

            val candidates = cellCandidatesByNumberOfCandidates[leastChances]
            if(candidates.isNullOrEmpty()) {
                cellCandidatesByNumberOfCandidates.remove(leastChances)
                continue
            }

            val cell = candidates.first()

            // We wont process this cell candidates anymore
            cellCandidatesByNumberOfCandidates[leastChances]!!.remove(cell)
            cellCandidates.remove(cell)

            val value = cell.candidates.random()

            matrix[cell.col][cell.row] = value

            // Update available values at cols, rows, groups. Is it really necessary ?
            val groupNumber = getGroupNumber(cell.col, cell.row)
            //availableValuesByGroup[groupNumber].remove(groupNumber)
            //availableValuesByColumn[cell.col].remove(value)
            //availableValuesByRow[cell.row].remove(value)

            removeCandidatesFromCells(cell.col, cell.row, groupNumber, value)
        }
    }

    private fun removeCandidatesFromCells(col: Int, row: Int, groupNumber: Int, value: Int) {
        cellCandidates.filter { it.row == row }
            .forEach {updateCandidatesForCell(it, value)}

        cellCandidates.filter { it.col == col }
            .forEach {updateCandidatesForCell(it, value)}

        cellCandidates.filter { it.groupNumber == groupNumber }
            .forEach {updateCandidatesForCell(it, value)}
    }

    private fun updateCandidatesForCell(cell: CellCandidates, value: Int) {
        cellCandidatesByNumberOfCandidates[cell.candidates.size]!!.remove(cell)
        cell.candidates.remove(value)
        cellCandidatesByNumberOfCandidates.getOrPut(cell.candidates.size) { mutableSetOf() }.add(cell)
    }

    private fun getGroupNumber(col: Int, row: Int): Int {
        val rowGroup = row / 3
        val columnGroup = col / 3

        return rowGroup * 3 + columnGroup
    }

    data class CellCandidates(
        val col: Int,
        val row: Int,
        val groupNumber: Int,
        val candidates: MutableSet<Int> = mutableSetOf()
    )



    @OptIn(ExperimentalStdlibApi::class)
    private fun freeFillGroup(matrix : Array<Array<Int?>>, group: Int) {
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


    private fun printMatrix() {
        for(row in 0..8)
            println(matrix[row].contentToString())
    }
}