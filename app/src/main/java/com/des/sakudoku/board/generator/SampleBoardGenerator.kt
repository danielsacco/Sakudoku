package com.des.sakudoku.board.generator

object SampleBoardGenerator : BoardGenerator {

    private val fixedValues  =  listOf(
        4, 9, 8, 6, 2, 1, 5, 3, 7,
        5, 3, 7, 9, 8, 4, 6, 2, 1,
        2, 1, 6, 7, 5, 3, 8, 9, 4,
        1, 4, 2, 5, 6, 7, 3, 8, 9,
        9, 8, 5, 4, 3, 2, 1, 7, 6,
        7, 6, 3, 8, 1, 9, 2, 4, 5,
        3, 5, 4, 1, 7, 8, 9, 6, 2,
        8, 7, 1, 2, 9, 6, 4, 5, 3,
        6, 2, 9, 3, 4, 5, 7, 1, 8
    )

    override fun generateBoard(): Board {

        // Return a new board instance on each invocation
        return Board().apply {
            cells.forEachIndexed { index, cell ->
                cell.value = fixedValues[index]
            }
        }
    }
}