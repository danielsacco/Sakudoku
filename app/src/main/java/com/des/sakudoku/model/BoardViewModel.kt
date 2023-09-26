package com.des.sakudoku.model


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import com.des.sakudoku.board.generator.FixedMaskGenerator
import com.des.sakudoku.board.generator.SampleBoardGenerator

class BoardViewModel : ViewModel() {

    private lateinit var correctValues: List<Int>

    private val _cells = initCellData().toMutableStateList()

    private var _editMode : Boolean by mutableStateOf(false)
    val editMode : Boolean
        get() = _editMode

    val cells: List<CellData>
        get() = _cells

    private var selectedCell: CellData? = null

    private var _numberInputEnabled : Boolean by mutableStateOf(false)
    val numberInputEnabled : Boolean
        get() = _numberInputEnabled

    fun clickedCell(cell: CellData) {
        if(selectedCell === cell) {
            //cell.selected = false
            //selectedCell = null
            //_numberInputEnabled = false
        } else {
            selectedCell?.selected = false
            selectedCell = cell
            cell.selected = true
            _numberInputEnabled = true
        }
    }


    fun isGuessCorrect(guessCell: CellData.CellGuess) =
        correctValues[guessCell.row * 9 + guessCell.col] == guessCell.value

    fun guessConflicts(guessCell: CellData.CellGuess) : List<CellData>  {
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

    }

    private fun toggleGuess(number: Int) {

    }

    private fun initCellData() : List<CellData> {
        val board = SampleBoardGenerator.generateBoard()
        val mask = FixedMaskGenerator.generateMask()

        correctValues = board.cells.map { it.value }

        return (0..80).map { index ->
            val row = index / 9
            val col = index % 9

            if (mask[index])
                CellData.FixedCell(value = board.cells[index].value, row = row, col = col)
            else
                CellData.CellCandidates(row = row, col = col)
        }
    }

}


