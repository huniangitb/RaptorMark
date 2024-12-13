package io.github.devriesl.raptormark

import androidx.room.TypeConverter
import io.github.devriesl.raptormark.data.TestCase
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter
    fun toResults(value: String): Map<TestCase, String> {
        return Json.decodeFromString(value)
    }

    @TypeConverter
    fun fromResults(results: Map<TestCase, String>): String {
        return Json.encodeToString(results)
    }
}