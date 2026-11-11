package com.visionpoint.crm.ui.screens.cashbook

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.visionpoint.crm.data.model.Cashbook
import com.visionpoint.crm.ui.components.ConfirmDialog
import com.visionpoint.crm.ui.components.CurrencyText
import com.visionpoint.crm.ui.components.EmptyStateView
import com.visionpoint.crm.ui.components.FilterChipRow
import com.visionpoint.crm.ui.theme.CreditAmountColor
import com.visionpoint.crm.ui.theme.DebitAmountColor
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashbookScreen(
    viewModel: CashbookViewModel,
    onNavigateToAddEntry: () -> Unit
) {
    val entries by viewModel.entries.collectAsState()
    val totalCredit by viewModel.totalCredit.collectAsState()
    val totalDebit by viewModel.totalDebit.collectAsState()
    val netEarnings by viewModel.netEarnings.collectAsState()

    val typeFilter by viewModel.typeFilter.collectAsState()
    val methodFilter by viewModel.methodFilter.collectAsState()

    var entryToDelete by remember { mutableStateOf<Cashbook?>(null) }
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Cashbook Ledger",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddEntry,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Entry")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Summary Cards Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CashbookSummaryPill(
                    title = "Total In (Credit)",
                    amount = totalCredit,
                    amountColor = CreditAmountColor,
                    modifier = Modifier.weight(1f)
                )

                CashbookSummaryPill(
                    title = "Total Out (Debit)",
                    amount = totalDebit,
                    amountColor = DebitAmountColor,
                    modifier = Modifier.weight(1f)
                )

                CashbookSummaryPill(
                    title = "Net Balance",
                    amount = netEarnings,
                    amountColor = if (netEarnings >= 0) MaterialTheme.colorScheme.primary else DebitAmountColor,
                    modifier = Modifier.weight(1f)
                )
            }

            // Filters
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FilterChipRow(
                    options = listOf("All", "Credit", "Debit"),
                    selectedOption = typeFilter,
                    onOptionSelected = viewModel::setTypeFilter
                )

                FilterChipRow(
                    options = listOf("All", "Cash", "Card", "UPI"),
                    selectedOption = methodFilter,
                    onOptionSelected = viewModel::setMethodFilter
                )
            }

            if (entries.isEmpty()) {
                EmptyStateView(
                    title = "No cashbook records",
                    message = "All billing payments automatically appear here. You can also add manual income or expense entries.",
                    icon = Icons.Default.AccountBalanceWallet,
                    action = {
                        Button(onClick = onNavigateToAddEntry) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Manual Entry")
                        }
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(entries, key = { it.id }) { entry ->
                        CashbookEntryCard(
                            entry = entry,
                            dateFormatter = dateFormatter,
                            onDelete = { entryToDelete = entry }
                        )
                    }
                }
            }
        }

        // Delete Confirm Dialog
        if (entryToDelete != null) {
            ConfirmDialog(
                title = "Delete Entry",
                message = "Are you sure you want to delete this ${entryToDelete?.type} entry of ₹${entryToDelete?.amount}?",
                confirmText = "Delete",
                onConfirm = {
                    entryToDelete?.let { viewModel.deleteEntry(it) }
                    entryToDelete = null
                },
                onDismiss = { entryToDelete = null }
            )
        }
    }
}

@Composable
fun CashbookSummaryPill(
    title: String,
    amount: Double,
    amountColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            CurrencyText(
                amount = amount,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
        }
    }
}

@Composable
fun CashbookEntryCard(
    entry: Cashbook,
    dateFormatter: DateTimeFormatter,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCredit = entry.type.equals("Credit", ignoreCase = true)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        if (isCredit) CreditAmountColor.copy(alpha = 0.15f)
                        else DebitAmountColor.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isCredit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    contentDescription = entry.type,
                    tint = if (isCredit) CreditAmountColor else DebitAmountColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.note?.ifBlank { entry.type } ?: entry.type,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${entry.date.format(dateFormatter)} • ${entry.method ?: "Cash"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Amount
            Text(
                text = "${if (isCredit) "+" else "-"} ₹${String.format("%.2f", entry.amount)}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isCredit) CreditAmountColor else DebitAmountColor
            )

            // Option to delete manual entries
            if (entry.paymentId == null) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
