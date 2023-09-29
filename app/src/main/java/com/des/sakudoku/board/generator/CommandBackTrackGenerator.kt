package com.des.sakudoku.board.generator

import com.des.sakudoku.board.generator.CommandBackTrackGenerator.unsolvableCount


object CommandBackTrackGenerator : BoardGenerator  {
    override fun generateBoard(): Board = CommandBackTrackGeneratorImpl().generateBoard()

    var unsolvableCount = 0L
}

private class CommandBackTrackGeneratorImpl : BoardGenerator {

    private val board = Board()

    private val usedValuesByRow = mutableMapOf<Int, Set<Int>>()
    private val usedValuesByCol = mutableMapOf<Int, Set<Int>>()

    fun takeSnapshot() : List<Cell> = board.cells.map { cell ->
        cell.copy(options = mutableSetOf<Int>().also {
            it.addAll(cell.options)
        })
    }

    override fun generateBoard(): Board {

        class FillGroupCommand(private val groupNumber: Int) : Command<Boolean> {
            override val name = "Fill group $groupNumber."
            var cellsSnapshot: MutableList<Cell> = mutableListOf()

            private fun saveSnapshot() {
                board.cells.map { cell ->
                    cell.copy(options = mutableSetOf<Int>().also { it.addAll(cell.options) })
                }.apply { cellsSnapshot.addAll(this) }
            }
            override fun execute(): () -> Boolean = {
                saveSnapshot()
                fillGroup(groupNumber)
            }

            override fun undo(): () -> Unit = {
                for ((index, cell) in board.cells.withIndex()) {
                    cell.value = cellsSnapshot[index].value
                    cell.options.clear()
                    cell.options.addAll(cellsSnapshot[index].options)
                }
            }
        }

        val fillRemainingCellsCommand = object : Command<Boolean> {
            override val name = "Fill remaining cells."
            private var cellsSnapshot: List<Cell>? = null

            override fun execute(): () -> Boolean = {
                cellsSnapshot = takeSnapshot()
                fillRemainingCellsInBoard()
            }

            override fun undo(): () -> Unit = {
                for ((index, cell) in board.cells.withIndex()) {
                    cellsSnapshot?.get(index)?.let {
                        cell.value = it.value
                        cell.options.clear()
                        cell.options.addAll(it.options)
                    }
                }
            }
        }

        randomFillIndependentGroup(listOf(0, 4, 8))
        calculateUsedValuesByCol()
        calculateUsedValuesByRow()
        updateOptionsForUnsetCells()

        val commands = mutableListOf (
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
            } else if(currentCommandRetries == 2) {
                // Step back to the previous command
                commands[commandIndex].undo()()
                if(commandIndex > 0) {
                    commandIndex--
                    commands[commandIndex].undo()()
                }
                currentCommandRetries = 0
            } else {
                currentCommandRetries++
            }
        }
        return board
    }

    private fun updateOptionsForUnsetCells() {
        board.cells.filter { it.value == 0 }.forEach {cell ->
            val usedValuesIntersection = usedValuesByRow[cell.row]!!.union(usedValuesByCol[cell.col]!!)
            cell.options.removeAll(usedValuesIntersection)
        }
    }

    private fun fillGroup(group: Int): Boolean {
        var groupSolved: Boolean
        try {

            // Create a set of cells to be filled and process them all
            val remainingCells = board.cellsByGroup[group]!!.toMutableSet()
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
        var boardSolved: Boolean
        try {

            // Create a set with the remaining cells in the whole board and process them all
            val remainingCells = board.cells.filter { it.value == 0 }.toMutableSet()
            while(remainingCells.isNotEmpty()) {
                // Create a map sorted by number of options in each remaining cell
                val cellsByOptionsSize = remainingCells.groupBy { it.options.size }.toSortedMap()

                // Fill a random cell from the ones with less possibilities of being filled
                cellsByOptionsSize[cellsByOptionsSize.firstKey()]!!.random().let {cell ->
                    cell.value = cell.options.random()
                    cell.options.clear()
                    cell.updateOptionsInGrid()
                    remainingCells.remove(cell)
                }
            }
            boardSolved = true
        } catch (e: NoSuchElementException)  {
            // Do nothing, command processor will undo and retry
            boardSolved = false
        }

        return boardSolved
    }

    private fun calculateUsedValuesByRow() {
        board.cellsByRow.forEach { (row, cells) ->
            cells
                .filter { it.value != 0 }
                .map { it.value }
                .toSet()
                .apply { usedValuesByRow[row] = this }
        }
    }

    private fun calculateUsedValuesByCol() {
        board.cellsByCol.forEach { (col, cells) ->
            cells
                .filter { it.value != 0 }
                .map { it.value }
                .toSet()
                .apply { usedValuesByCol[col] = this }
        }
    }

    /**
     * Randomly fill independent groups of cells.
     * Independent groups are those that not share columns and rows so we can fill each one
     * without affecting the others.
     * For example: groups 0, 4, 8 do not share rows or cols.
     * Other options are (2, 4, 6); (0, 5, 7) etc.
     */
    private fun randomFillIndependentGroup(groups: List<Int>) {
        groups.forEach {group ->

            val options = (1..9).toMutableSet()

            board.cellsByGroup[group]!!.forEach { cell ->
                options.random().apply {
                    cell.value = this
                    options.remove(this)
                }
            }
        }
    }

    private fun Cell.updateOptionsInGrid() {
        board.cellsByCol[this.col]!!
            .filter { it.options.isNotEmpty() }
            .filterNot { it == this }
            .forEach { cell ->
                cell.options.remove(this.value)
                unsolvableCount++  // DEBUG
                if(cell.options.isEmpty()) throw NoSuchElementException("You left cell ${cell.row}:${cell.col} with no options.")
            }
        board.cellsByRow[this.row]!!
            .filter { it.options.isNotEmpty() }
            .filterNot { it == this }
            .forEach { cell ->
                cell.options.remove(this.value)
                unsolvableCount++  // DEBUG
                if(cell.options.isEmpty()) throw NoSuchElementException("You left cell ${cell.row}:${cell.col} with no options.")
            }
        board.cellsByGroup[this.group]!!
            .filter { it.options.isNotEmpty() }
            .filterNot { it == this }
            .forEach { cell ->
                cell.options.remove(this.value)
                unsolvableCount++  // DEBUG
                if(cell.options.isEmpty()) throw NoSuchElementException("You left cell ${cell.row}:${cell.col} with no options.")
            }
    }

}