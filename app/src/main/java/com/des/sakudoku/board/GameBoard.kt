package com.des.sakudoku.board

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.des.sakudoku.model.CellData
import com.des.sakudoku.ui.theme.SakudokuTheme
import kotlin.random.Random

@Composable
fun GameBoard(modifier: Modifier = Modifier) {
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
                        // TODO Values should come from the model !!!
                        data = CellData.CellValue(Random.nextInt(1, 10)),
                        modifier = modifier,
                        bgColor = backgroundColor(col = col, row = row)
                    )
                }
            }
        }
    }
}

fun backgroundColor(row: Int, col: Int): Color {
    val groupNumber = (row /3) + (col / 3)

    return if(groupNumber % 2 == 0)
        Color(0xFFFAFAFA) else
        Color(0xFFF2F2F2)
}

@Composable
fun GameCell(
    data: CellData,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    var selected by remember { mutableStateOf(false) }

    Surface(
        color = if(selected) MaterialTheme.colorScheme.surfaceVariant
                else bgColor,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(52.dp)
                .border(1.dp, Color(0xFFF0F0F0))
                .selectable(true) { selected = !selected }
        ) {
            when (data) {
                is CellData.ExposedCell -> Text(text = data.value.toString())
                is CellData.CellValue -> Text(text = data.value.toString())
                is CellData.CellCandidates -> CellOptions(data)
            }
        }
    }
}

@Composable
fun CellOptions(
    options: CellData.CellCandidates
) {
    Column {
        var value = 0
        for (row in 0..2) {
            Row {
                for (index in 1..3) {
                    value++
                    Text(
                        text =  if(Random.nextBoolean()) value.toString()
                        else "",
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SakudokuTheme {
        GameBoard()
    }
}