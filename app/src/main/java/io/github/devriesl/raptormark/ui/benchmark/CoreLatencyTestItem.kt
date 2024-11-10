package io.github.devriesl.raptormark.ui.benchmark

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun CoreLatencyTestItem(
    @StringRes title: Int,
    latencies: Map<Pair<Int, Int>, Int>?,
    progress: Float?
) {
    val latencyColors = listOf(
        Color.Gray,
        Color(0xFF0DCD51),
        Color(0xFF34D048),
        Color(0xFF5AD23E),
        Color(0xFF78DA08),
        Color(0xFF93E108),
        Color(0xFFADE100),
        Color(0xFFD3E400),
        Color(0xFFDFE200),
        Color(0xFFEBE000),
        Color(0xFFFBE700),
        Color(0xFFFCD100),
        Color(0xFFFDBB00),
        Color(0xFFFFA500)
    )

    latencies?.let { map ->
        val rowKeys = map.keys.map { it.first }.distinct().sorted()
        val columnKeys = map.keys.map { it.second }.distinct().sorted()

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .border(BorderStroke(1.dp, Color.Black))
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${((progress ?: 0f) * 100).toInt()}%",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                columnKeys.forEach { col ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .border(BorderStroke(1.dp, Color.Black))
                            .background(Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "$col", fontSize = 16.sp)
                    }
                }
            }

            rowKeys.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .border(BorderStroke(1.dp, Color.Black))
                            .background(Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "$row", fontSize = 16.sp)
                    }

                    columnKeys.forEach { col ->
                        val cellValue = map[Pair(row, col)]
                        val backgroundColor = when {
                            cellValue == null -> latencyColors[0]
                            cellValue <= 30 -> latencyColors[1]
                            cellValue <= 40 -> latencyColors[2]
                            cellValue <= 50 -> latencyColors[3]
                            cellValue <= 60 -> latencyColors[4]
                            cellValue <= 70 -> latencyColors[5]
                            cellValue <= 80 -> latencyColors[6]
                            cellValue <= 90 -> latencyColors[7]
                            cellValue <= 100 -> latencyColors[8]
                            cellValue <= 200 -> latencyColors[9]
                            cellValue <= 300 -> latencyColors[10]
                            cellValue <= 400 -> latencyColors[11]
                            cellValue <= 500 -> latencyColors[12]
                            else -> latencyColors[13]
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .border(BorderStroke(1.dp, Color.Black))
                                .background(backgroundColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cellValue?.toString() ?: "-",
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
