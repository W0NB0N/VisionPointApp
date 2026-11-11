package com.visionpoint.crm.ui.screens.sales

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.visionpoint.crm.ui.components.CurrencyText
import com.visionpoint.crm.ui.components.DatePickerField
import com.visionpoint.crm.ui.components.DropdownField
import com.visionpoint.crm.ui.theme.DueAmountColor
import java.time.LocalDate

@Composable
fun AddPaymentDialog(
    currentDueAmount: Double,
    onDismiss: () -> Unit,
    onConfirmPayment: (amount: Double, method: String, date: LocalDate) -> Unit
) {
    val initialAmount = remember(currentDueAmount) {
        if (currentDueAmount > 0) {
            String.format("%.2f", currentDueAmount).trimEnd('0').trimEnd('.')
        } else ""
    }
    var amountInput by remember { mutableStateOf(initialAmount) }
    var selectedMethod by remember { mutableStateOf("Cash") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var amountError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Payment,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = "Record Payment",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Outstanding Due:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    CurrencyText(
                        amount = currentDueAmount,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = DueAmountColor
                    )
                }

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { input ->
                        val sanitized = input.filter { it.isDigit() || it == '.' }
                        if (sanitized.count { it == '.' } <= 1) {
                            amountInput = sanitized
                            amountError = null
                        }
                    },
                    label = { Text("Payment Amount (₹) *") },
                    placeholder = { Text("e.g. 500") },
                    singleLine = true,
                    isError = amountError != null,
                    supportingText = amountError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                DropdownField(
                    label = "Payment Method",
                    options = listOf("Cash", "Card", "UPI"),
                    selectedOption = selectedMethod,
                    onOptionSelected = { selectedMethod = it }
                )

                DatePickerField(
                    label = "Payment Date",
                    selectedDate = selectedDate,
                    onDateSelected = { selectedDate = it }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = amountInput.toDoubleOrNull()
                    if (parsed == null || parsed <= 0) {
                        amountError = "Enter a valid payment amount"
                    } else {
                        onConfirmPayment(parsed, selectedMethod, selectedDate)
                    }
                }
            ) {
                Text("Record Payment")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
