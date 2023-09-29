package com.des.sakudoku.model


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import com.des.sakudoku.board.generator.FixedMaskGenerator
import com.des.sakudoku.board.generator.SampleBoardGenerator
import com.des.sakudoku.board.generator.UndoCommand

class BoardViewModel : ViewModel() {

    private var undoStack: MutableList<UndoCommand> = mutableListOf<UndoCommand>().toMutableStateList()

    private lateinit var correctValues: List<Int>

    private val _cells = initCellData().toMutableStateList()

    private var _editMode : Boolean by mutableStateOf(false)
    val editMode : Boolean
        get() = _editMode

    val cells: List<CellData>
        get() = _cells

    private var selectedCell: CellData? by mutableStateOf(null)

    fun clickedCell(cell: CellData) {
        if(selectedCell === cell) {
            //cell.selected = false
            //selectedCell = null
            //_numberInputEnabled = false
        } else {
            selectedCell?.selected = false
            selectedCell = cell
            cell.selected = true
        }
    }

    fun isEditableCell() = selectedCell is CellData.PlayerCellData

    fun isGuessCorrect(playerCell: CellData.PlayerCellData) =
        correctValues[playerCell.row * 9 + playerCell.col] == playerCell.guess

    fun guessConflicts(playerCell: CellData.PlayerCellData) : List<CellData>  {
        TODO()
        //return emptyList()
    }

    fun toggleEditMode() {
        _editMode = !_editMode
    }

    fun numberEntered(number: Int) {
        if(_editMode) toggleCandidate(number)
        else toggleGuess(number)
    }

    private fun toggleCandidate(number: Int) {
        if(selectedCell is CellData.PlayerCellData) {
            val cell = selectedCell as CellData.PlayerCellData
            cell.toggleOption(number)
        }
    }

    private fun toggleGuess(number: Int) {
        if(selectedCell is CellData.PlayerCellData) {
            val cell = selectedCell as CellData.PlayerCellData
            cell.toggleGuess(number)
        }
    }

    fun clearCell() {
        selectedCell?.clear()
    }

    private fun initCellData() : List<CellData> {
        val board = SampleBoardGenerator.generateBoard()
        val mask = FixedMaskGenerator.generateMask()

        correctValues = board.cells.map { it.value }

        return (0..80).map { index ->
            val row = index / 9
            val col = index % 9

            if (mask[index])
                CellData.FixedCellData(value = board.cells[index].value, row = row, col = col)
            else
                CellData.PlayerCellData(row = row, col = col)
        }
    }

}


