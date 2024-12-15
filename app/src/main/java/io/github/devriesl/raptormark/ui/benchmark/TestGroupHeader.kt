package io.github.devriesl.raptormark.ui.benchmark

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign

@Composable
fun TestGroupHeader(
    headers: List<String>,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth().background(color),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        headers.forEachIndexed { index, it ->
            Text(
                text = it,
                style = MaterialTheme.typography.labelMedium,
                textAlign = when (index) {
                    0 -> {
                        TextAlign.Start
                    }
                    headers.size - 1 -> {
                        TextAlign.End
                    }
                    else -> {
                        TextAlign.Center
                    }
                },
            )
        }
    }
}