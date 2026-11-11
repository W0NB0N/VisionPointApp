package com.visionpoint.crm.ui.screens.cashbook

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.visionpoint.crm.ui.components.DatePickerField
import com.visionpoint.crm.ui.components.DropdownField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCashbookEntryScreen(
    viewModel: CashbookViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    val entryDate by viewModel.entryDate.collectAsState()
    val entryType by viewModel.entryType.collectAsState()
    val entryAmount by viewModel.entryAmount.collectAsState()
    val entryMethod by viewModel.entryMethod.collectAsState()
    val entryNote by viewModel.entryNote.collectAsState()
    val entryAmountError by viewModel.entryAmountError.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.initAddEntry()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Add Cashbook Entry",
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
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Type selector (Credit vs Debit)
                    Text(
                        text = "Transaction Type *",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilterChip(
                            selected = entryType == "Credit",
                            onClick = { viewModel.onEntryTypeChange("Credit") },
                            label = { Text("Credit (Income +)") },
                            modifier = Modifier.weight(1f)
                        )

                        FilterChip(
                            selected = entryType == "Debit",
                            onClick = { viewModel.onEntryTypeChange("Debit") },
                            label = { Text("Debit (Expense -)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Date & Amount Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            DatePickerField(
                                label = "Date *",
                                selectedDate = entryDate,
                                onDateSelected = viewModel::onEntryDateChange
                            )
                        }

                        OutlinedTextField(
                            value = entryAmount,
                            onValueChange = viewModel::onEntryAmountChange,
                            label = { Text("Amount (₹) *") },
                            placeholder = { Text("0") },
                            singleLine = true,
                            isError = entryAmountError != null,
                            supportingText = entryAmountError?.let { { Text(it) } },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Payment Method Dropdown
                    DropdownField(
                        label = "Payment Method",
                        options = listOf("Cash", "Card", "UPI"),
                        selectedOption = entryMethod,
                        onOptionSelected = viewModel::onEntryMethodChange
                    )

                    // Note / Description
                    OutlinedTextField(
                        value = entryNote,
                        onValueChange = viewModel::onEntryNoteChange,
                        label = { Text("Note / Description (Optional)") },
                        placeholder = { Text("e.g. Shop rent, Tea/Snacks, Extra frame sale") },
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Save Button
            Button(
                onClick = {
                    viewModel.saveEntry {
                        Toast.makeText(context, "Cashbook entry saved", Toast.LENGTH_SHORT).show()
                        onNavigateBack()
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
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Save Entry",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
