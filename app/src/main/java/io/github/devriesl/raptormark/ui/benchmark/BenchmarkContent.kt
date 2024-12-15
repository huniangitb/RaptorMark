package io.github.devriesl.raptormark.ui.benchmark

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.devriesl.raptormark.R
import io.github.devriesl.raptormark.viewmodels.BenchmarkViewModel
import kotlinx.coroutines.launch

@Composable
fun BenchmarkContent(
    benchmarkViewModel: BenchmarkViewModel,
    snackbarHostState: SnackbarHostState
) {
    val scrollState = rememberScrollState()
    val state = benchmarkViewModel.benchmarkState
    val coroutineScope = rememberCoroutineScope()
    val tooltipString = stringResource(R.string.stop_click_tooltip)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
            .verticalScroll(scrollState)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.calculated_scores_result_format, state.score),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.CenterVertically).weight(1f)
            )
            ElevatedButton(
                modifier = Modifier.wrapContentWidth(),
                onClick = {
                    if (state.running) {
                        benchmarkViewModel.onTestStop()
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(tooltipString)
                        }
                    } else {
                        benchmarkViewModel.onTestStart()
                    }
                }
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (state.running) {
                        Icon(
                            painter = painterResource(R.drawable.ic_stop_button),
                            contentDescription = stringResource(R.string.stop_button_desc)
                        )
                        Text(stringResource(R.string.stop_button_label))
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.ic_start_button),
                            contentDescription = stringResource(R.string.start_button_desc)
                        )
                        Text(stringResource(R.string.start_button_label))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        TestItemList(benchmarkViewModel.testItems.associate { it.testCase to it.testResult })
    }
}
