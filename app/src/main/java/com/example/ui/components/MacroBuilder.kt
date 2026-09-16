package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Macro
import com.example.data.model.MacroStep
import com.example.ui.viewmodel.FarmRpgViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MacroBuilder(
    viewModel: FarmRpgViewModel,
    onSaveSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var macroName by remember { mutableStateOf("") }
    var macroDescription by remember { mutableStateOf("") }
    var intervalMs by remember { mutableStateOf("1000") }
    var repeatCount by remember { mutableStateOf("0") }

    val steps = remember { mutableStateListOf<MacroStep>() }

    // Preset options for streamlined macro creation
    val presets = listOf(
        Triple("Explore Button", "click_text", "Explore"),
        Triple("Explore Again", "click_text", "Explore Again"),
        Triple("Harvest All Crops", "click_text", "Harvest All"),
        Triple("Plant All Seeds", "click_text", "Plant All"),
        Triple("Craft Plank", "click_text", "Craft Wood Plank"),
        Triple("Fishing Cast", "click_text", "Cast Line"),
        Triple("Fish Again", "click_text", "Fish Again")
    )

    // Current step building form state
    var selectedType by remember { mutableStateOf("click_text") }
    var stepTarget by remember { mutableStateOf("") }
    var stepValue by remember { mutableStateOf("") }

    var stepTypeExpanded by remember { mutableStateOf(false) }
    val stepTypes = listOf(
        Pair("click_text", "Click Button with Text"),
        Pair("click_selector", "Click CSS Selector"),
        Pair("wait", "Delay (Wait) in ms"),
        Pair("input", "Input text field"),
        Pair("scroll", "Scroll page vertically"),
        Pair("refresh", "Refresh web page")
    )

    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = "Create Custom Automation Macro",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Configure custom triggers and actions to speed up your routine tasks.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            // General Details Section
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Macro Identity",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = macroName,
                            onValueChange = { macroName = it },
                            label = { Text("Macro Name") },
                            placeholder = { Text("e.g. Iron Ring Crafter") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = macroDescription,
                            onValueChange = { macroDescription = it },
                            label = { Text("Description") },
                            placeholder = { Text("Briefly explain what this does...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = intervalMs,
                                onValueChange = { intervalMs = it },
                                label = { Text("Interval (ms)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = repeatCount,
                                onValueChange = { repeatCount = it },
                                label = { Text("Repeats (0 = inf)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }
                }
            }

            // Quick Preset Addition Row
            item {
                Text(
                    text = "Quick Presets Helper",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp)
                ) {
                    presets.take(4).forEach { (label, type, target) ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    steps.add(
                                        MacroStep(
                                            type = type,
                                            target = target,
                                            value = "",
                                            order = steps.size
                                        )
                                    )
                                }
                        ) {
                            Box(
                                modifier = Modifier.padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // Add Actions Form Section
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Add Action Step",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Type Selection dropdown
                        ExposedDropdownMenuBox(
                            expanded = stepTypeExpanded,
                            onExpandedChange = { stepTypeExpanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = stepTypes.find { it.first == selectedType }?.second ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Action Type") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = stepTypeExpanded) },
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = stepTypeExpanded,
                                onDismissRequest = { stepTypeExpanded = false }
                            ) {
                                stepTypes.forEach { (typeVal, typeLabel) ->
                                    DropdownMenuItem(
                                        text = { Text(typeLabel) },
                                        onClick = {
                                            selectedType = typeVal
                                            stepTypeExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Conditional inputs depending on step type
                        if (selectedType == "click_text" || selectedType == "click_selector" || selectedType == "input") {
                            OutlinedTextField(
                                value = stepTarget,
                                onValueChange = { stepTarget = it },
                                label = {
                                    Text(
                                        if (selectedType == "click_text") "Button Text (e.g. Explore)"
                                        else "CSS Selector (e.g. .btn-explore)"
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (selectedType == "input" || selectedType == "scroll" || selectedType == "wait") {
                            OutlinedTextField(
                                value = stepValue,
                                onValueChange = { stepValue = it },
                                label = {
                                    Text(
                                        when (selectedType) {
                                            "input" -> "Text Input Value"
                                            "scroll" -> "Scroll Amount (pixels, e.g. 200)"
                                            "wait" -> "Delay Milliseconds"
                                            else -> "Value"
                                        }
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = {
                                    if (selectedType.isNotEmpty()) {
                                        steps.add(
                                            MacroStep(
                                                type = selectedType,
                                                target = stepTarget,
                                                value = stepValue,
                                                order = steps.size
                                            )
                                        )
                                        // Reset state
                                        stepTarget = ""
                                        stepValue = ""
                                    }
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Step",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Step")
                            }
                        }
                    }
                }
            }

            // Interactive Steps List
            item {
                Text(
                    text = "Configured Steps (${steps.size})",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (steps.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Add steps using the builder above.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                itemsIndexed(steps) { index, step ->
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = when (step.type) {
                                            "click_text" -> "Click Text: \"${step.target}\""
                                            "click_selector" -> "Click Selector: \"${step.target}\""
                                            "wait" -> "Delay: ${step.value}ms"
                                            "input" -> "Input: \"${step.value}\" in \"${step.target}\""
                                            "scroll" -> "Scroll: ${step.value}px"
                                            "refresh" -> "Refresh Page"
                                            else -> "Custom Step"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            IconButton(
                                onClick = { steps.removeAt(index) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete step",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

        // Save Button Footer
        Button(
            onClick = {
                if (macroName.isNotEmpty() && steps.isNotEmpty()) {
                    val finalMacro = Macro(
                        name = macroName,
                        description = macroDescription.ifEmpty { "Custom created automation macro." },
                        steps = steps.toList(),
                        intervalMs = intervalMs.toLongOrNull() ?: 1000L,
                        repeatCount = repeatCount.toIntOrNull() ?: 0,
                        isSystemBuiltIn = false,
                        isEnabled = true
                    )
                    viewModel.saveMacro(finalMacro)
                    onSaveSuccess()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = macroName.isNotEmpty() && steps.isNotEmpty(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Save,
                contentDescription = "Save Macro"
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Save Automation Macro")
        }
    }
}
