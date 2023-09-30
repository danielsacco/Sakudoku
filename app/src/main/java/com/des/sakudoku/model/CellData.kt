package com.des.sakudoku.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

sealed class CellData {

    abstract val col: Int

    abstract val row: Int

    abstract fun clear()

    open fun erasable() = false

    var selected: Boolean by mutableStateOf(false)

    /**
     * A fixed cell that exposes the real value it holds. These cells show their value from the
     * beginning fo the game.
     */
    data class FixedCellData(override val col: Int, override val row: Int, val value: Int) : CellData() {
        override fun clear() {
            // Do nothing
        }
    }

    /**
     * A free cell for player input
     */
    data class PlayerCellData(override val col: Int, override val row: Int) : CellData() {

        private var candidates: Set<Int> by mutableStateOf(setOf())

        private var _guess: Int? by mutableStateOf(null)
        val guess: Int?
            get() = _guess

        fun isCandidate(value: Int) = candidates.contains(value)

        fun isEmpty() = candidates.isEmpty()

        fun hasGuess() = _guess != null

        fun toggleOption(value: Int) {
            candidates = if(candidates.contains(value)) {
                candidates.minus(value)
            } else {
                candidates.plus(value)
            }
        }

        fun toggleGuess(value: Int) {
            _guess = if(_guess == value) null else value
        }

        override fun erasable() = candidates.isNotEmpty() || _guess != null

        override fun clear() {
            candidates = emptySet()
            _guess = null
        }

        fun copyStateFrom(other: PlayerCellData) {
            candidates = other.candidates
            _guess = other.guess
        }

        fun deepCopy() : PlayerCellData {
            return this.copy().also {
                it.candidates = mutableSetOf<Int>().apply {
                    addAll(candidates)
                }
                it._guess = _guess
            }
        }
    }
}

