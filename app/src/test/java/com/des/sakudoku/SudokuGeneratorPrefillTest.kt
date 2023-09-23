package com.des.sakudoku

import org.junit.Assert.fail
import org.junit.Test

class SudokuGeneratorPrefillTest {
    private val cells = Array(81) {
        Cell(it.toRowCol())
    }

    private interface Command {
        fun execute(method: () -> Boolean) : Boolean

        fun undo(method: () -> Unit) : Unit
    }

    private val commands = mutableListOf<Command>()

    private val undoStack = ArrayDeque<() -> Unit>()

    private val cellsByCol = cells.groupBy { it.col }

    private val cellsByRow = cells.groupBy { it.row }

    private val cellsByGroup = cells.groupBy { it.group }

    private val usedValuesByRow = mutableMapOf<Int, Set<Int>>()
    private val usedValuesByCol = mutableMapOf<Int, Set<Int>>()

    private fun resetBoard() {
        cells.forEach {
            it.value = 0
            it.options.clear()
        }
    }

    @Test
    fun testPrefill() {
        // Prefill groups 0, 4 and 8
        //randomFillDiagonalGroups()    // Should use this method
        fixSamplesFillDiagonalGroups()  // Just for testing purposes

        val fillDiagonalsWithFixedSamples = object : Command {
            override fun execute(method: () -> Boolean) = fixSamplesFillDiagonalGroups()
            override fun undo(method: () -> Unit) {
                resetBoard()
            }
        }

        val fillDiagonalGroups = object : Command {
            override fun execute(method: () -> Boolean) = randomFillDiagonalGroups()
            override fun undo(method: () -> Unit) {
                resetBoard()
            }
        }

        commands.add(fillDiagonalsWithFixedSamples)

        val calculateCandidates = object : Command {
            override fun execute(method: () -> Boolean) : Boolean {
                calculateUsedValuesByRow()
                calculateUsedValuesByCol()
                applyCandidatesToWholeGrid()
                return true
            }
            override fun undo(method: () -> Unit) {
                // Do nothing
            }
        }

        commands.add(calculateCandidates)

        val fillGroup2 = object : Command {
            override fun execute(method: () -> Boolean): Boolean {
                TODO("Not yet implemented")
            }

            override fun undo(method: () -> Unit) {
                TODO("Not yet implemented")
            }
        }

        // Here we have at least 6 options per cell, try to fill a single group
        while(!fillGroup(1)) {
            println("Could not solve group 1, retrying.")
        }
        println("Filled group 1.")
        printRows(cellsByRow)

        // Groups 3, 5 and 6 still have at least 6 options per cell
        while(!fillGroup(3)) {
            println("Could not solve group 3, retrying.")
        }
        println("Filled group 3.")
        printRows(cellsByRow)

        // At this point we may have several cells that have only one option,
        // try to solve the whole board ordered by the lowest options cells
        var retryCount = 0
        while (!fillRemainingCellsInBoard()) {
            println("Could not fill the whole board, retrying.")
            retryCount++
            if(retryCount == 10) fail("Could not fill the whole board, retried $retryCount times.")
        }
        printRows(cellsByRow)
        println("Board filled.")

    }

    private fun applyCandidatesToWholeGrid() {
        cells.filter { it.value == 0 }.forEach {cell ->
            val usedValuesIntersection = usedValuesByRow[cell.row]!!.union(usedValuesByCol[cell.col]!!)
            cell.options.removeAll(usedValuesIntersection)
        }

    }

    private fun fillGroup(group: Int): Boolean {
        // Prepare state snapshot in undo stack in case we cannot solve the group
        saveSnapshotInUndoStack()

        var groupSolved = false
        try {

            // Create a set of cells to be filled and process them all
            val remainingCells = cellsByGroup[group]!!.toMutableSet()
            while(remainingCells.isNotEmpty()) {
                // Create a map sorted by number of options in each remaining cell
                val cellsByOptionsSize = remainingCells.groupBy { it.options.size }.toSortedMap()

                // Fill a random cell from the ones with lower options
                cellsByOptionsSize[cellsByOptionsSize.firstKey()]!!.random().let {cell ->
                    cell.value = cell.options.random()
                    cell.options.clear()
                    cell.updateOptionsInGrid()
                    remainingCells.remove(cell)
                }
            }
            groupSolved = true
        } catch (e: NoSuchElementException)  {
            // Restore state before trying to solve group
            undoStack.last()()
//        } finally {
//            undoStack.removeLast()
        }

        return groupSolved
    }

