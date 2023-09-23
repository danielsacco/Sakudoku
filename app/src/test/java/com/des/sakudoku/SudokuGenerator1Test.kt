package com.des.sakudoku

import org.junit.Test

class SudokuGenerator1Test {
    private val cells = Array(81) {
        Cell(it.toRowCol())
    }

    private val cellsByCol = cells.groupBy { it.col }

    private val cellsByRow = cells.groupBy { it.row }

    private val cellsByGroup = cells.groupBy { it.group }

    @Test
    fun testLinearFiller() {
        var currentIndex = 0
        var noOptionsCount = 0
        var lastStuckIndex = 0
        var traceBackNumber = 2

        while (currentIndex < 9*9) {

            // Process a cell
            var cellHasAValue = false

            while(!cellHasAValue) {
                val cell = cells[currentIndex]

                val candidates = cell.options

                if(candidates.isEmpty()) {
                    println("Candidates for cell ${cell.row}:${cell.col} are empty.")
                    printRows(cellsByRow)
                    noOptionsCount ++

                    // Back trace n cells: TODO Analyze how many cells should we go back
                    if(lastStuckIndex == currentIndex) {
                        traceBackNumber++
                    } else {
                        lastStuckIndex = currentIndex
                        traceBackNumber = 2
                    }

                    backCells(currentIndex, traceBackNumber)
                    currentIndex -= traceBackNumber
                    continue
                } else {
                    cell.value = candidates.random()
                    cell.options.clear()

                    // Update options by column, row and group
                    cell.updateOptionsInGrid()

                    cellHasAValue = true
                    currentIndex++
                }
            }
        }
        println("Result:")
        printRows(cellsByRow)
        println("No Options count (must go back): $noOptionsCount")
    }

    private fun backCells(current: Int, n: Int) {

        // Set of cols, rows and groups that should be recalculated
        val dirtyRows = mutableSetOf<Int>()
        val dirtyCols = mutableSetOf<Int>()
        val dirtyGroups = mutableSetOf<Int>()

        for (index in (current - 1 downTo current - n)) {
            val cell = cells[index]

            // Clear cell value
            cell.value = 0
            dirtyRows.add(cell.row)
            dirtyCols.add(cell.col)
            dirtyGroups.add(cell.group)
        }

        resetOptions(dirtyRows, cellsByRow)
        resetOptions(dirtyCols, cellsByCol)
        resetOptions(dirtyGroups, cellsByGroup)

        removeInvalidOptions(dirtyRows, cellsByRow)
        removeInvalidOptions(dirtyCols, cellsByCol)
        removeInvalidOptions(dirtyGroups, cellsByGroup)
    }

    private fun resetOptions(dirtyIndexes: Set<Int>, cellsMap: Map<Int, List<Cell>>) {
        dirtyIndexes.map { cellsMap[it] }.forEach { cells ->
            cells?.filter { it.value == 0 }?.forEach {cell ->
                cell.options.addAll((1..9))
            }
        }
    }

    private fun removeInvalidOptions(dirtyIndexes: Set<Int>, cellsMap: Map<Int, List<Cell>>) {
        dirtyIndexes.map { cellsMap[it] }.forEach { cells ->
            val alreadyUsedNumbers = cells?.filter { it.value != 0 }?.map { it.value }?.toSet()
            cells?.filter { it.value == 0 }?.forEach { cell ->
                alreadyUsedNumbers?.let {
                    cell.options.removeAll(it)
                }
            }
        }
    }

    private fun Int.toRowCol(): RowCol {
        val col = this % 9
        val row = this / 9
        return RowCol(row, col)
    }

    data class RowCol(val row: Int, val col: Int)

    /**
     * Cells that start with all possible options set instead of an empty discardedValues set
     */
    private data class Cell (
        val col: Int,
        val row: Int,
        var value: Int = 0,
        val options: MutableSet<Int> = (1..9).toMutableSet(),
        val group: Int = (row / 3) * 3 + (col / 3)
    ) {
        constructor(rowCol: RowCol) : this(row = rowCol.row, col = rowCol.col)

        override fun toString(): String = if (value != 0) value.toString() else " "
    }

    private fun Cell.updateOptionsInGrid() {
        cellsByCol[this.col]?.filterNot { it == this }?.forEach { it.options.remove(this.value) }
        cellsByRow[this.row]?.filterNot { it == this }?.forEach { it.options.remove(this.value) }
        cellsByGroup[this.group]?.filterNot { it == this }?.forEach { it.options.remove(this.value) }
    }

    private fun printRows(rows: Map<Int, List<Cell>>) {
        rows.toSortedMap().forEach {entry ->
            entry.value?.let {
                println(it)
            }
        }
    }
}

