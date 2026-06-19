package io.github.devriesl.raptormark

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import io.github.devriesl.raptormark.data.NativeHandler
import java.io.File

@HiltAndroidApp
class RaptorApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initFioRunner()
    }

    private fun initFioRunner() {
        try {
            // Try to extract fio_runner from assets for root execution
            val runnerDir = File(filesDir, "bin")
            runnerDir.mkdirs()
            val runnerFile = File(runnerDir, "fio_runner")

            if (!runnerFile.exists()) {
                try {
                    applicationContext.assets.open("bin/fio_runner").use { input ->
                        runnerFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    runnerFile.setExecutable(true)
                    Log.i(TAG, "fio_runner extracted to ${runnerFile.absolutePath}")
                } catch (e: Exception) {
                    Log.w(TAG, "fio_runner not in assets, root execution disabled: ${e.message}")
                }
            }

            if (runnerFile.exists() && runnerFile.canExecute()) {
                NativeHandler.setFioRunnerPath(runnerFile.absolutePath)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to initialize fio_runner: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "RaptorApplication"
    }
}