package com.visionpoint.crm.ui.screens.sales

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.visionpoint.crm.data.model.Customer
import com.visionpoint.crm.data.model.Payment
import com.visionpoint.crm.data.model.Sale
import com.visionpoint.crm.data.model.SaleItem
import com.visionpoint.crm.ui.components.CurrencyText
import com.visionpoint.crm.ui.components.StatusChip
import com.visionpoint.crm.ui.theme.CreditAmountColor
import com.visionpoint.crm.ui.theme.DueAmountColor
import com.visionpoint.crm.util.ContactsHelper
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaleDetailScreen(
    saleId: Int,
    viewModel: SaleViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Int) -> Unit,
    onNavigateToCustomer: (Int) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(saleId) {
        viewModel.loadSaleDetail(saleId)
    }

    val saleWithDetails by viewModel.selectedSaleDetail.collectAsState()
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showStatusDropdown by remember { mutableStateOf(false) }

    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Bill #$saleId",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val d = saleWithDetails ?: return@IconButton
                        coroutineScope.launch {
                            val (shopName, shopAddr, shopPhone) = viewModel.getShopDetails()
                            val pdf = com.visionpoint.crm.util.PdfGenerator.generateBillPdf(
                                context = context,
                                sale = d.sale,
                                items = d.items,
                                payments = d.payments,
                                customer = d.customer,
                                shopDetails = com.visionpoint.crm.util.ShopDetails(shopName, shopPhone, shopAddr)
                            )
                            com.visionpoint.crm.util.ShareUtils.sharePdf(context, pdf, "Bill #${d.sale.id}")
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share Bill PDF")
                    }
                    IconButton(onClick = { onNavigateToEdit(saleId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Bill")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        val details = saleWithDetails
        if (details == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }
        } else {
            val sale = details.sale
            val customer = details.customer
            val items = details.items
            val payments = details.payments

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Bill Overview Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Date: ${sale.saleDate.format(dateFormatter)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                if (sale.recallDate != null) {
                                    Text(
                                        text = "Recall: ${sale.recallDate.format(dateFormatter)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            // Clickable status chip to change status
                            Box {
                                Row(
                                    modifier = Modifier.clickable { showStatusDropdown = true },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    StatusChip(status = sale.status)
                                }

                                DropdownMenu(
                                    expanded = showStatusDropdown,
                                    onDismissRequest = { showStatusDropdown = false }
                                ) {
                                    listOf("Pending", "Ready", "Completed").forEach { statusOption ->
                                        DropdownMenuItem(
                                            text = { Text(statusOption) },
                                            onClick = {
                                                viewModel.updateSaleStatus(statusOption)
                                                showStatusDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Customer Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToCustomer(customer.id) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = customer.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = customer.phone,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = { ContactsHelper.launchDialer(context, customer.phone) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Call", tint = MaterialTheme.colorScheme.primary)
                        }

                        IconButton(
                            onClick = { ContactsHelper.launchWhatsApp(context, customer.phone) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = "WhatsApp", tint = MaterialTheme.colorScheme.tertiary)
                        }
                    }
                }

                // 3. Items Breakdown Table
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Purchased Items (${items.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        items.forEachIndexed { idx, item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${idx + 1}. ${item.name}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    val itemDetailText = listOfNotNull(
                                        item.itemType,
                                        item.itemDetail?.ifBlank { null }?.let { "Internal Ref: $it" }
                                    ).joinToString(" • ")

                                    if (itemDetailText.isNotBlank()) {
                                        Text(
                                            text = itemDetailText,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Text(
                                    text = "${item.quantity} x ₹${String.format("%.0f", item.price)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )

                                CurrencyText(
                                    amount = item.amount,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (idx < items.size - 1) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            }
                        }
                    }
                }

                // 4. Financial Breakdown
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Subtotal", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            CurrencyText(amount = sale.subtotal, style = MaterialTheme.typography.bodyMedium)
                        }

                        val discountAmount = if (sale.discountType == "amount") sale.discountAmount else (sale.subtotal * sale.discountPercent / 100.0)
                        if (discountAmount > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val discountLabel = if (sale.discountType == "amount") "Discount" else "Discount (${sale.discountPercent}%)"
                                Text(discountLabel, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("- ₹${String.format("%.2f", discountAmount)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Net Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            CurrencyText(amount = sale.netAmount, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Paid", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            CurrencyText(amount = sale.receivedAmount, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = CreditAmountColor)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Balance Due", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (sale.dueAmount > 0) DueAmountColor else MaterialTheme.colorScheme.onSurface)
                            CurrencyText(amount = sale.dueAmount, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (sale.dueAmount > 0) DueAmountColor else CreditAmountColor)
                        }
                    }
                }

                // 5. Payment Records
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Payment History (${payments.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            }

                            if (sale.dueAmount > 0) {
                                OutlinedButton(
                                    onClick = { showPaymentDialog = true },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                ) {
                                    Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Payment", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }

                        if (payments.isEmpty()) {
                            Text(
                                text = "No payments recorded yet.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            payments.forEachIndexed { pIdx, payment ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${payment.date.format(dateFormatter)} (${payment.method})",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    CurrencyText(
                                        amount = payment.amount,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = CreditAmountColor
                                    )
                                }
                                if (pIdx < payments.size - 1) {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                }
                            }
                        }
                    }
                }

                // 6. Invoice Actions (WhatsApp PDF, Share PDF, View PDF)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Invoice Sharing & Export",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // WhatsApp Bill with PDF attached
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val (shopName, shopAddr, shopPhone) = viewModel.getShopDetails()
                                    val pdf = com.visionpoint.crm.util.PdfGenerator.generateBillPdf(
                                        context = context,
                                        sale = sale,
                                        items = items,
                                        payments = payments,
                                        customer = customer,
                                        shopDetails = com.visionpoint.crm.util.ShopDetails(shopName, shopPhone, shopAddr)
                                    )
                                    val msg = "Hello ${customer.name}, please find attached your Tax Invoice (Bill #${sale.id}) from $shopName. Thank you for your business!"
                                    com.visionpoint.crm.util.ShareUtils.shareViaWhatsApp(
                                        context = context,
                                        phone = customer.phone,
                                        message = msg,
                                        pdfFile = pdf
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("WhatsApp Bill (PDF)", fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch {
                                        val (shopName, shopAddr, shopPhone) = viewModel.getShopDetails()
                                        val pdf = com.visionpoint.crm.util.PdfGenerator.generateBillPdf(
                                            context = context,
                                            sale = sale,
                                            items = items,
                                            payments = payments,
                                            customer = customer,
                                            shopDetails = com.visionpoint.crm.util.ShopDetails(shopName, shopPhone, shopAddr)
                                        )
                                        com.visionpoint.crm.util.ShareUtils.sharePdf(context, pdf, "Bill #${sale.id}")
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share PDF")
                            }

                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch {
                                        val (shopName, shopAddr, shopPhone) = viewModel.getShopDetails()
                                        val pdf = com.visionpoint.crm.util.PdfGenerator.generateBillPdf(
                                            context = context,
                                            sale = sale,
                                            items = items,
                                            payments = payments,
                                            customer = customer,
                                            shopDetails = com.visionpoint.crm.util.ShopDetails(shopName, shopPhone, shopAddr)
                                        )
                                        com.visionpoint.crm.util.ShareUtils.viewPdf(context, pdf)
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("View PDF")
                            }
                        }
                    }
                }

                // 7. Status-based WhatsApp Reminder Button
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val msg = viewModel.getStatusReminderMessage(
                                customerName = customer.name,
                                status = sale.status,
                                dueAmount = sale.dueAmount,
                                netAmount = sale.netAmount,
                                billId = sale.id
                            )
                            ContactsHelper.launchWhatsApp(context, customer.phone, msg)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when (sale.status) {
                            "Ready" -> MaterialTheme.colorScheme.primary
                            "Completed" -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.tertiary
                        }
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    val actionLabel = when (sale.status) {
                        "Ready" -> "Send 'Spectacle Ready' Alert via WhatsApp"
                        "Completed" -> "Send 'Thank You' Message via WhatsApp"
                        else -> "Send Status Update via WhatsApp"
                    }
                    Text(actionLabel, fontWeight = FontWeight.Bold)
                }

                // Additional quick action: Due Reminder if balance is pending
                if (sale.dueAmount > 0) {
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                val msg = viewModel.getDueReminderMessage(
                                    customerName = customer.name,
                                    dueAmount = sale.dueAmount,
                                    billId = sale.id
                                )
                                ContactsHelper.launchWhatsApp(context, customer.phone, msg)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null, tint = DueAmountColor, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Send Due Reminder via WhatsApp", color = DueAmountColor, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Add Payment Dialog Modal
        if (showPaymentDialog && saleWithDetails != null) {
            AddPaymentDialog(
                currentDueAmount = saleWithDetails!!.sale.dueAmount,
                onDismiss = { showPaymentDialog = false },
                onConfirmPayment = { amount, method, date ->
                    viewModel.addPaymentToSale(amount, method, date) {
                        Toast.makeText(context, "Payment of ₹$amount recorded", Toast.LENGTH_SHORT).show()
                        showPaymentDialog = false
                    }
                }
            )
        }
    }
}
