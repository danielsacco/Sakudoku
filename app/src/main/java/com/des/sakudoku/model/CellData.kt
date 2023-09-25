package com.des.sakudoku.model

sealed class CellData {

    abstract val col: Int

    abstract val row: Int

    var selected = false

    /**
     * A cell that exposes the real value it holds. These cells show their value from the
     * beginning fo the game.
     */
    data class FixedCell(override val col: Int, override val row: Int, val value: Int) : CellData()

    /**
     * A cell filled with a number guessed by the player
     */
    data class CellGuess(override val col: Int, override val row: Int, val value: Int) : CellData()

    /**
     * A Cell showing the candidates guessed by the player or calculated by software
     */
    class CellCandidates(override val col: Int, override val row: Int, private val options: Set<Int> = emptySet()) : CellData() {
        fun isCandidate(value: Int) = options.contains(value)

        fun isEmpty() = options.isEmpty()
    }
}

