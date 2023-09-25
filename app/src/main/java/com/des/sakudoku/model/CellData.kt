package com.des.sakudoku.model

sealed class CellData {

    /**
     * A cell that exposes the real value is holds. These cells show their value from the
     * beginning fo the game.
     */
    data class FixedCell(val value: Int) : CellData()

    /**
     * A cell filled with a number guessed by the player
     */
    data class CellGuess(val value: Int) : CellData()

    /**
     * A Cell showing the candidates guessed by the player or calculated by software
     */
    class CellCandidates(private val options: Set<Int> = emptySet()) : CellData() {
        fun isCandidate(value: Int) = options.contains(value)

        fun isEmpty() = options.isEmpty()
    }
}

