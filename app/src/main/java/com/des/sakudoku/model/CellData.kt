package com.des.sakudoku.model

sealed class CellData {

    /**
     * A cell that exposes the real value is holds. These cells show their value from the
     * beginning fo the game.
     */
    data class ExposedCell(val value: Int) : CellData()

    data class CellValue(val value: Int) : CellData()

    class CellCandidates(private val options: Set<Int> = emptySet()) : CellData() {
        fun isCandidate(value: Int) = options.contains(value)

        fun isEmpty() = options.isEmpty()
    }
}

