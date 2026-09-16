package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Remove
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
import com.example.ui.viewmodel.CraftingRecipe

@Composable
fun CalculatorTab(
    viewModel: CompanionViewModel,
    modifier: Modifier = Modifier
) {
    val inventory by viewModel.userInventory.collectAsState()
    val recipes = viewModel.recipes

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Resources Stock Grid
        Text(
            text = "Inventory Material Stock",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.height(130.dp)
        ) {
            items(inventory.keys.toList()) { resource ->
                val qty = inventory[resource] ?: 0
                val icon = when (resource) {
                    "Wood" -> "🪵"
                    "Iron" -> "⛓️"
                    "Stone" -> "🪨"
                    "Clay" -> "🧱"
                    "Straw" -> "🌾"
                    "Feather" -> "🪶"
                    else -> "📦"
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text("$icon $resource", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        
                        // Qty Text
                        Text(
                            text = "$qty",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.testTag("inventory_qty_$resource")
                        )

                        // Plus / Minus adjustments
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { viewModel.updateInventory(resource, qty - 1) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Minus", modifier = Modifier.size(12.dp))
                            }
                            IconButton(
                                onClick = { viewModel.updateInventory(resource, qty + 5) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Plus 5", modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                }
            }
        }

        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

        // Crafting Targets List
        Text(
            text = "Workshop Crafting Estimates",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(recipes) { recipe ->
                // Calculate craftable count
                val craftableCount = recipe.ingredients.map { (ing, req) ->
                    val stock = inventory[ing] ?: 0
                    stock / req
                }.minOrNull() ?: 0

                val totalRevenue = craftableCount * recipe.sellPrice

                // Determine limiting bottlenecks
                val limitingDetails = recipe.ingredients.map { (ing, req) ->
                    val stock = inventory[ing] ?: 0
                    val currentRatio = if (req > 0) stock.toFloat() / req.toFloat() else Float.MAX_VALUE
                    Triple(ing, req, currentRatio)
                }.minByOrNull { it.third }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = recipe.name,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = recipe.description,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    lineHeight = 13.sp
                                )
                            }
                            
                            // Potential Output
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "$craftableCount Ready",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = if (craftableCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = "+$totalRevenue Silver",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Ingredients Info Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            recipe.ingredients.forEach { (ing, req) ->
                                val stock = inventory[ing] ?: 0
                                val hasEnough = stock >= req
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            if (hasEnough) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                            else MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "$ing: $req (Have $stock)",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (hasEnough) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }

                        // Bottleneck Alert
                        limitingDetails?.let { (ing, req, ratio) ->
                            val stock = inventory[ing] ?: 0
                            if (stock < req) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "⚠️ Bottleneck: Need ${req - stock} more $ing to craft another unit.",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
