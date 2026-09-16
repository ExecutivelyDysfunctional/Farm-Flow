package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "macros")
data class MacroEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String, // "AUTO_CLICKER", "JS_INJECT", "NAVIGATION"
    val jsCode: String,
    val intervalMs: Long = 1000L,
    val targetSelector: String = "",
    val isPredefined: Boolean = false,
    val description: String = ""
)
