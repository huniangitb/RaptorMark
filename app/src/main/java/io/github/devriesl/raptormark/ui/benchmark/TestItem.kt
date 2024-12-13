package io.github.devriesl.raptormark.ui.benchmark

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.devriesl.raptormark.R
import io.github.devriesl.raptormark.data.TestCase
import io.github.devriesl.raptormark.data.TestResult
import io.github.devriesl.raptormark.data.isMixRw
import io.github.devriesl.raptormark.data.isRandRw
import io.github.devriesl.raptormark.data.isSeqRw

@Composable
fun TestItem(
    case: TestCase,
    result: TestResult?
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = case.name,
            style = MaterialTheme.typography.bodySmall,
        )

        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.End
        ) {
            when {
                case.isRandRw() -> {
                    (result as? TestResult.RANDOM)?.let {
                        Text(
                            text = stringResource(R.string.sum_of_iops_test_result_format, it.ioPerSec),
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = stringResource(R.string.avg_of_lat_test_result_format, it.latency),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
                case.isMixRw() -> {
                    (result as? TestResult.MIX)?.let {
                        Text(
                            text = stringResource(R.string.sum_of_iops_test_result_format, it.rdIoPerSec),
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = stringResource(R.string.sum_of_iops_test_result_format, it.wrIoPerSec),
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = stringResource(R.string.avg_of_lat_test_result_format, it.latency),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
                case.isSeqRw() -> {
                    (result as? TestResult.SEQUENCE)?.let {
                        Text(
                            text = stringResource(R.string.sum_of_bw_test_result_format, it.bandwidth),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}
