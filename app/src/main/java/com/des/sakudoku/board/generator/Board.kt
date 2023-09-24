package com.des.sakudoku.board.generator


class Board {
    val cells = Array(81) {it.toRowCol().createCell()}

    val cellsByCol = cells.groupBy { it.col }
    val cellsByRow = cells.groupBy { it.row }
    val cellsByGroup = cells.groupBy { it.group }

    fun reset() {
        cells.forEach {
            it.value = 0
            it.options.clear()
        }
    }

    fun print() {
        cellsByRow.toSortedMap().forEach {entry ->
            entry.value?.let {
                println(it)
            }
        }
    }

}

data class RowCol(val row: Int, val col: Int)

/**
 * For a 9 X 9 matrix cells indexes from 0 to 80 can be converted to a Row/Col coordinate.
 */
fun Int.toRowCol(): RowCol {
    val col = this % 9
    val row = this / 9
    return RowCol(row, col)
}

fun RowCol.createCell() : Cell = Cell(row = this.row, col = this.col)