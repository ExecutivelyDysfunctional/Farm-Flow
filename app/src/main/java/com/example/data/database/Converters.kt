package com.example.data.database

import androidx.room.TypeConverter
import com.example.data.model.MacroStep
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class Converters {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    
    private val stepsType = Types.newParameterizedType(List::class.java, MacroStep::class.java)
    private val stepsAdapter = moshi.adapter<List<MacroStep>>(stepsType)

    @TypeConverter
    fun fromStepsList(steps: List<MacroStep>?): String {
        return stepsAdapter.toJson(steps ?: emptyList())
    }

    @TypeConverter
    fun toStepsList(stepsJson: String?): List<MacroStep> {
        if (stepsJson.isNullOrEmpty()) return emptyList()
        return try {
            stepsAdapter.fromJson(stepsJson) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
