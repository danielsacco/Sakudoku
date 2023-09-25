package com.des.sakudoku.model


import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import com.des.sakudoku.board.generator.FixedMaskGenerator
import com.des.sakudoku.board.generator.SampleBoardGenerator

class BoardViewModel : ViewModel() {

    private val _cells = BoardState().cells.toMutableStateList()
    val cells = _cells

    private var selectedCell: CellData? = null

    fun clickedCell(cell: CellData) {
        selectedCell?.selected = false

        selectedCell = cell
        cell.selected = true

        // Replace by the new cell so the State: Doesn't work
        //_cells[cell.row][cell.col] = cell




    }

}

class BoardState {

    var cells = List(9) { row ->
        MutableList<CellData>(9) { col ->
            CellData.CellCandidates(row = row, col = col)
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
                        CellData.FixedCell(value = rowValues!![col].value, row = row, col = col)
                    else
                        CellData.CellCandidates(row = row, col = col)
            }
        }
    }

}
