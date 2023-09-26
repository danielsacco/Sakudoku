package com.des.sakudoku.board

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
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
                        onClick = boardViewModel::clickedCell
                    )
                }
            }
        }
    }
}

@Composable
fun GameCell(
    data: CellData,
    bgColor: Color,
    onClick: (cellData: CellData) -> Unit,
    modifier: Modifier = Modifier
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
                .selectable(true) { onClick(data) }
        ) {
            when (data) {
                is CellData.FixedCell -> Text(
                    text = data.value.toString(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
                is CellData.CellGuess -> Text(text = data.value.toString())
                is CellData.CellCandidates -> OptionsCell(data)
            }
        }
    }
}

@Composable
fun OptionsCell(
    options: CellData.CellCandidates
) {
    Column {
        var value = 0
        for (row in 0..2) {
            Row {
                for (index in 1..3) {
                    value++
                    Text(
                        text = "",
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
    Column {
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
                        enabled = boardViewModel.numberInputEnabled
                    ) {
                        Text(number.toString())
                    }
                }
            }
        }
    }
}

@Composable
fun ControlsPad(boardViewModel: BoardViewModel = viewModel()) {

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