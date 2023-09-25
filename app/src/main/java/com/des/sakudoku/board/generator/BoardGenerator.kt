package com.des.sakudoku.board.generator

interface BoardGenerator {

    fun generateBoard() : Board
}

interface MaskGenerator {

    /**
     * Mask of cells to be exposed as initial guidance to the player.
     */
    fun generateMask() : List<Boolean>
}