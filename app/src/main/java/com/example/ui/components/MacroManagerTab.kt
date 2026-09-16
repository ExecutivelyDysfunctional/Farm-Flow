package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MacroEntity
import com.example.ui.viewmodel.CompanionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MacroManagerTab(
    viewModel: CompanionViewModel,
    modifier: Modifier = Modifier
) {
    val macros by viewModel.macros.collectAsState()
    val activeMacro by viewModel.activeMacro.collectAsState()
    val macroIntervalMs by viewModel.macroIntervalMs.collectAsState()
    val logs by viewModel.macroLogs.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newJsCode by remember { mutableStateOf("") }
    var newInterval by remember { mutableStateOf("1000") }
    var newDescription by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Upper Controls: Speed Adjustment & Add Button
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Macro Speed Control",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Global Repeat: ${macroIntervalMs}ms (${String.format("%.1f", macroIntervalMs / 1000f)}s)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                    
                    // Add New Custom Macro
                    Button(
                        onClick = { showCreateDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Macro", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Macro", fontSize = 12.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Speed adjustment slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Fast (500ms)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Slider(
                        value = macroIntervalMs.toFloat(),
                        onValueChange = { viewModel.updateMacroInterval(it.toLong()) },
                        valueRange = 200f..5000f,
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                    )
                    Text("Slow (5s)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // List of Available Macros
        Text(
            text = "Available Task Routines",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(macros, key = { it.id }) { macro ->
                val isRunning = activeMacro?.id == macro.id
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = if (isRunning) 1.5.dp else 0.dp,
                            color = if (isRunning) MaterialTheme.colorScheme.secondary else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isRunning) {
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = macro.name,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    // Custom or System Badge
                                    val badgeBg = if (macro.isPredefined) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                                    val badgeFg = if (macro.isPredefined) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(badgeBg)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (macro.isPredefined) "System" else "Custom",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = badgeFg
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = macro.description,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Delete button for custom macros
                                if (!macro.isPredefined) {
                                    IconButton(
                                        onClick = { viewModel.deleteMacro(macro) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete Custom Macro",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                // Run / Stop Toggle Button
                                Button(
                                    onClick = { viewModel.toggleMacro(macro) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                    ),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                                        contentDescription = if (isRunning) "Stop" else "Run",
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isRunning) "Stop" else "Run",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Live Macro Log Console Terminal
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black)
                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Live Execution Console",
                    fontSize = 10.sp,
                    color = Color.Green,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "ACTIVE: ${if (activeMacro != null) "RUNNING" else "IDLE"}",
                    fontSize = 9.sp,
                    color = if (activeMacro != null) Color.Green else Color.Gray,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                reverseLayout = false
            ) {
                items(logs) { log ->
                    Text(
                        text = log,
                        fontSize = 10.sp,
                        color = Color.Green,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 12.sp
                    )
                }
            }
        }
    }

    // Modal Create Dialog
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create Custom Script Macro", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Macro Name") },
                        placeholder = { Text("e.g. Auto-Sell Iron Ring") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("new_macro_name")
                    )
                    OutlinedTextField(
                        value = newDescription,
                        onValueChange = { newDescription = it },
                        label = { Text("Description") },
                        placeholder = { Text("What does this script do?") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newJsCode,
                        onValueChange = { newJsCode = it },
                        label = { Text("JavaScript Action Code") },
                        placeholder = { Text("(function() { ... })();") },
                        maxLines = 6,
                        modifier = Modifier.fillMaxWidth().height(120.dp)
                    )
                    OutlinedTextField(
                        value = newInterval,
                        onValueChange = { newInterval = it },
                        label = { Text("Interval Interval (ms)") },
                        placeholder = { Text("1000") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val intervalVal = newInterval.toLongOrNull() ?: 1000L
                        if (newName.isNotBlank() && newJsCode.isNotBlank()) {
                            viewModel.addNewMacro(
                                name = newName,
                                jsCode = newJsCode,
                                intervalMs = intervalVal,
                                description = newDescription.ifBlank { "Custom user-crafted JavaScript utility loop." }
                            )
                            // reset
                            newName = ""
                            newJsCode = ""
                            newInterval = "1000"
                            newDescription = ""
                            showCreateDialog = false
                        }
                    }
                ) {
                    Text("Save Macro")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
