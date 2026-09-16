package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timers")
data class TimerEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val label: String,
    val totalDurationSeconds: Long,
    val targetTimestamp: Long, // Epoch timestamp in ms when the timer expires
    val category: String = "Crops", // "Crops", "Winery", "Expedition", "Custom"
    val isActive: Boolean = true
)
