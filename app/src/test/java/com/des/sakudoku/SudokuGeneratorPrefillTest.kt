package com.des.sakudoku

import org.junit.Assert.assertEquals
import org.junit.Test

class SudokuGeneratorPrefillTest {
    private val cells = Array(81) {
        Cell(it.toRowCol())
    }

    private interface Command {
        val name: String
        fun execute() : () -> Boolean
        fun undo() : () -> Unit
    }

    private val cellsByCol = cells.groupBy { it.col }
    private val cellsByRow = cells.groupBy { it.row }
    private val cellsByGroup = cells.groupBy { it.group }

    private val usedValuesByRow = mutableMapOf<Int, Set<Int>>()
    private val usedValuesByCol = mutableMapOf<Int, Set<Int>>()

    fun resetBoard() {
        cells.forEach {
            it.value = 0
            it.options.clear()
        }
    }

    @Test
    fun testPrefill() {
        val fillDiagonalGroups = object : Command {
            override val name = "Random Fill Diagonals."
            override fun execute() = ::randomFillDiagonalGroups
            override fun undo() = ::resetBoard
        }

        val calculateUsedValuesByColCommand = object : Command {
            override val name = "Calculated already used values by columns."
            override fun execute() = ::calculateUsedValuesByCol
            override fun undo(): () -> Unit  = { }
        }

        val calculateUsedValuesByRowCommand = object : Command {
            override val name = "Calculated already used values by rows."
            override fun execute() = ::calculateUsedValuesByRow
            override fun undo(): () -> Unit = {}
        }

        val applyCandidatesToWholeGridCommand = object : Command {
            override val name = "Apply candidates to all the unfilled cells."
            override fun execute() = ::applyCandidatesToWholeGrid
            override fun undo(): () -> Unit = {}
        }

        class FillGroupCommand(private val groupNumber: Int) : Command {
            override val name = "Fill group $groupNumber."
            var cellsSnapshot: MutableList<Cell> = mutableListOf()

            private fun saveSnapshot() {
                cells.map { cell ->
                    cell.copy(options = mutableSetOf<Int>().also { it.addAll(cell.options) })
                }.apply { cellsSnapshot.addAll(this) }
            }
            override fun execute(): () -> Boolean = {
                saveSnapshot()
                fillGroup(groupNumber)
            }

            override fun undo(): () -> Unit = {
                for ((index, cell) in cells.withIndex()) {
                    cell.value = cellsSnapshot[index].value
                    cell.options.clear()
                    cell.options.addAll(cellsSnapshot[index].options)
                }
            }
        }

        val fillRemainingCellsCommand = object : Command {
            override val name = "Fill remaining cells."
            var cellsSnapshot: MutableList<Cell> = mutableListOf()

            private fun saveSnapshot() {
                cells.map { cell ->
                    cell.copy(options = mutableSetOf<Int>().also { it.addAll(cell.options) })
                }.apply { cellsSnapshot.addAll(this) }
            }
            override fun execute(): () -> Boolean = {
                saveSnapshot()
                fillRemainingCellsInBoard()
            }

            override fun undo(): () -> Unit = {
                for ((index, cell) in cells.withIndex()) {
                    cell.value = cellsSnapshot[index].value
                    cell.options.clear()
                    cell.options.addAll(cellsSnapshot[index].options)
                }
            }
        }

        val commands = mutableListOf<Command>(
            fillDiagonalGroups,
            calculateUsedValuesByColCommand,
            calculateUsedValuesByRowCommand,
            applyCandidatesToWholeGridCommand,
            FillGroupCommand(2),
            FillGroupCommand(6),
            fillRemainingCellsCommand
        )

        // We cannot use a classic for loop cause we may need to step backwards
        var commandIndex = 0
        var currentCommandRetries = 0

        while(commandIndex < commands.size) {

            val command = commands[commandIndex]
            if(command.execute()()) {
                commandIndex++
                currentCommandRetries = 0

                println("""Command "${command.name}" executed successfully.""")
                printRows(cellsByRow)
            } else if(currentCommandRetries == 2) {
                // Step back to the previous command
                println("""Command "${command.name}" failed $currentCommandRetries times. """ +
                        "Undoing it.")
                commands[commandIndex].undo()()

                commandIndex--
                commands[commandIndex].undo()()
                println("Stepping back to the previous command: " +
                        "\"${commands[commandIndex].name}\". It will also be undone.")

                currentCommandRetries = 0
            } else {
                currentCommandRetries++
                println("Command \"${command.name}\" failed $currentCommandRetries times. " +
                        "Retrying...")
            }
        }

        cellsByCol.forEach {
            val cells = it.value
            assertEquals(cells.distinctBy { cell -> cell.value }.size, 9)
        }
        cellsByRow.forEach {
            val cells = it.value
            assertEquals(cells.distinctBy { cell -> cell.value }.size, 9)
        }
        cellsByGroup.forEach {
            val cells = it.value
            assertEquals(cells.distinctBy { cell -> cell.value }.size, 9)
        }

    }

    private fun applyCandidatesToWholeGrid() : Boolean {
        cells.filter { it.value == 0 }.forEach {cell ->
            val usedValuesIntersection = usedValuesByRow[cell.row]!!.union(usedValuesByCol[cell.col]!!)
            cell.options.removeAll(usedValuesIntersection)
        }
        return true
    }

    private fun fillGroup(group: Int): Boolean {
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
        } catch (_: NoSuchElementException)  {
            // Do nothing, command processor will undo and retry
            groupSolved = false
        }

        return groupSolved
    }

    private fun fillRemainingCellsInBoard(): Boolean {
        // Prepare state snapshot in undo stack in case we cannot solve the group
        var boardSolved = false
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
            boardSolved = true
        } catch (e: NoSuchElementException)  {
            //undoStack.last()()
        }

        return boardSolved
    }

    private fun calculateUsedValuesByRow() : Boolean {
        for (row in (0..8)) {
            cellsByRow[row]
                ?.filter { it.value != 0 }
                ?.map { it.value }
                ?.toSet()
                ?.let {
                    usedValuesByRow.put(row, it)
            }
        }
        return true
    }

    private fun calculateUsedValuesByCol() : Boolean {
        for (col in (0..8)) {
            cellsByCol[col]
                ?.filter { it.value != 0 }
                ?.map { it.value }
                ?.toSet()
                ?.let {
                    usedValuesByCol.put(col, it)
                }
        }
        return true
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

