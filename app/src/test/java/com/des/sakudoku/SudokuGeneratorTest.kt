package com.des.sakudoku

import org.junit.Assert.assertEquals
import org.junit.Test
import java.lang.Integer.max

class SudokuGeneratorTest {

    private data class Cell (
        val col: Int,
        val row: Int,
        var value: Int = 0,
        val discardedValues: MutableSet<Int> = mutableSetOf(),
        val group: Int = (row / 3) * 3 + (col / 3)
    ) {
        constructor(rowCol: RowCol) : this(row = rowCol.row, col = rowCol.col)

        override fun toString(): String = if (value != 0) value.toString() else " "
    }

    private val cells = Array(81) {
        Cell(it.toRowCol())
    }

    private val cellsByCol = cells.groupBy { it.col }

    private val cellsByRow = cells.groupBy { it.row }

    private val cellsByGroup = cells.groupBy { it.group }

    private fun printRows(rows: Map<Int, List<Cell>>) {
        rows.toSortedMap().forEach {entry ->
            entry.value?.let {
                println(it)
            }
        }
    }

    @Test
    fun testLinearFiller() {
        var currentIndex = 0
        var collisionCount = 0
        var noOptionsCount = 0

        while (currentIndex < 9*9) {

            // Process a cell
            var validValue = false

            while(!validValue) {
                val cell = cells[currentIndex]
                val candidates = (1..9).filterNot { cell.discardedValues.contains(it) }

                if(candidates.isEmpty()) {
                    // Back trace n cells
                    noOptionsCount ++
                    val backIndex = max((currentIndex - noOptionsCount) + 1, 9)
                    backToCell(currentIndex, backIndex)
                    currentIndex = backIndex
                    continue
                }

                cell.value = candidates.random()
                val collisions = findCollisions(cell)
                if(collisions.isNotEmpty()) {
                    collisionCount++
                    cell.discardedValues.add(cell.value)
                } else {
                    validValue = true

                    // Jump to the next cell
                    currentIndex++
                }
            }

        }

        println("Result:")
        printRows(cellsByRow)
        println("Collisions count: $collisionCount")
        println("No Options count (must go back): $noOptionsCount")

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

    private fun backToCell(index: Int, backTo: Int) {
        var currentIndex = index

        while(currentIndex >= backTo) {
            resetCell(cells[currentIndex])
            currentIndex--
        }
    }

    private fun resetCell(cell: Cell) {
        cell.value = 0;
        cell.discardedValues.clear()
    }

    @Suppress()
    private fun printCollisions(cell: Cell, collisions: Set<Cell>) {
        println("Value ${cell.value} at ${cell.row}:${cell.col} conflicts with:")
        for (collision in collisions)
            println("Cell at ${collision.row}:${collision.col}")
    }

    private fun findCollisions(cell: Cell) : Set<Cell> {
        return mutableSetOf<Cell>().apply {
            findColumnCollision(cell)?.let {  this.add(it) }
            findRowCollision(cell)?.let {  this.add(it) }
            findGroupCollision(cell)?.let {  this.add(it) }
        }
    }

    private fun findColumnCollision(cell: Cell) : Cell? =
        cellsByCol[cell.col]?.filterNot { it == cell }?.firstOrNull { it.value == cell.value }

    private fun findRowCollision(cell: Cell) : Cell? =
        cellsByRow[cell.row]?.filterNot { it == cell }?.firstOrNull { it.value == cell.value }

    private fun findGroupCollision(cell: Cell) : Cell? =
        cellsByGroup[cell.group]?.filterNot { it == cell }?.firstOrNull { it.value == cell.value }



    @Test
    fun testCoordinatesPositioning() {

        val cell0x2 = cells[2].also { it.value = 5 }
        val cell3x1 = cells[28].also { it.value = 7 }

        assertEquals(cell0x2.col, 2)
        assertEquals(cell0x2.row, 0)
        assertEquals(cell0x2.group, 0)
        assertEquals(cell0x2.value, 5)

        assertEquals(cell3x1.col, 1)
        assertEquals(cell3x1.row, 3)
        assertEquals(cell3x1.group, 3)
        assertEquals(cell3x1.value, 7)

        printRows(cellsByRow)
    }

    @Test
    fun testGroupCalculator() {
        assertEquals(Cell(col = 0, row = 0).group, 0)
        assertEquals(Cell(col = 1, row = 0).group, 0)
        assertEquals(Cell(col = 2, row = 0).group, 0)
        assertEquals(Cell(col = 3, row = 0).group, 1)
        assertEquals(Cell(27.toRowCol()).group, 3)
        assertEquals(Cell(50.toRowCol()).group, 4)
        assertEquals(Cell(51.toRowCol()).group, 5)
        assertEquals(Cell(80.toRowCol()).group, 8)
    }

    @Test
    fun testRowColCalculator() {
        assertEquals(RowCol(0,0), 0.toRowCol())
        assertEquals(RowCol(0,1), 1.toRowCol())
        assertEquals(RowCol(2,3), 21.toRowCol())
        assertEquals(RowCol(8,8), 80.toRowCol())
    }

    private fun Int.toRowCol(): RowCol {
        val col = this % 9
        val row = this / 9
        return RowCol(row, col)
    }

    data class RowCol(val row: Int, val col: Int)

    private fun printGrid(grid: Array<Array<Cell>>) {
        for(row in 0..8)
            println(grid[row].contentToString())
    }
}




