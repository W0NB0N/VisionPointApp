package com.visionpoint.crm.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

@Composable
fun EyePowerPickerDialog(
    title: String,
    initialValue: String = "",
    isAddPower: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    onClear: () -> Unit
) {
    val signs = listOf("+", "-")
    val intValues = if (isAddPower) (0..6).map { it.toString() } else (0..12).map { it.toString() }
    val decimals = listOf("00", "25", "50", "75")

    // Parse initial value
    val parsedSign = when {
        initialValue.startsWith("-") -> "-"
        else -> "+"
    }
    val cleanValue = initialValue.removePrefix("+").removePrefix("-")
    val parts = cleanValue.split(".")
    val parsedInt = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val rawDec = parts.getOrNull(1)?.padEnd(2, '0')?.take(2) ?: "00"
    val parsedDec = if (rawDec in decimals) rawDec else "00"

    var selectedSignIndex by remember {
        mutableIntStateOf(signs.indexOf(parsedSign).coerceAtLeast(0))
    }
    var selectedIntIndex by remember {
        mutableIntStateOf(intValues.indexOf(parsedInt.toString()).coerceIn(0, intValues.size - 1))
    }
    var selectedDecIndex by remember {
        mutableIntStateOf(decimals.indexOf(parsedDec).coerceIn(0, decimals.size - 1))
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                // Wheel container with top/bottom frame lines
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(132.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Two horizontal framing lines for the center selected row
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        HorizontalDivider(
                            thickness = 1.5.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(44.dp))
                        HorizontalDivider(
                            thickness = 1.5.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Columns
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!isAddPower) {
                            // Sign column (+ / -)
                            PowerWheelColumn(
                                items = signs,
                                selectedIndex = selectedSignIndex,
                                onSelect = { selectedSignIndex = it },
                                modifier = Modifier.width(60.dp)
                            )
                        }

                        // Integer column (0..12 or 0..6)
                        PowerWheelColumn(
                            items = intValues,
                            selectedIndex = selectedIntIndex,
                            onSelect = { selectedIntIndex = it },
                            modifier = Modifier.width(60.dp)
                        )

                        // Decimal separator dot
                        Text(
                            text = ".",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        // Decimal column (00, 25, 50, 75)
                        PowerWheelColumn(
                            items = decimals,
                            selectedIndex = selectedDecIndex,
                            onSelect = { selectedDecIndex = it },
                            modifier = Modifier.width(60.dp)
                        )
                    }
                }

                // Action Buttons: Clear & OK
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            onClear()
                            onDismiss()
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text("Clear", fontWeight = FontWeight.Medium)
                    }

                    Button(
                        onClick = {
                            val sign = if (isAddPower) "+" else signs[selectedSignIndex]
                            val integer = intValues[selectedIntIndex]
                            val decimal = decimals[selectedDecIndex]

                            val result = if (isAddPower) {
                                if (integer == "0" && decimal == "00") "" else "+$integer.$decimal"
                            } else {
                                "$sign$integer.$decimal"
                            }
                            onConfirm(result)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(44.dp)
                    ) {
                        Text("OK", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PowerWheelColumn(
    items: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    itemHeight: Dp = 44.dp
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)
    val coroutineScope = rememberCoroutineScope()
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    LaunchedEffect(selectedIndex) {
        if (listState.firstVisibleItemIndex != selectedIndex) {
            listState.animateScrollToItem(selectedIndex)
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { index ->
                if (index in items.indices) {
                    onSelect(index)
                }
            }
    }

    Box(
        modifier = modifier.height(itemHeight * 3),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top padding item
            item {
                Spacer(modifier = Modifier.height(itemHeight))
            }

            items(items.size) { index ->
                val isSelected = index == listState.firstVisibleItemIndex
                val alpha by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0.35f,
                    label = "wheelAlpha"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .clickable {
                            coroutineScope.launch {
                                listState.animateScrollToItem(index)
                                onSelect(index)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = items[index],
                        fontSize = if (isSelected) 24.sp else 18.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.alpha(alpha)
                    )
                }
            }

            // Bottom padding item
            item {
                Spacer(modifier = Modifier.height(itemHeight))
            }
        }
    }
}
