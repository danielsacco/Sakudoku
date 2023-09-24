package com.des.sakudoku.board.generator

data class Cell (
    val col: Int,
    val row: Int,
    var value: Int = 0,
    val options: MutableSet<Int> = (1..9).toMutableSet(),
    val group: Int = (row / 3) * 3 + (col / 3)
) {
    //constructor(rowCol: RowCol) : this(row = rowCol.row, col = rowCol.col)

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
