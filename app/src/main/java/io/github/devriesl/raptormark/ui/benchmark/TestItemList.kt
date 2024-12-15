package io.github.devriesl.raptormark.ui.benchmark

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.devriesl.raptormark.data.TestCase
import io.github.devriesl.raptormark.data.TestResult
import io.github.devriesl.raptormark.data.isQ1T1MixRw
import io.github.devriesl.raptormark.data.isQ2T1MixRw
import io.github.devriesl.raptormark.data.isQ4T1MixRw
import io.github.devriesl.raptormark.data.isQ4T4MixRw
import io.github.devriesl.raptormark.data.isRandRd
import io.github.devriesl.raptormark.data.isRandWr
import io.github.devriesl.raptormark.data.isSeqRw

@Composable
fun TestItemList(
    tests: Map<TestCase, TestResult?>,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.wrapContentHeight()
    ) {
        tests.filter { it.key.isSeqRw() }.forEach { (testCase, testResult) ->
            TestItem(
                case = testCase,
                result = testResult
            )
            HorizontalDivider()
        }
        TestGroupHeader(
            headers = listOf("4K READ", "IOPS", "LAT AVG"),
            color = MaterialTheme.colorScheme.primary
        )
        HorizontalDivider()
        tests.filter { it.key.isRandRd() }.forEach { (testCase, testResult) ->
            TestItem(
                case = testCase,
                result = testResult
            )
            HorizontalDivider()
        }
        TestGroupHeader(
            headers = listOf("4K WRITE", "IOPS", "LAT AVG"),
            color = MaterialTheme.colorScheme.primary
        )
        HorizontalDivider()
        tests.filter { it.key.isRandWr() }.forEach { (testCase, testResult) ->
            TestItem(
                case = testCase,
                result = testResult
            )
            HorizontalDivider()
        }
        TestGroupHeader(
            headers = listOf("Q1T1 MIX", "READ IOPS", "WRITE IOPS"),
            color = MaterialTheme.colorScheme.secondary
        )
        HorizontalDivider()
        tests.filter { it.key.isQ1T1MixRw() }.forEach { (testCase, testResult) ->
            TestItem(
                case = testCase,
                result = testResult
            )
            HorizontalDivider()
        }
        TestGroupHeader(
            headers = listOf("Q2T1 MIX", "READ IOPS", "WRITE IOPS"),
            color = MaterialTheme.colorScheme.tertiary
        )
        HorizontalDivider()
        tests.filter { it.key.isQ2T1MixRw() }.forEach { (testCase, testResult) ->
            TestItem(
                case = testCase,
                result = testResult
            )
            HorizontalDivider()
        }
        TestGroupHeader(
            headers = listOf("Q4T1 MIX", "READ IOPS", "WRITE IOPS"),
            color = MaterialTheme.colorScheme.secondary
        )
        HorizontalDivider()
        tests.filter { it.key.isQ4T1MixRw() }.forEach { (testCase, testResult) ->
            TestItem(
                case = testCase,
                result = testResult
            )
            HorizontalDivider()
        }
        TestGroupHeader(
            headers = listOf("Q4T4 MIX", "READ IOPS", "WRITE IOPS"),
            color = MaterialTheme.colorScheme.tertiary
        )
        HorizontalDivider()
        tests.filter { it.key.isQ4T4MixRw() }.forEach { (testCase, testResult) ->
            TestItem(
                case = testCase,
                result = testResult
            )
            HorizontalDivider()
        }
    }
}