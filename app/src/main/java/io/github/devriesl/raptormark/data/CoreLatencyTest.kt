package io.github.devriesl.raptormark.data

import org.json.JSONArray
import org.json.JSONObject

class CoreLatencyTest(testCase: TestCases, settingSharedPrefs: SettingSharedPrefs) : BenchmarkTest(
    testCase, settingSharedPrefs
) {
    override fun nativeTest(jsonCommand: String): Int {
        samplesMapList.clear()
        return NativeHandler.native_CoreLatencyTest(jsonCommand)
    }

    override fun testOptionsBuilder(): String {
        val root = JSONObject()
        val options = JSONArray()

        root.put("shortopts", false)
        root.put("options", options)

        return root.toString()
    }

    companion object {
        private val samplesMapList = mutableMapOf<Pair<Int, Int>, MutableList<Int>>()
        private const val TOTAL_SAMPLES = 500

        @JvmStatic
        fun parseResult(result: String): TestResult.CoreLatency {
            for (line in result.lines()) {
                val regex = Regex("""\s*(\d+)\s*,\s*(\d+)\s*,\s*(\d+)\s*""")
                val matchResult = regex.find(line)
                if (matchResult != null) {
                    val (core1, core2, latency) = matchResult.destructured
                    val corePair = Pair(core1.toInt(), core2.toInt())
                    val latencyValue = latency.toInt()

                    samplesMapList.getOrPut(corePair) { mutableListOf() }.add(latencyValue)
                }
            }

            val samples = samplesMapList.values.maxOfOrNull { it.size } ?: 0

            val latencyMap = samplesMapList.mapValues {
                it.value.mode()
            }

            return TestResult.CoreLatency(latencyMap, samples.toFloat() / TOTAL_SAMPLES)
        }
    }
}

fun <T : Comparable<T>> List<T>.mode(): T {
    val frequencyMap = this.groupingBy { it }.eachCount()

    val maxCount = frequencyMap.values.max()

    val modes = frequencyMap.filter { it.value == maxCount }.keys.sorted()

    return modes.first()
}
