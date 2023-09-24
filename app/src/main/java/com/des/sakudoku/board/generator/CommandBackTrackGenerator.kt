package com.des.sakudoku.board.generator

class CommandBackTrackGenerator : BoardGenerator {

    private val board = Board()

    private val usedValuesByRow = mutableMapOf<Int, Set<Int>>()
    private val usedValuesByCol = mutableMapOf<Int, Set<Int>>()

    private interface Command {
        val name: String
        fun execute() : () -> Boolean
        fun undo() : () -> Unit
    }

    override fun generateBoard(): Board {
        val fillDiagonalGroups = object : Command {
            override val name = "Random Fill Diagonals."
            override fun execute() = ::randomFillDiagonalGroups
            override fun undo() = board::reset
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

        val fillRemainingCellsCommand = object : Command {
            override val name = "Fill remaining cells."
            var cellsSnapshot: MutableList<Cell> = mutableListOf()

            private fun saveSnapshot() {
                board.cells.map { cell ->
                    cell.copy(options = mutableSetOf<Int>().also { it.addAll(cell.options) })
                }.apply { cellsSnapshot.addAll(this) }
            }
            override fun execute(): () -> Boolean = {
                saveSnapshot()
                fillRemainingCellsInBoard()
            }

            override fun undo(): () -> Unit = {
                for ((index, cell) in board.cells.withIndex()) {
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

                //println("""Command "${command.name}" executed successfully.""")
                //printRows(board.cellsByRow)
            } else if(currentCommandRetries == 2) {
                // Step back to the previous command
                //println("""Command "${command.name}" failed $currentCommandRetries times. """ +
                //        "Undoing it.")
                commands[commandIndex].undo()()

                commandIndex--
                commands[commandIndex].undo()()
                //println("Stepping back to the previous command: " +
                //        "\"${commands[commandIndex].name}\". It will also be undone.")

                currentCommandRetries = 0
            } else {
                currentCommandRetries++
                //retriesLog.add(Pair(commandIndex, command.name))
                //println("Command \"${command.name}\" failed $currentCommandRetries times. " +
                //        "Retrying...")
            }
        }
        return board
    }

    private fun applyCandidatesToWholeGrid() : Boolean {
        board.cells.filter { it.value == 0 }.forEach {cell ->
            val usedValuesIntersection = usedValuesByRow[cell.row]!!.union(usedValuesByCol[cell.col]!!)
            cell.options.removeAll(usedValuesIntersection)
        }
        return true
    }

    private fun fillGroup(group: Int): Boolean {
        var groupSolved = false
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
        // Prepare state snapshot in undo stack in case we cannot solve the group
        var boardSolved = false
        try {

            // Create a set with the remaining cells in the whole board and process them all
            val remainingCells = board.cells.filter { it.value == 0 }.toMutableSet()
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
            board.cellsByRow[row]
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
            board.cellsByCol[col]
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
            board.cellsByGroup[group]?.forEach {cell ->
                options.random().apply {
                    cell.value = this
                    options.remove(this)
                }
            }
        }
        return true
    }

    private fun Cell.updateOptionsInGrid() {
        board.cellsByCol[this.col]
            ?.filter { it.options.isNotEmpty() }
            ?.filterNot { it == this }
            ?.forEach { cell ->
                cell.options.remove(this.value)
                if(cell.options.isEmpty()) throw NoSuchElementException("You left cell ${cell.row}:${cell.col} with no options.")
            }
        board.cellsByRow[this.row]
            ?.filter { it.options.isNotEmpty() }
            ?.filterNot { it == this }
            ?.forEach { cell ->
                cell.options.remove(this.value)
                if(cell.options.isEmpty()) throw NoSuchElementException("You left cell ${cell.row}:${cell.col} with no options.")
            }
        board.cellsByGroup[this.group]
            ?.filter { it.options.isNotEmpty() }
            ?.filterNot { it == this }
            ?.forEach { cell ->
                cell.options.remove(this.value)
                if(cell.options.isEmpty()) throw NoSuchElementException("You left cell ${cell.row}:${cell.col} with no options.")
            }
    }

}