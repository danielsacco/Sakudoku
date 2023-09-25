package com.des.sakudoku.board.generator

object FixedMaskGenerator :MaskGenerator {

    private val fixedMask  =  listOf(
        false, false,  true,  true, false,  true, false, false,  true,
        false, false, false, false,  true, false, false, false,  true,
         true, false,  true,  true, false, false,  true, false,  true,
        false, false,  true,  true,  true, false,  true,  true, false,
         true, false,  true,  true, false, false, false,  true, false,
         true, false,  true,  true,  true, false,  true, false,  true,
        false,  true,  true, false,  true,  true,  true,  true,  true,
         true, false,  true,  true,  true, false, false, false, false,
        false, false, false, false, false, false, false,  true, false,
    )

    override fun generateMask(): List<Boolean> = fixedMask
}