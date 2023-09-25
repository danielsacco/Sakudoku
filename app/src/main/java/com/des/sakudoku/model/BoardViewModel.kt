package com.des.sakudoku.model


import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import com.des.sakudoku.board.generator.FixedMaskGenerator
import com.des.sakudoku.board.generator.SampleBoardGenerator

class BoardViewModel : ViewModel() {

    private val _cells = BoardState().cells.toMutableStateList()
    val cells = _cells

}

class BoardState {

    var cells = List(9) {
        MutableList<CellData>(9) {
            CellData.CellCandidates()
        }
    }

    init {
        val board = SampleBoardGenerator.generateBoard()
        val mask = FixedMaskGenerator.generateMask()
        cells.forEachIndexed { row, rowCells ->
            val rowValues = board.cellsByRow[row]
            rowCells.forEachIndexed { col, _ ->
                cells[row][col] =
                    if(mask[row*9+col])
                        CellData.FixedCell(rowValues!![col].value)
                    else
                        CellData.CellCandidates()
            }
        }
    }

}