    private fun fillRemainingCellsInBoard(): Boolean {
        // Prepare state snapshot in undo stack in case we cannot solve the group
        saveSnapshotInUndoStack()

        var groupSolved = false
        try {

            // Create a set with the remaining cells in the whole board and process them all
            val remainingCells = cells.filter { it.value == 0 }.toMutableSet()
            while(remainingCells.isNotEmpty()) {
                // Create a map sorted by number of options in each remaining cell
                val cellsByOptionsSize = remainingCells.groupBy { it.options.size }.toSortedMap()

                // Fill a random cell from the ones with lower options
                cellsByOptionsSize[cellsByOptionsSize.firstKey()]!!.random().let {cell ->
                    cell.value = cell.options.random()
                    cell.options.clear()
                    cell.updateOptionsInGrid()
                    remainingCells.remove(cell)
                }
            }
            groupSolved = true
        } catch (e: NoSuchElementException)  {
            undoStack.last()()
        }

        return groupSolved
    }

    private fun saveSnapshotInUndoStack() {
        val cellsCopy = cells.map { cell ->
            cell.copy(options = mutableSetOf<Int>().also { it.addAll(cell.options) })
        }
        undoStack.add {
            for ((index, cell) in cells.withIndex()) {
                cell.value = cellsCopy[index].value
                cell.options.clear()
                cell.options.addAll(cellsCopy[index].options)
            }
        }
    }

    private fun calculateUsedValuesByRow() {
        for (row in (0..8)) {
            cellsByRow[row]
                ?.filter { it.value != 0 }
                ?.map { it.value }
                ?.toSet()
                ?.let {
                    usedValuesByRow.put(row, it)
            }
        }
    }

    private fun calculateUsedValuesByCol() {
        for (col in (0..8)) {
            cellsByCol[col]
                ?.filter { it.value != 0 }
                ?.map { it.value }
                ?.toSet()
                ?.let {
                    usedValuesByCol.put(col, it)
                }
        }
    }

    private fun calculateCandidatesForGroup(group: Int) {
        cellsByGroup[group]?.forEach {cell ->
            usedValuesByRow[cell.row]?.let { rowCandidates ->
                usedValuesByCol[cell.col]?.let { colCandidates ->
                    cell.options.addAll(rowCandidates intersect colCandidates)
                }
            }
        }
    }

    private fun randomFillDiagonalGroups() : Boolean {
        listOf(0, 4, 8).forEach {group ->
            val options = (1..9).toMutableSet()
            cellsByGroup[group]?.forEach {cell ->
                options.random().apply {
                    cell.value = this
                    options.remove(this)
                }
            }
        }
        return true
    }

    private fun fixSamplesFillDiagonalGroups() : Boolean {

        val fixedValues0 = listOf(2, 3, 4, 6, 8, 7, 5, 9, 1)
        for ((index, cell) in cellsByGroup[0]!!.withIndex()) {
            cell.value = fixedValues0[index]
        }

        val fixedValues4 = listOf(4, 2, 9, 8, 3, 5, 1, 7, 6)
        for ((index, cell) in cellsByGroup[4]!!.withIndex()) {
            cell.value = fixedValues4[index]
        }

        val fixedValues8 = listOf(4, 8, 9, 6, 3, 2, 5, 7, 1)
        for ((index, cell) in cellsByGroup[8]!!.withIndex()) {
            cell.value = fixedValues8[index]
        }

        return true
    }

    @Test
    fun testLinearFiller() {    // It fails !!!
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

        override fun equals(other: Any?): Boolean {
            return this === other ||
                    (other is Cell && this.col == other.col && this.row == other.row)
        }

        override fun hashCode(): Int {
            var result = col
            result = 31 * result + row
            return result
        }
    }

    private fun Cell.updateOptionsInGrid() {
        cellsByCol[this.col]
            ?.filter { it.options.isNotEmpty() }
            ?.filterNot { it == this }
            ?.forEach { cell ->
                cell.options.remove(this.value)
                if(cell.options.isEmpty()) throw NoSuchElementException("You left cell ${cell.row}:${cell.col} with no options.")
        }
        cellsByRow[this.row]
            ?.filter { it.options.isNotEmpty() }
            ?.filterNot { it == this }
            ?.forEach { cell ->
                cell.options.remove(this.value)
                if(cell.options.isEmpty()) throw NoSuchElementException("You left cell ${cell.row}:${cell.col} with no options.")
        }
        cellsByGroup[this.group]
            ?.filter { it.options.isNotEmpty() }
            ?.filterNot { it == this }
            ?.forEach { cell ->
                cell.options.remove(this.value)
                if(cell.options.isEmpty()) throw NoSuchElementException("You left cell ${cell.row}:${cell.col} with no options.")
        }
    }

    private fun printRows(rows: Map<Int, List<Cell>>) {
        println("Grid.")
        rows.toSortedMap().forEach {entry ->
            entry.value?.let {
                println(it)
            }
        }
    }
}

