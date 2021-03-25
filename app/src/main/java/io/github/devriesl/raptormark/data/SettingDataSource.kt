package io.github.devriesl.raptormark.data

import android.content.Context
import io.github.devriesl.raptormark.Constants.DEFAULT_IO_ENGINE_VALUE
import org.json.JSONObject

class SettingDataSource private constructor(context: Context) {
    private val sharedPrefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
    private val appFilesDir = context.filesDir
    private var engineConfig: String? = null

    init {
        engineConfig = getEngineConfig()
    }

    fun getEngineList(): ArrayList<String> {
        val engineList: ArrayList<String> = ArrayList()
        val jsonObject = JSONObject(NativeDataSource.native_ListEngines())
        val jsonArray = jsonObject.getJSONArray("engines")
        for (i in 0 until jsonArray.length()) {
            val engineObject: JSONObject = jsonArray.getJSONObject(i)
            val engineItem = engineObject.getString("name")
            val engineAvailable = engineObject.getBoolean("available")
            if (engineAvailable) {
                engineList.add(engineItem)
            }
        }
        return engineList
    }

    fun getEngineConfig(): String {
        val defaultEngine = engineConfig ?: DEFAULT_IO_ENGINE_VALUE
        val engine = sharedPrefs.getString(ENGINE_CONFIG_KEY, defaultEngine)
        return engine ?: defaultEngine
    }

    fun setEngineConfig(engine: String) {
        with(sharedPrefs.edit()) {
            putString(ENGINE_CONFIG_KEY, engine)
            commit()
        }
    }

    fun getAppStoragePath(): String {
        return appFilesDir.absolutePath
    }

    companion object {
        const val SHARED_PREFS_NAME = "raptor_mark_settings"
        const val ENGINE_CONFIG_KEY = "engine_config"

        @Volatile
        private var INSTANCE: SettingDataSource? = null

        fun getInstance(context: Context): SettingDataSource =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: SettingDataSource(context).also { INSTANCE = it }
            }
    }
}