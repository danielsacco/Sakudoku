package com.des.sakudoku.board.generator

object LinearBackTrackGenerator : BoardGenerator {
    override fun generateBoard(): Board = LinearBackTrackGeneratorImpl().generateBoard()
}

private class LinearBackTrackGeneratorImpl : BoardGenerator {

    private val board = Board()

    override fun generateBoard(): Board {
        var currentIndex = 0
        var collisionCount = 0
        var noOptionsCount = 0

        while (currentIndex < 9*9) {

            var validValue = false

            while(!validValue) {
                val cell = board.cells[currentIndex]
                val candidates = cell.options

                if(candidates.isEmpty()) {
                    // Back trace n cells
                    noOptionsCount ++
                    val backIndex = Integer.max((currentIndex - noOptionsCount) + 1, 9)
                    backToCell(currentIndex, backIndex)
                    currentIndex = backIndex
                    continue
                }

                cell.value = candidates.random()
                val collisions = findCollisions(cell)
                if(collisions.isNotEmpty()) {
                    collisionCount++
                    cell.options.remove(cell.value)
                } else {
                    validValue = true

                    // Jump to the next cell
                    currentIndex++
                }
            }
        }
        return board
    }

    private fun findCollisions(cell: Cell) : Set<Cell> {
        return mutableSetOf<Cell>().apply {
            findColumnCollision(cell)?.let {  this.add(it) }
            findRowCollision(cell)?.let {  this.add(it) }
            findGroupCollision(cell)?.let {  this.add(it) }
        }
    }

    private fun findColumnCollision(cell: Cell) : Cell? =
        board.cellsByCol[cell.col]
            ?.filterNot { it == cell }
            ?.firstOrNull { it.value == cell.value }

    private fun findRowCollision(cell: Cell) : Cell? =
        board.cellsByRow[cell.row]
            ?.filterNot { it == cell }
            ?.firstOrNull { it.value == cell.value }

    private fun findGroupCollision(cell: Cell) : Cell? =
        board.cellsByGroup[cell.group]
            ?.filterNot { it == cell }
            ?.firstOrNull { it.value == cell.value }

    private fun backToCell(index: Int, backTo: Int) {
        var currentIndex = index

        while(currentIndex >= backTo) {
            resetCell(board.cells[currentIndex])
            currentIndex--
        }
    }

    private fun resetCell(cell: Cell) {
        cell.value = 0
        (1..9).apply {
            cell.options.addAll(this)
        }
    }
}