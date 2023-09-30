package com.des.sakudoku.board.generator

interface Command<T> {
    val name: String
    fun execute() : () -> T
    fun undo() : () -> Unit

}
