package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CalculatorTab
import com.example.ui.components.MacroBrowser
import com.example.ui.components.MacroManagerTab
import com.example.ui.components.TimersTab
import com.example.ui.viewmodel.CompanionViewModel

sealed class CompanionTab(val title: String, val icon: ImageVector) {
    object Macros : CompanionTab("Macros", Icons.Default.PlayArrow)
    object Alarms : CompanionTab("Alarms", Icons.Default.Alarm)
    object Calculator : CompanionTab("Calculator", Icons.Default.Calculate)
}

@Composable
fun MainLayout(
    viewModel: CompanionViewModel,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf<CompanionTab>(CompanionTab.Macros) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Upper Half: FarmRPG Web View and Navigation Shortcuts
        Box(
            modifier = Modifier
                .weight(1.1f)
                .fillMaxWidth()
        ) {
            MacroBrowser(viewModel = viewModel)
        }

        // Horizontal Separator / Divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        )

        // Lower Half: Companion Utility Deck with M3 Tabs
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // Tab Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(vertical = 4.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val tabs = listOf(CompanionTab.Macros, CompanionTab.Alarms, CompanionTab.Calculator)
                tabs.forEach { tab ->
                    val isSelected = activeTab == tab
                    val tabBgColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    val tabFgColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(tabBgColor)
                            .clickable { activeTab = tab }
                            .padding(vertical = 8.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title,
                            tint = tabFgColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = tab.title,
                            color = tabFgColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Tab Content Display Screen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (activeTab) {
                    CompanionTab.Macros -> MacroManagerTab(viewModel = viewModel)
                    CompanionTab.Alarms -> TimersTab(viewModel = viewModel)
                    CompanionTab.Calculator -> CalculatorTab(viewModel = viewModel)
                }
            }
        }
    }
}
