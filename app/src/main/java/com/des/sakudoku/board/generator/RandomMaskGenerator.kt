package com.des.sakudoku.board.generator

object RandomMaskGenerator : MaskGenerator {
    private const val numberOfShownCells = 38
    private const val totalCells = 81

    override fun generateMask(): List<Boolean> {
        val mask = MutableList(numberOfShownCells) {true}
        mask.addAll(MutableList(totalCells - numberOfShownCells) {false})
        return mask.shuffled()
    }
}