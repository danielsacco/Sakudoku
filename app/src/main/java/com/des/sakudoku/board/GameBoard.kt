package com.des.sakudoku.board

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.des.sakudoku.model.BoardViewModel
import com.des.sakudoku.model.CellData
import com.des.sakudoku.ui.theme.SakudokuTheme
import androidx.lifecycle.viewmodel.compose.viewModel


@Composable
fun GameScreen() {
    Column {
        GameBoard()
        ActionsBoard()
    }
}

@Composable
fun GameBoard(
    modifier: Modifier = Modifier,
    boardViewModel: BoardViewModel = viewModel()
) {
    Row(
        modifier = modifier.fillMaxWidth()
    ) {
        for (col in 0..8) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier.weight(1f)
            ) {
                for (row in 0..8) {
                    GameCell(
                        data = boardViewModel.cells[row * 9 + col],
                        modifier = modifier,
                        bgColor = backgroundColor(col = col, row = row),
                        onSelect = boardViewModel::selectCell
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GameCell(
    data: CellData,
    bgColor: Color,
    onSelect: (cellData: CellData) -> Unit,
    modifier: Modifier = Modifier,
    boardViewModel: BoardViewModel = viewModel()
) {
    Surface(
        color = if(data.selected) MaterialTheme.colorScheme.surfaceVariant
                else bgColor,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(52.dp)
                .border(1.dp, Color(0xFFF0F0F0))
                //.selectable(true) { onSelect(data) }
                .combinedClickable(
                    onClick = { onSelect(data) },
                    onLongClick = {
                        onSelect(data)
                        boardViewModel.toggleEditMode()
                    }
                )

                
        ) {
            when (data) {
                is CellData.FixedCellData -> Text(
                    text = data.value.toString(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
                is CellData.PlayerCellData -> PlayerCell(data)
            }
        }
    }
}

@Composable
fun PlayerCell(data: CellData.PlayerCellData) {

    if(data.hasGuess()) {
        Text(
            text = data.guess.toString(),
            fontSize = 24.sp
        )
    } else {
        OptionsCell(data)
    }

}

@Composable
fun OptionsCell(
    data: CellData.PlayerCellData
) {
    Column {
        var value = 0
        for (row in 0..2) {
            Row {
                for (index in 1..3) {
                    value++
                    Text(
                        text = if (data.isCandidate(value)) value.toString() else "",
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun ActionsBoard(boardViewModel: BoardViewModel = viewModel()) {
    Row (
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.fillMaxWidth()
    ) {
        NumbersPad()
        ControlsPad()
    }
}


@Composable
fun NumbersPad(boardViewModel: BoardViewModel = viewModel()) {
    Column {
        for(row in 0..2) {
            Row {
                for (col in 1..3) {
                    val number = row * 3 + col
                    Button(
                        onClick = { boardViewModel.numberEntered(number) },
                        enabled = boardViewModel.isEditableCell(),
                        modifier = Modifier.size(80.dp, 60.dp)
                    ) {
                        Text(
                            number.toString(),
                            fontSize = if (boardViewModel.editMode) 16.sp else 26.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ControlsPad(boardViewModel: BoardViewModel = viewModel()) {
    Column(
        horizontalAlignment = Alignment.End
    ) {
        EditSwitch()
        //EditButton()
        ClearButton()
        UndoButton()
        VerifyButton()
    }
}

@Composable
fun EditSwitch(boardViewModel: BoardViewModel = viewModel()) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Edit")
        Spacer(modifier = Modifier.size(12.dp))
        Switch(
            checked = boardViewModel.editMode,
            onCheckedChange = {boardViewModel.toggleEditMode()}
        )
    }
}

@Composable
fun EditButton(boardViewModel: BoardViewModel = viewModel()) {
    Button(
        onClick = { boardViewModel.toggleEditMode() },
        //enabled = true
    ) {
        Icon(imageVector = Icons.Default.Edit, contentDescription = null)
    }

}

@Composable
fun ClearButton(boardViewModel: BoardViewModel = viewModel()) {
    Button(
        onClick = { boardViewModel.clearCell() },
        enabled = boardViewModel.isErasableCell()
    ) {
        Icon(imageVector = Icons.Default.Delete, contentDescription = null)
    }
}


@Composable
fun UndoButton(boardViewModel: BoardViewModel = viewModel()) {
    Button(
        onClick = { boardViewModel.undoLastPlayerAction() },
        enabled = boardViewModel.canUndo()
    ) {
        Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
    }
}

@Composable
fun VerifyButton(boardViewModel: BoardViewModel = viewModel()) {
    Button(
        onClick = {  }, // TODO
    ) {
        Icon(imageVector = Icons.Default.Check, contentDescription = null)
    }
}

fun backgroundColor(row: Int, col: Int): Color {
    val groupNumber = (row /3) + (col / 3)

    return if(groupNumber % 2 == 0)
        Color(0xFFFAFAFA) else
        Color(0xFFF2F2F2)
}

@Preview(showBackground = true)
@Composable
fun ScreenPreview() {
    SakudokuTheme {
        GameScreen()
    }
}

//@Preview(showBackground = true)
@Composable
fun BoardPreview() {
    SakudokuTheme {
        GameBoard()
    }
}

//@Preview(showBackground = true)
@Composable
fun ActionsBoardPreview() {
    SakudokuTheme {
        ActionsBoard()
    }
}