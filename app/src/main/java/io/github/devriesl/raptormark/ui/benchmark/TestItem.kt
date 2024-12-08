package io.github.devriesl.raptormark.ui.benchmark

import androidx.compose.runtime.Composable
import io.github.devriesl.raptormark.data.*

@Composable
fun TestItem(
    testCase: TestCases,
    result: String?
) {
    when {
        testCase.isFIO() -> {
            val testResult = result?.let { FIOTest.parseResult(it) }
            FIOTestItem(
                title = testCase.title,
                bandwidth = testResult?.bandwidth,
                showLatency = testCase.isFIORand(),
                latency = testResult?.latency
            )
        }
    }
}
