package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.CompanionViewModel
import com.example.ui.viewmodel.LiveTimerState

data class TimerPreset(val name: String, val seconds: Long, val category: String, val emoji: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimersTab(
    viewModel: CompanionViewModel,
    modifier: Modifier = Modifier
) {
    val liveTimers by viewModel.liveTimers.collectAsState()
    
    var showCustomDialog by remember { mutableStateOf(false) }
    var customLabel by remember { mutableStateOf("") }
    var customMinutes by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Crops") }

    val presets = listOf(
        TimerPreset("Wheat", 60L, "Crops", "🌾"),
        TimerPreset("Hops", 600L, "Crops", "🌿"),
        TimerPreset("Carrots", 900L, "Crops", "🥕"),
        TimerPreset("Potatoes", 1800L, "Crops", "🥔"),
        TimerPreset("Cabbage", 3600L, "Crops", "🥬"),
        TimerPreset("Winery", 14400L, "Winery", "🍷"),
        TimerPreset("Expedition", 28800L, "Expedition", "🧗"),
        TimerPreset("Custom", 0L, "Custom", "⏰")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Quick Presets Area
        Text(
            text = "Quick Alarms & Farm Presets",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(160.dp)
        ) {
            items(presets) { preset ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (preset.name == "Custom") {
                                showCustomDialog = true
                            } else {
                                viewModel.addTimer(preset.name, preset.seconds, preset.category)
                            }
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (preset.name == "Custom") MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(preset.emoji, fontSize = 20.sp)
                        Column {
                            Text(
                                text = preset.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (preset.seconds > 0) formatDuration(preset.seconds) else "Add custom",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

        // Running Timers Area
        Text(
            text = "Active Timers (${liveTimers.count { it.isActive }})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        if (liveTimers.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Alarm,
                        contentDescription = "No active alarms",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "No active crop or winery alarms",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(liveTimers, key = { it.id }) { timer ->
                    val cardBgColor by animateColorAsState(
                        targetValue = if (!timer.isActive) {
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        label = "card_color"
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = cardBgColor)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val icon = when (timer.category) {
                                        "Crops" -> "🌾"
                                        "Winery" -> "🍷"
                                        "Expedition" -> "🧗"
                                        else -> "⏰"
                                    }
                                    Text(icon, fontSize = 22.sp)
                                    Column {
                                        Text(
                                            text = timer.label,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = if (timer.isActive) "Time left: ${formatDuration(timer.secondsLeft)}" else "READY FOR HARVEST!",
                                            fontSize = 11.sp,
                                            fontWeight = if (timer.isActive) FontWeight.Normal else FontWeight.Bold,
                                            color = if (timer.isActive) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) else MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                                
                                IconButton(
                                    onClick = { viewModel.deleteTimer(timer.id) }
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Dismiss",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }

                            if (timer.isActive) {
                                Spacer(modifier = Modifier.height(10.dp))
                                LinearProgressIndicator(
                                    progress = timer.progress,
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Custom Alarm Dialog
    if (showCustomDialog) {
        AlertDialog(
            onDismissRequest = { showCustomDialog = false },
            title = { Text("Set Custom Alarms", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = customLabel,
                        onValueChange = { customLabel = it },
                        label = { Text("Alarm Label / Crop") },
                        placeholder = { Text("e.g. Winery Stock #1") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("custom_timer_label")
                    )
                    OutlinedTextField(
                        value = customMinutes,
                        onValueChange = { customMinutes = it },
                        label = { Text("Duration (Minutes)") },
                        placeholder = { Text("60") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("custom_timer_duration")
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Category", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("Crops", "Winery", "Expedition", "Custom").forEach { cat ->
                            val isSelected = selectedCategory == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { selectedCategory = cat }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val min = customMinutes.toLongOrNull() ?: 1L
                        if (customLabel.isNotBlank()) {
                            viewModel.addTimer(customLabel, min * 60L, selectedCategory)
                            // reset
                            customLabel = ""
                            customMinutes = ""
                            showCustomDialog = false
                        }
                    }
                ) {
                    Text("Start Timer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun formatDuration(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) {
        String.format("%dh %dm %ds", h, m, s)
    } else if (m > 0) {
        String.format("%dm %ds", m, s)
    } else {
        String.format("%ds", s)
    }
}
