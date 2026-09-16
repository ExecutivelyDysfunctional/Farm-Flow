package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MacroStep(
    val type: String, // "click_selector", "click_text", "wait", "input", "scroll", "refresh"
    val target: String, // CSS selector or text string
    val value: String = "", // Input text or wait duration in ms
    val order: Int = 0
)

@Entity(tableName = "macros")
data class Macro(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val steps: List<MacroStep>,
    val intervalMs: Long = 1000,
    val repeatCount: Int = 0, // 0 means infinite / run until stopped
    val isSystemBuiltIn: Boolean = false,
    val isEnabled: Boolean = true
)

@Entity(tableName = "macro_logs")
data class MacroLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val macroId: Int,
    val macroName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val message: String,
    val type: String = "INFO" // "INFO", "SUCCESS", "ERROR", "WARNING"
)

@Entity(tableName = "favorite_shortcuts")
data class FavoriteShortcut(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val url: String,
    val iconName: String, // "home", "farming", "workshop", "explore", "fishing", "town"
    val order: Int = 0
)
