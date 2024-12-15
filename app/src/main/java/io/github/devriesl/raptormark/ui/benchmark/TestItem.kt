package io.github.devriesl.raptormark.ui.benchmark

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
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
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = if(case.isSeqRw()) {
                case.name
            } else if (case.isMixRw()) {
                case.name.substring(case.name.length - 4)
            } else {
                case.name.substring(0, 4)
            },
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Start
        )

        when {
            case.isRandRw() -> {
                (result as? TestResult.RANDOM)?.let {
                    VerticalDivider(Modifier.height(8.dp))
                    Text(
                        text = it.ioPerSec.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    VerticalDivider(Modifier.height(8.dp))
                    Text(
                        text = it.latency.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                }
            }
            case.isMixRw() -> {
                (result as? TestResult.MIX)?.let {
                    VerticalDivider(Modifier.height(8.dp))
                    Text(
                        text = it.rdIoPerSec.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    VerticalDivider(Modifier.height(8.dp))
                    Text(
                        text = it.wrIoPerSec.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                }
            }
            case.isSeqRw() -> {
                (result as? TestResult.SEQUENCE)?.let {
                    Text(
                        text = stringResource(R.string.sum_of_bw_test_result_format, it.bandwidth),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}
