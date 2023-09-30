package com.des.sakudoku.model


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import com.des.sakudoku.board.generator.CommandBackTrackGenerator
import com.des.sakudoku.board.generator.RandomMaskGenerator

class BoardViewModel : ViewModel() {

    private var undoStack: MutableList<() -> Unit> = listOf<() -> Unit>().toMutableStateList()

    /**
     * The solution values.
     */
    private lateinit var solutionValues: List<Int>

    private val _boardCells = initCellData().toMutableStateList()

    private var _editMode : Boolean by mutableStateOf(true)
    val editMode : Boolean
        get() = _editMode

    val cells: List<CellData>
        get() = _boardCells

    private var selectedCell: CellData? by mutableStateOf(null)

    fun selectCell(cell: CellData) {
        if(selectedCell !== cell) {
            selectedCell?.selected = false
            selectedCell = cell
            cell.selected = true
        } else {
            //cell.selected = false
            //selectedCell = null
            //_numberInputEnabled = false
        }
    }

    fun isEditableCell() = selectedCell is CellData.PlayerCellData

    fun isErasableCell() = selectedCell?.erasable() ?: false

    fun isGuessCorrect(playerCell: CellData.PlayerCellData) =
        solutionValues[playerCell.index()] == playerCell.guess

    fun guessConflicts(playerCell: CellData.PlayerCellData) : List<CellData>  {
        TODO()
        //return emptyList()
    }

    fun toggleEditMode() {
        _editMode = !_editMode
    }

    fun numberEntered(number: Int) {
        if(_editMode && !(selectedCell as CellData.PlayerCellData).hasGuess()) {
            toggleOption(number)
        }
        else {
            toggleGuess(number)
        }
    }

    private fun toggleOption(number: Int) {
        if(selectedCell is CellData.PlayerCellData) {
            stackSelectedCellState()
            (selectedCell as? CellData.PlayerCellData)?.toggleOption(number)
        }
    }

    private fun toggleGuess(number: Int) {
        if(selectedCell is CellData.PlayerCellData) {
            stackSelectedCellState()
            (selectedCell as? CellData.PlayerCellData)?.toggleGuess(number)
        }
    }

    private fun restoreCellFrom(cell: CellData.PlayerCellData) {
        (_boardCells[cell.index()] as? CellData.PlayerCellData)?.copyStateFrom(cell)
    }

    private fun stackSelectedCellState() {
        when (selectedCell) {
            is CellData.PlayerCellData -> {
                val cell = (selectedCell as CellData.PlayerCellData).deepCopy()
                undoStack.add {restoreCellFrom(cell)}
            }
            else -> Unit
        }
    }

    fun clearCell() {

        // Prepare undo data
        stackSelectedCellState()
        selectedCell?.clear()
    }

    private fun initCellData() : List<CellData> {
        val board = CommandBackTrackGenerator.generateBoard()
        val mask = RandomMaskGenerator.generateMask()

        solutionValues = board.cells.map { it.value }

        return (0..80).map { index ->
            val row = index / 9
            val col = index % 9

            if (mask[index])
                CellData.FixedCellData(value = board.cells[index].value, row = row, col = col)
            else
                CellData.PlayerCellData(row = row, col = col)
        }
    }

    fun undoLastPlayerAction() {
        if(undoStack.isNotEmpty()) {
            undoStack.removeLast()()
        }
    }

    fun canUndo() = undoStack.isNotEmpty()

}

fun CellData.index() = row * 9 + col
