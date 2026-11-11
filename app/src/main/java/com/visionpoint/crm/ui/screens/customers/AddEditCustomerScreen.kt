package com.visionpoint.crm.ui.screens.customers

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.visionpoint.crm.ui.components.DatePickerField
import com.visionpoint.crm.ui.components.DropdownField
import com.visionpoint.crm.util.ContactsHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCustomerScreen(
    viewModel: CustomerViewModel,
    customerId: Int? = null,
    onNavigateBack: () -> Unit,
    onSaved: (Int) -> Unit
) {
    val context = LocalContext.current
    val name by viewModel.name.collectAsState()
    val phone by viewModel.phone.collectAsState()
    val gender by viewModel.gender.collectAsState()
    val dob by viewModel.dob.collectAsState()
    val age by viewModel.age.collectAsState()
    val address by viewModel.address.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val nameError by viewModel.nameError.collectAsState()
    val phoneError by viewModel.phoneError.collectAsState()
    val generalError by viewModel.generalError.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    var saveToContactsChecked by remember { mutableStateOf(customerId == null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val readGranted = permissions[android.Manifest.permission.READ_CONTACTS] == true
        val writeGranted = permissions[android.Manifest.permission.WRITE_CONTACTS] == true
        if (readGranted && writeGranted) {
            ContactsHelper.saveCustomerToContacts(context, name.trim(), phone.trim())
        }
    }

    LaunchedEffect(customerId) {
        viewModel.initEditCustomer(customerId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (customerId != null) "Edit Customer" else "Add New Customer",
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
            // General Error banner
            if (generalError != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = generalError ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Name Field
            OutlinedTextField(
                value = name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Customer Name *") },
                placeholder = { Text("e.g. Ramesh Kumar") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null)
                },
                isError = nameError != null,
                supportingText = nameError?.let { { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    keyboardType = KeyboardType.Text
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Phone Field
            OutlinedTextField(
                value = phone,
                onValueChange = viewModel::onPhoneChange,
                label = { Text("Phone Number *") },
                placeholder = { Text("e.g. 9876543210") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Phone, contentDescription = null)
                },
                isError = phoneError != null,
                supportingText = phoneError?.let { { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Gender & Age Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    DropdownField(
                        label = "Gender",
                        options = listOf("Male", "Female", "Other"),
                        selectedOption = gender ?: "Male",
                        onOptionSelected = viewModel::onGenderChange
                    )
                }

                OutlinedTextField(
                    value = age?.toString() ?: "",
                    onValueChange = { str ->
                        val parsed = str.filter { it.isDigit() }.toIntOrNull()
                        viewModel.onAgeChange(parsed)
                    },
                    label = { Text("Age (yrs)") },
                    placeholder = { Text("e.g. 45") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            // Date of Birth
            DatePickerField(
                label = "Date of Birth (Optional)",
                selectedDate = dob,
                onDateSelected = viewModel::onDobChange,
                placeholder = "Select Date of Birth",
                modifier = Modifier.fillMaxWidth()
            )

            // Address Field
            OutlinedTextField(
                value = address,
                onValueChange = viewModel::onAddressChange,
                label = { Text("Customer Address (Optional)") },
                placeholder = { Text("e.g. Flat 101, Galaxy Apts, MG Road, City") },
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = viewModel::onNotesChange,
                label = { Text("Notes / Medical History (Optional)") },
                placeholder = { Text("e.g. Diabetic, previous cataract surgery") },
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Save to Contacts checkbox
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = saveToContactsChecked,
                    onCheckedChange = { saveToContactsChecked = it }
                )
                Text(
                    text = "Save customer to phone contacts",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Save Button
            Button(
                onClick = {
                    viewModel.saveCustomer { savedId ->
                        if (saveToContactsChecked) {
                            if (ContactsHelper.isContactPermissionGranted(context)) {
                                ContactsHelper.saveCustomerToContacts(
                                    context = context,
                                    name = name.trim(),
                                    phone = phone.trim()
                                )
                            } else {
                                permissionLauncher.launch(
                                    arrayOf(
                                        android.Manifest.permission.READ_CONTACTS,
                                        android.Manifest.permission.WRITE_CONTACTS
                                    )
                                )
                            }
                        }
                        Toast.makeText(context, "Customer saved successfully", Toast.LENGTH_SHORT).show()
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
                        text = if (customerId != null) "Update Customer" else "Save Customer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
