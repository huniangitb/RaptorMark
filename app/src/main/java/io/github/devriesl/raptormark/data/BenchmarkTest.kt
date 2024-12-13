package io.github.devriesl.raptormark.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.devriesl.raptormark.Constants
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class BenchmarkTest(
    val testCase: TestCase,
    val settingSharedPrefs: SettingSharedPrefs
) {
    private val filePath = getRandomFilePath()

    var testResult: TestResult? by mutableStateOf(null)
        private set

    var nativeResult: String? = null

    val nativeListener: NativeListener = object : NativeListener {
        override fun onTestResult(result: String) {
            nativeResult = listOfNotNull(nativeResult, result).joinToString(System.lineSeparator())
            testResult = parseResult(nativeResult ?: return)
        }
    }

    fun nativeTest(jsonCommand: String): Int {
        return NativeHandler.native_FIOTest(jsonCommand)
    }

    fun runTest(): String? {
        nativeResult = null

        NativeHandler.registerListener(nativeListener)
        val options = testOptionsBuilder()
        val ret = nativeTest(options)
        NativeHandler.unregisterListener(nativeListener)

        val testFile = File(filePath)
        if (testFile.exists()) testFile.delete()

        return nativeResult
    }

    fun createOption(name: String, value: String? = null): JSONObject {
        val jsonOption = JSONObject()
        jsonOption.put("name", name)
        jsonOption.put("value", value)
        return jsonOption
    }

    fun testOptionsBuilder(): String {
        val root = JSONObject()
        val options = JSONArray()

        root.put("shortopts", false)

        options.put(createOption(Constants.NEW_JOB_OPT_NAME, testCase.name))
        options.put(createOption(Constants.FILE_PATH_OPT_NAME, filePath))
        options.put(createOption(Constants.IO_TYPE_OPT_NAME, testCase.type))
        options.put(
            createOption(
                Constants.IO_DEPTH_OPT_NAME,
                testCase.ioDepth.toString()
            )
        )
        options.put(
            createOption(
                Constants.RUNTIME_OPT_NAME,
                SettingOptions.RUNTIME_LIMIT.dataImpl.getValue(settingSharedPrefs)
            )
        )
        options.put(
            createOption(
                Constants.BLOCK_SIZE_OPT_NAME,
                if (testCase.isSeqRw()) {
                    SettingOptions.SEQ_BLOCK_SIZE.dataImpl.getValue(settingSharedPrefs)
                } else {
                    SettingOptions.RAND_BLOCK_SIZE.dataImpl.getValue(settingSharedPrefs)
                }
            )
        )
        options.put(
            createOption(
                Constants.IO_SIZE_OPT_NAME,
                SettingOptions.IO_SIZE.dataImpl.getValue(settingSharedPrefs)
            )
        )
        options.put(createOption(Constants.DIRECT_IO_OPT_NAME, Constants.CONSTANT_DIRECT_IO_VALUE))
        options.put(
            createOption(
                Constants.IO_ENGINE_OPT_NAME,
                SettingOptions.IO_ENGINE.dataImpl.getValue(settingSharedPrefs)
            )
        )
        options.put(
            createOption(
                Constants.NUM_THREADS_OPT_NAME,
                testCase.numJobs.toString()
            )
        )
        testCase.rwMixWrite?.let {
            options.put(createOption(Constants.RW_MIX_WRITE_OPT_NAME, it.toString()))
        }

        options.put(createOption(Constants.ETA_PRINT_OPT_NAME, Constants.CONSTANT_ETA_PRINT_VALUE))
        options.put(
            createOption(
                Constants.OUTPUT_FORMAT_OPT_NAME,
                Constants.CONSTANT_OUTPUT_FORMAT_VALUE
            )
        )

        root.put("options", options)

        return root.toString()
    }

    private fun getRandomFilePath(): String {
        val randomSuffix = List(FILE_SUFFIX_LENGTH) {
            (('a'..'z') + ('A'..'Z') + ('0'..'9')).random()
        }.joinToString("")

        return settingSharedPrefs.getTestDirPath() + "/" + testCase.name + randomSuffix
    }

    companion object {
        const val FILE_SUFFIX_LENGTH = 8

        @JvmStatic
        fun parseResult(result: String): TestResult? {
            var jobsId: String? = null
            var sumOfBwBytes: Long = 0
            var sumOfRdIoPerSec = 0.0
            var sumOfWrIoPerSec = 0.0
            var sumOfAvgClatNs = 0.0

            val jsonResult = JSONObject(result)
            val jobsArray = jsonResult.getJSONArray("jobs")
            for (i in 0 until jobsArray.length()) {
                val jobObject: JSONObject = jobsArray.getJSONObject(i)
                if (jobsId.isNullOrEmpty()) {
                    jobsId = jobObject.getString("jobname")
                }

                val rdObject: JSONObject = jobObject.getJSONObject("read")
                sumOfBwBytes += rdObject.getLong("bw_bytes")
                sumOfRdIoPerSec += rdObject.getDouble("iops")
                val rdClatObject: JSONObject = rdObject.getJSONObject("clat_ns")
                sumOfAvgClatNs += rdClatObject.getDouble("mean")
                val wrObject: JSONObject = jobObject.getJSONObject("write")
                sumOfBwBytes += wrObject.getLong("bw_bytes")
                sumOfWrIoPerSec += wrObject.getDouble("iops")
                val wrClatObject: JSONObject = wrObject.getJSONObject("clat_ns")
                sumOfAvgClatNs += wrClatObject.getDouble("mean")
            }

            val sumOfBw = (sumOfBwBytes / 1000 / 1000).toInt()
            val jobsAvgClat = (sumOfAvgClatNs / jobsArray.length() / 1000).toInt()

            return when {
                jobsId?.contains("SEQ") == true -> {
                    TestResult.SEQUENCE(sumOfBw)
                }
                jobsId?.contains("RAND") == true -> {
                    TestResult.RANDOM((sumOfRdIoPerSec + sumOfWrIoPerSec).toInt(), jobsAvgClat)
                }
                jobsId?.contains("MIX") == true -> {
                    TestResult.MIX(sumOfRdIoPerSec.toInt(), sumOfWrIoPerSec.toInt(), jobsAvgClat)
                }
                else -> {
                    null
                }
            }
        }
    }
}