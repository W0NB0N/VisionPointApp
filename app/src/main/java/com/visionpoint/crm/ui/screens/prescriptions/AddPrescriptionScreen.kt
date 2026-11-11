package com.visionpoint.crm.ui.screens.prescriptions

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.visionpoint.crm.ui.components.DatePickerField
import com.visionpoint.crm.ui.components.DropdownField
import com.visionpoint.crm.ui.components.EyePowerPickerDialog

private data class ActivePickerState(
    val eyeSide: String, // "Right" or "Left"
    val field: String,   // "SPH", "CYL", "Add", etc.
    val currentValue: String,
    val isAdd: Boolean
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddPrescriptionScreen(
    customerId: Int,
    prescriptionId: Int? = null,
    viewModel: PrescriptionViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val customerName by viewModel.customerName.collectAsState()
    val date by viewModel.date.collectAsState()
    val type by viewModel.type.collectAsState()
    val doctorName by viewModel.doctorName.collectAsState()
    val eyeFields by viewModel.eyeFields.collectAsState()
    val rightEye by viewModel.rightEye.collectAsState()
    val leftEye by viewModel.leftEye.collectAsState()
    val pd by viewModel.pd.collectAsState()
    val psm by viewModel.psm.collectAsState()
    val fh by viewModel.fh.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val isPreviousLens by viewModel.isPreviousLens.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val prescriptionIdToEdit by viewModel.prescriptionIdToEdit.collectAsState()

    var activePicker by remember { mutableStateOf<ActivePickerState?>(null) }

    val isEditMode = (prescriptionId != null && prescriptionId > 0) || (prescriptionIdToEdit != null && prescriptionIdToEdit!! > 0)

    LaunchedEffect(customerId, prescriptionId) {
        viewModel.init(customerId, prescriptionId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isEditMode) "Edit Prescription" else "Add Prescription",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (customerName.isNotBlank()) {
                            Text(
                                text = "Patient: $customerName",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
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
            // 1. General Info Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "General Details",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            DatePickerField(
                                label = "Date *",
                                selectedDate = date,
                                onDateSelected = viewModel::onDateChange
                            )
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            DropdownField(
                                label = "Type",
                                options = listOf("Specs", "Contact Lens"),
                                selectedOption = type,
                                onOptionSelected = viewModel::onTypeChange
                            )
                        }
                    }

                    OutlinedTextField(
                        value = doctorName,
                        onValueChange = viewModel::onDoctorNameChange,
                        label = { Text("Doctor / Optometrist Name (Optional)") },
                        placeholder = { Text("e.g. Arun Bhalla") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Checkbox: "Prescription as per patient's previous lenses"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.onIsPreviousLensChange(!isPreviousLens) }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isPreviousLens,
                            onCheckedChange = viewModel::onIsPreviousLensChange,
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Prescription as per patient's previous lenses",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // 2. Right Eye Section (RE / OD)
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
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
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Right Eye (OD)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        OutlinedButton(
                            onClick = viewModel::copyRightToLeft,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy to Left", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        maxItemsInEachRow = 3
                    ) {
                        eyeFields.forEach { field ->
                            val isSph = field.equals("SPH", ignoreCase = true)
                            val isCyl = field.equals("CYL", ignoreCase = true)
                            val isAdd = field.equals("Add", ignoreCase = true) || field.equals("ADD", ignoreCase = true)
                            val usePicker = isSph || isCyl || isAdd
                            val currentValue = rightEye[field] ?: ""

                            if (usePicker) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            activePicker = ActivePickerState(
                                                eyeSide = "Right",
                                                field = field,
                                                currentValue = currentValue,
                                                isAdd = isAdd
                                            )
                                        }
                                ) {
                                    OutlinedTextField(
                                        value = currentValue,
                                        onValueChange = {},
                                        label = { Text(field) },
                                        placeholder = { Text("0.00") },
                                        readOnly = true,
                                        singleLine = true,
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.ArrowDropDown,
                                                contentDescription = "Select power",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = false,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            disabledTrailingIconColor = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }
                            } else {
                                OutlinedTextField(
                                    value = currentValue,
                                    onValueChange = { viewModel.onRightEyeFieldChange(field, it) },
                                    label = { Text(field) },
                                    placeholder = { Text(if (field.equals("Axis", ignoreCase = true)) "180" else "6/6") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // 3. Left Eye Section (LE / OS)
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Left Eye (OS)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        maxItemsInEachRow = 3
                    ) {
                        eyeFields.forEach { field ->
                            val isSph = field.equals("SPH", ignoreCase = true)
                            val isCyl = field.equals("CYL", ignoreCase = true)
                            val isAdd = field.equals("Add", ignoreCase = true) || field.equals("ADD", ignoreCase = true)
                            val usePicker = isSph || isCyl || isAdd
                            val currentValue = leftEye[field] ?: ""

                            if (usePicker) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            activePicker = ActivePickerState(
                                                eyeSide = "Left",
                                                field = field,
                                                currentValue = currentValue,
                                                isAdd = isAdd
                                            )
                                        }
                                ) {
                                    OutlinedTextField(
                                        value = currentValue,
                                        onValueChange = {},
                                        label = { Text(field) },
                                        placeholder = { Text("0.00") },
                                        readOnly = true,
                                        singleLine = true,
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.ArrowDropDown,
                                                contentDescription = "Select power",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = false,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            disabledTrailingIconColor = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }
                            } else {
                                OutlinedTextField(
                                    value = currentValue,
                                    onValueChange = { viewModel.onLeftEyeFieldChange(field, it) },
                                    label = { Text(field) },
                                    placeholder = { Text(if (field.equals("Axis", ignoreCase = true)) "180" else "6/6") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // 4. Additional Measurements (PD, PSM, FH)
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Additional Measurements (Optional)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = pd,
                            onValueChange = viewModel::onPdChange,
                            label = { Text("PD (mm)") },
                            placeholder = { Text("62") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = psm,
                            onValueChange = viewModel::onPsmChange,
                            label = { Text("PSM") },
                            placeholder = { Text("PSM") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = fh,
                            onValueChange = viewModel::onFhChange,
                            label = { Text("FH") },
                            placeholder = { Text("FH") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 5. Notes Section
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Prescription Notes (Optional)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = viewModel::onNotesChange,
                        label = { Text("Notes / Observations") },
                        placeholder = { Text("e.g. Progressive lenses advised, check back in 6 months...") },
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 6. Save / Update Prescription Button
            Button(
                onClick = {
                    viewModel.savePrescription {
                        val message = if (isEditMode) "Prescription updated successfully" else "Prescription saved successfully"
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
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
                        text = if (isEditMode) "Update Prescription" else "Save Prescription",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Render Active Eye Power Picker Dialog
    activePicker?.let { pickerState ->
        val dialogTitle = if (pickerState.isAdd) {
            "Select ${pickerState.eyeSide} ADD Power"
        } else {
            "Select ${pickerState.eyeSide} ${pickerState.field} Power"
        }

        EyePowerPickerDialog(
            title = dialogTitle,
            initialValue = pickerState.currentValue,
            isAddPower = pickerState.isAdd,
            onDismiss = { activePicker = null },
            onConfirm = { result ->
                if (pickerState.eyeSide == "Right") {
                    viewModel.onRightEyeFieldChange(pickerState.field, result)
                } else {
                    viewModel.onLeftEyeFieldChange(pickerState.field, result)
                }
                activePicker = null
            },
            onClear = {
                if (pickerState.eyeSide == "Right") {
                    viewModel.onRightEyeFieldChange(pickerState.field, "")
                } else {
                    viewModel.onLeftEyeFieldChange(pickerState.field, "")
                }
                activePicker = null
            }
        )
    }
}
