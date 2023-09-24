package com.des.sakudoku.board.generator

interface Command {
    val name: String
    fun execute() : () -> Boolean
    fun undo() : () -> Unit

}