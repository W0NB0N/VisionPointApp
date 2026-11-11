package com.visionpoint.crm.ui.screens.sales

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.visionpoint.crm.data.model.Customer
import com.visionpoint.crm.ui.components.CurrencyText
import com.visionpoint.crm.ui.components.DatePickerField
import com.visionpoint.crm.ui.components.DropdownField
import com.visionpoint.crm.ui.theme.DueAmountColor
import java.time.format.DateTimeFormatter
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditSaleScreen(
    viewModel: SaleViewModel,
    saleId: Int? = null,
    preselectedCustomerId: Int? = null,
    onNavigateBack: () -> Unit,
    onSaved: (Int) -> Unit
) {
    val context = LocalContext.current

    val availableCustomers by viewModel.availableCustomers.collectAsState()
    val selectedCustomer by viewModel.selectedCustomer.collectAsState()
    val customerPrescriptions by viewModel.customerPrescriptions.collectAsState()
    val selectedPrescriptionId by viewModel.selectedPrescriptionId.collectAsState()

    val saleDate by viewModel.saleDate.collectAsState()
    val recallDate by viewModel.recallDate.collectAsState()
    val items by viewModel.items.collectAsState()
    val discountType by viewModel.discountType.collectAsState()
    val discountPercentInput by viewModel.discountPercentInput.collectAsState()
    val discountAmountInput by viewModel.discountAmountInput.collectAsState()
    val payments by viewModel.payments.collectAsState()
    val saleStatus by viewModel.saleStatus.collectAsState()
    val availableItemTypes by viewModel.availableItemTypes.collectAsState()

    val isSaving by viewModel.isSaving.collectAsState()
    val formError by viewModel.formError.collectAsState()

    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    // Derived Financials
    val subtotal = items.sumOf { it.amount }
    val effectiveDiscount = if (discountType == "amount") {
        val amt = discountAmountInput.toDoubleOrNull() ?: 0.0
        amt.coerceAtMost(subtotal)
    } else {
        val pct = (discountPercentInput.toDoubleOrNull() ?: 0.0).coerceIn(0.0, 100.0)
        subtotal * pct / 100.0
    }
    val netAmount = max(0.0, subtotal - effectiveDiscount)
    val totalPaid = payments.sumOf { it.amount }
    val dueAmount = max(0.0, netAmount - totalPaid)

    LaunchedEffect(saleId, preselectedCustomerId) {
        viewModel.initSaleForm(saleId, preselectedCustomerId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (saleId != null) "Edit Bill #$saleId" else "Create New Sale",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Error banner
            if (formError != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = formError ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // 1. Customer Selection Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Customer Information",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    CustomerSelector(
                        customers = availableCustomers,
                        selectedCustomer = selectedCustomer,
                        onCustomerSelected = viewModel::onCustomerSelected,
                        onClearCustomer = viewModel::clearCustomer
                    )

                    // Linked Prescription Dropdown (if customer is selected)
                    if (selectedCustomer != null) {
                        if (customerPrescriptions.isNotEmpty()) {
                            val prescriptionOptions = listOf("None") + customerPrescriptions.map {
                                "${it.type ?: "Specs"} (${it.date.format(dateFormatter)})"
                            }
                            val currentPrescriptionLabel = customerPrescriptions.find { it.id == selectedPrescriptionId }?.let {
                                "${it.type ?: "Specs"} (${it.date.format(dateFormatter)})"
                            } ?: "None"

                            DropdownField(
                                label = "Linked Prescription",
                                options = prescriptionOptions,
                                selectedOption = currentPrescriptionLabel,
                                onOptionSelected = { selectedLabel ->
                                    if (selectedLabel == "None") {
                                        viewModel.onPrescriptionSelected(null)
                                    } else {
                                        val match = customerPrescriptions.find {
                                            "${it.type ?: "Specs"} (${it.date.format(dateFormatter)})" == selectedLabel
                                        }
                                        viewModel.onPrescriptionSelected(match?.id)
                                    }
                                }
                            )
                        } else {
                            Text(
                                text = "No prescriptions recorded for this customer yet.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 2. Dates Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        DatePickerField(
                            label = "Bill Date *",
                            selectedDate = saleDate,
                            onDateSelected = viewModel::onSaleDateChange
                        )
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        DatePickerField(
                            label = "Recall Date",
                            selectedDate = recallDate,
                            onDateSelected = { viewModel.onRecallDateChange(it) },
                            placeholder = "Follow-up"
                        )
                    }
                }
            }

            // 3. Items Section
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Items (${items.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = viewModel::addItem,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Item", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    items.forEachIndexed { index, item ->
                        SaleItemEditorCard(
                            item = item,
                            itemNumber = index + 1,
                            availableCategories = availableItemTypes,
                            canRemove = items.size > 1,
                            onUpdate = { updated -> viewModel.updateItem(index, updated) },
                            onRemove = { viewModel.removeItem(index) }
                        )
                    }
                }
            }

            // 4. Totals & Discount Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Subtotal",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        CurrencyText(
                            amount = subtotal,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Discount",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier.padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilterChip(
                                    selected = discountType == "percentage",
                                    onClick = { viewModel.onDiscountTypeChange("percentage") },
                                    label = { Text("%", style = MaterialTheme.typography.labelSmall) }
                                )
                                FilterChip(
                                    selected = discountType == "amount",
                                    onClick = { viewModel.onDiscountTypeChange("amount") },
                                    label = { Text("₹", style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }

                        if (discountType == "percentage") {
                            OutlinedTextField(
                                value = discountPercentInput,
                                onValueChange = viewModel::onDiscountInputChange,
                                placeholder = { Text("0") },
                                trailingIcon = { Text("%", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(end = 8.dp)) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.width(110.dp)
                            )
                        } else {
                            OutlinedTextField(
                                value = discountAmountInput,
                                onValueChange = viewModel::onDiscountAmountInputChange,
                                placeholder = { Text("0") },
                                leadingIcon = { Text("₹", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 8.dp)) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.width(120.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Net Amount",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        CurrencyText(
                            amount = netAmount,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // 5. Initial Payment Section (Only on New Sale creation)
            if (saleId == null) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Payment,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Payment Record",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = viewModel::addPayment,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Payment", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        if (payments.isEmpty()) {
                            Text(
                                text = "No advance payment added yet. Full balance will be marked as Due.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            payments.forEachIndexed { index, payment ->
                                PaymentEditorRow(
                                    payment = payment,
                                    onUpdate = { updated -> viewModel.updatePayment(index, updated) },
                                    onRemove = { viewModel.removePayment(index) }
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row {
                                Text("Received: ", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                CurrencyText(amount = totalPaid, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            }
                            Row {
                                Text("Balance Due: ", style = MaterialTheme.typography.bodyMedium, color = DueAmountColor)
                                CurrencyText(amount = dueAmount, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = DueAmountColor)
                            }
                        }
                    }
                }
            }

            // 6. Status Selector
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(14.dp)) {
                    DropdownField(
                        label = "Invoice Status",
                        options = listOf("Pending", "Ready", "Completed"),
                        selectedOption = saleStatus,
                        onOptionSelected = viewModel::onStatusChange
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Save Sale Button
            Button(
                onClick = {
                    viewModel.saveSale { savedId ->
                        Toast.makeText(context, "Sale invoice saved successfully", Toast.LENGTH_SHORT).show()
                        onSaved(savedId)
                    }
                },
                enabled = !isSaving,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = if (saleId != null) "Update Bill" else "Generate Bill",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerSelector(
    customers: List<Customer>,
    selectedCustomer: Customer?,
    onCustomerSelected: (Customer) -> Unit,
    onClearCustomer: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var filterText by remember { mutableStateOf("") }

    val filteredCustomers = remember(filterText, customers) {
        if (filterText.isBlank()) customers
        else customers.filter {
            it.name.contains(filterText, ignoreCase = true) || it.phone.contains(filterText)
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = if (selectedCustomer != null) "${selectedCustomer.name} (${selectedCustomer.phone})" else filterText,
            onValueChange = { input ->
                if (selectedCustomer != null) {
                    onClearCustomer()
                }
                filterText = input
                expanded = true
            },
            label = { Text("Select Customer *") },
            placeholder = { Text("Type name or phone...") },
            trailingIcon = {
                if (selectedCustomer != null || filterText.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            onClearCustomer()
                            filterText = ""
                            expanded = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear Customer Selection"
                        )
                    }
                } else {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded && filteredCustomers.isNotEmpty(),
            onDismissRequest = { expanded = false }
        ) {
            filteredCustomers.take(15).forEach { customer ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(text = customer.name, fontWeight = FontWeight.SemiBold)
                            Text(text = customer.phone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    onClick = {
                        onCustomerSelected(customer)
                        filterText = ""
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun SaleItemEditorCard(
    item: SaleItemFormState,
    itemNumber: Int,
    availableCategories: List<String>,
    canRemove: Boolean,
    onUpdate: (SaleItemFormState) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1: Header + Remove
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Item #$itemNumber",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                if (canRemove) {
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove item",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Category & Type Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    DropdownField(
                        label = "Category",
                        options = listOf("Product", "Service"),
                        selectedOption = item.category,
                        onOptionSelected = { onUpdate(item.copy(category = it)) }
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    DropdownField(
                        label = "Type",
                        options = availableCategories,
                        selectedOption = item.itemType,
                        onOptionSelected = { onUpdate(item.copy(itemType = it)) }
                    )
                }
            }

            // Item Name (Visible on bill)
            OutlinedTextField(
                value = item.name,
                onValueChange = { onUpdate(item.copy(name = it)) },
                label = { Text("Item Name (Visible on Bill) *") },
                placeholder = { Text("e.g. Ray-Ban Aviator / Single Vision Lens") },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Internal Details (Workshop/Lab reference only - not on bill)
            OutlinedTextField(
                value = item.itemDetail,
                onValueChange = { onUpdate(item.copy(itemDetail = it)) },
                label = { Text("Internal Details (Reference Only - Not shown on bill)") },
                placeholder = { Text("e.g. Model #8821, Bevel 1.2, Anti-glare coating code") },
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Price, Qty & Amount Row (Preserves decimal input properly)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = item.priceInput,
                    onValueChange = { input ->
                        val sanitized = input.filter { it.isDigit() || it == '.' }
                        if (sanitized.count { it == '.' } <= 1) {
                            onUpdate(item.copy(priceInput = sanitized))
                        }
                    },
                    label = { Text("Price (₹) *") },
                    placeholder = { Text("0") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1.2f)
                )

                OutlinedTextField(
                    value = item.quantityInput,
                    onValueChange = { input ->
                        val sanitized = input.filter { it.isDigit() }
                        onUpdate(item.copy(quantityInput = sanitized))
                    },
                    label = { Text("Qty") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(0.8f)
                )

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text("Amount", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    CurrencyText(amount = item.amount, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                }
            }
        }
    }
}

@Composable
fun PaymentEditorRow(
    payment: PaymentFormState,
    onUpdate: (PaymentFormState) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = payment.amountInput,
                onValueChange = { input ->
                    val sanitized = input.filter { it.isDigit() || it == '.' }
                    if (sanitized.count { it == '.' } <= 1) {
                        onUpdate(payment.copy(amountInput = sanitized))
                    }
                },
                label = { Text("Amount (₹)") },
                placeholder = { Text("0") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1.2f)
            )

            Box(modifier = Modifier.weight(1f)) {
                DropdownField(
                    label = "Method",
                    options = listOf("Cash", "Card", "UPI"),
                    selectedOption = payment.method,
                    onOptionSelected = { onUpdate(payment.copy(method = it)) }
                )
            }

            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Remove payment", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
            }
        }
    }
}
