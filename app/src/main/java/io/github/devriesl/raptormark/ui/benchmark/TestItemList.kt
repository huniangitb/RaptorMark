package io.github.devriesl.raptormark.ui.benchmark

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.devriesl.raptormark.data.TestCase
import io.github.devriesl.raptormark.data.TestResult

@Composable
fun TestItemList(
    tests: Map<TestCase, TestResult?>,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.wrapContentHeight()
    ) {
        tests.onEachIndexed { index, (testCase, testResult) ->
            TestItem(
                case = testCase,
                result = testResult
            )
            if (index < tests.size - 1) {
                HorizontalDivider()
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}