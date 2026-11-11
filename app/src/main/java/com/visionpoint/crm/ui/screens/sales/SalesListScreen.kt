package com.visionpoint.crm.ui.screens.sales

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.visionpoint.crm.data.model.SaleWithCustomer
import com.visionpoint.crm.ui.components.AppSearchBar
import com.visionpoint.crm.ui.components.CurrencyText
import com.visionpoint.crm.ui.components.EmptyStateView
import com.visionpoint.crm.ui.components.FilterChipRow
import com.visionpoint.crm.ui.components.StatusChip
import com.visionpoint.crm.ui.theme.DueAmountColor
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesListScreen(
    viewModel: SaleViewModel,
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToCreate: () -> Unit
) {
    val sales by viewModel.salesList.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    val totalNet = sales.sumOf { it.sale.netAmount }
    val totalDue = sales.sumOf { it.sale.dueAmount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Sales & Invoices",
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
                onClick = onNavigateToCreate,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Create Sale")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search & Filter Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppSearchBar(
                    query = searchQuery,
                    onQueryChange = viewModel::setSearchQuery,
                    placeholder = "Search by bill #, customer name or phone..."
                )

                FilterChipRow(
                    options = listOf("All", "Pending", "Ready", "Completed"),
                    selectedOption = statusFilter,
                    onOptionSelected = viewModel::setStatusFilter
                )

                // Quick Summary Header
                if (sales.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${sales.size} Bills",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row {
                                Text("Total: ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                CurrencyText(amount = totalNet, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                            if (totalDue > 0) {
                                Row {
                                    Text("Due: ", style = MaterialTheme.typography.bodySmall, color = DueAmountColor)
                                    CurrencyText(amount = totalDue, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = DueAmountColor)
                                }
                            }
                        }
                    }
                }
            }

            if (sales.isEmpty()) {
                if (searchQuery.isBlank() && statusFilter == "All") {
                    EmptyStateView(
                        title = "No sales recorded yet",
                        message = "Create invoices for customer optical purchases, frames, lenses, and eye testing services.",
                        icon = Icons.Default.ReceiptLong,
                        action = {
                            Button(onClick = onNavigateToCreate) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Create Sale")
                            }
                        }
                    )
                } else {
                    EmptyStateView(
                        title = "No matching sales",
                        message = "No sales found matching your current search or status filter.",
                        icon = Icons.Default.ReceiptLong
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(sales, key = { it.sale.id }) { saleWithCustomer ->
                        SaleListItemCard(
                            saleWithCustomer = saleWithCustomer,
                            dateFormatter = dateFormatter,
                            onClick = { onNavigateToDetail(saleWithCustomer.sale.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SaleListItemCard(
    saleWithCustomer: SaleWithCustomer,
    dateFormatter: DateTimeFormatter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sale = saleWithCustomer.sale
    val customer = saleWithCustomer.customer

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Bill #${sale.id}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    StatusChip(status = sale.status)
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = customer.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "${customer.phone} • ${sale.saleDate.format(dateFormatter)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                CurrencyText(
                    amount = sale.netAmount,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (sale.dueAmount > 0) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Due: ",
                            style = MaterialTheme.typography.labelSmall,
                            color = DueAmountColor
                        )
                        CurrencyText(
                            amount = sale.dueAmount,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = DueAmountColor
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Paid",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
