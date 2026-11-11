package com.visionpoint.crm.ui.screens.customers

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.visionpoint.crm.data.model.Prescription
import com.visionpoint.crm.data.model.SaleWithDetails
import com.visionpoint.crm.ui.components.ConfirmDialog
import com.visionpoint.crm.ui.components.CurrencyText
import com.visionpoint.crm.ui.components.EmptyStateView
import com.visionpoint.crm.ui.components.StatusChip
import com.visionpoint.crm.ui.theme.DueAmountColor
import com.visionpoint.crm.util.ContactsHelper
import com.visionpoint.crm.util.EyePowerUtils
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    customerId: Int,
    viewModel: CustomerViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Int) -> Unit,
    onNavigateToAddPrescription: (Int) -> Unit,
    onNavigateToEditPrescription: (Int, Int) -> Unit,
    onNavigateToCreateSale: (Int) -> Unit,
    onNavigateToSaleDetail: (Int) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(customerId) {
        viewModel.loadCustomerDetail(customerId)
    }

    val customerWithPrescriptions by viewModel.selectedCustomer.collectAsState()
    val sales by viewModel.selectedCustomerSales.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var prescriptionToDelete by remember { mutableStateOf<Prescription?>(null) }

    val customer = customerWithPrescriptions?.customer
    val prescriptions = customerWithPrescriptions?.prescriptions?.sortedWith(
        compareByDescending<Prescription> { it.date }.thenByDescending { it.id }
    ) ?: emptyList()

    var isContactAlreadySaved by remember(customer?.phone) {
        mutableStateOf(
            if (customer != null) ContactsHelper.isContactSaved(context, customer.phone) else false
        )
    }

    val contactPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val readGranted = permissions[android.Manifest.permission.READ_CONTACTS] == true
        val writeGranted = permissions[android.Manifest.permission.WRITE_CONTACTS] == true
        if (readGranted && writeGranted && customer != null) {
            val success = ContactsHelper.saveCustomerToContacts(context, customer.name, customer.phone)
            if (success) isContactAlreadySaved = true
        } else {
            Toast.makeText(context, "Contacts permission was denied", Toast.LENGTH_SHORT).show()
        }
    }

    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = customer?.name ?: "Customer Profile",
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
                    if (customer != null) {
                        IconButton(onClick = { onNavigateToEdit(customer.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Customer")
                        }
                        IconButton(onClick = { showDeleteConfirmDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete Customer",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        if (customer == null) {
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Header Customer Card
                CustomerHeaderCard(
                    customer = customer,
                    isContactAlreadySaved = isContactAlreadySaved,
                    onCall = { ContactsHelper.launchDialer(context, customer.phone) },
                    onWhatsApp = { ContactsHelper.launchWhatsApp(context, customer.phone) },
                    onSaveContact = {
                        if (ContactsHelper.isContactPermissionGranted(context)) {
                            val success = ContactsHelper.saveCustomerToContacts(context, customer.name, customer.phone)
                            if (success) isContactAlreadySaved = true
                        } else {
                            contactPermissionLauncher.launch(
                                arrayOf(
                                    android.Manifest.permission.READ_CONTACTS,
                                    android.Manifest.permission.WRITE_CONTACTS
                                )
                            )
                        }
                    }
                )

                // Tabs: Prescriptions (X) | Sales History (Y)
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = {
                            Text(
                                "Prescriptions (${prescriptions.size})",
                                fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = {
                            Text(
                                "Sales (${sales.size})",
                                fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }

                when (selectedTabIndex) {
                    0 -> {
                        // Prescriptions List Tab
                        PrescriptionsTabContent(
                            prescriptions = prescriptions,
                            dateFormatter = dateFormatter,
                            onAddPrescription = { onNavigateToAddPrescription(customer.id) },
                            onEditPrescription = { rx -> onNavigateToEditPrescription(customer.id, rx.id) },
                            onDeletePrescription = { prescriptionToDelete = it },
                            onSharePrescription = { rx ->
                                coroutineScope.launch {
                                    val shop = viewModel.getShopDetails()
                                    val eyeFields = viewModel.getEyeFields()
                                    val pdf = com.visionpoint.crm.util.PdfGenerator.generatePrescriptionPdf(
                                        context = context,
                                        prescription = rx,
                                        customer = customer,
                                        shopDetails = shop,
                                        eyeFields = eyeFields
                                    )
                                    com.visionpoint.crm.util.ShareUtils.sharePdf(context, pdf, "Prescription #${rx.id}")
                                }
                            },
                            onWhatsAppPrescription = { rx ->
                                coroutineScope.launch {
                                    val shop = viewModel.getShopDetails()
                                    val eyeFields = viewModel.getEyeFields()
                                    val pdf = com.visionpoint.crm.util.PdfGenerator.generatePrescriptionPdf(
                                        context = context,
                                        prescription = rx,
                                        customer = customer,
                                        shopDetails = shop,
                                        eyeFields = eyeFields
                                    )
                                    val msg = "Hello ${customer.name}, please find attached your Eye Prescription from ${shop.name}."
                                    com.visionpoint.crm.util.ShareUtils.shareViaWhatsApp(
                                        context = context,
                                        phone = customer.phone,
                                        message = msg,
                                        pdfFile = pdf
                                    )
                                }
                            },
                            onViewPrescription = { rx ->
                                coroutineScope.launch {
                                    val shop = viewModel.getShopDetails()
                                    val eyeFields = viewModel.getEyeFields()
                                    val pdf = com.visionpoint.crm.util.PdfGenerator.generatePrescriptionPdf(
                                        context = context,
                                        prescription = rx,
                                        customer = customer,
                                        shopDetails = shop,
                                        eyeFields = eyeFields
                                    )
                                    com.visionpoint.crm.util.ShareUtils.viewPdf(context, pdf)
                                }
                            }
                        )
                    }
                    1 -> {
                        // Sales List Tab
                        SalesTabContent(
                            sales = sales,
                            dateFormatter = dateFormatter,
                            onCreateSale = { onNavigateToCreateSale(customer.id) },
                            onSaleClick = onNavigateToSaleDetail
                        )
                    }
                }
            }
        }

        // Delete Customer Confirmation Dialog
        if (showDeleteConfirmDialog && customer != null) {
            ConfirmDialog(
                title = "Delete Customer",
                message = "Are you sure you want to delete ${customer.name}? All associated prescriptions and records will be deleted.",
                confirmText = "Delete",
                onConfirm = {
                    viewModel.deleteCustomer(customer) {
                        Toast.makeText(context, "Customer deleted", Toast.LENGTH_SHORT).show()
                        onNavigateBack()
                    }
                },
                onDismiss = { showDeleteConfirmDialog = false }
            )
        }

        // Delete Prescription Confirmation Dialog
        if (prescriptionToDelete != null) {
            ConfirmDialog(
                title = "Delete Prescription",
                message = "Are you sure you want to delete this prescription dated ${prescriptionToDelete?.date?.format(dateFormatter)}?",
                confirmText = "Delete",
                onConfirm = {
                    prescriptionToDelete?.let { viewModel.deletePrescription(it) }
                    prescriptionToDelete = null
                },
                onDismiss = { prescriptionToDelete = null }
            )
        }
    }
}

@Composable
fun CustomerHeaderCard(
    customer: Customer,
    isContactAlreadySaved: Boolean,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit,
    onSaveContact: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val initials = customer.name.trim().take(2).uppercase()
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials.ifBlank { "C" },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = customer.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = customer.phone,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val metaList = mutableListOf<String>()
                    customer.gender?.let { metaList.add(it) }
                    customer.age?.let { metaList.add("$it years") }
                    customer.dob?.let { metaList.add("DOB: ${it.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}") }

                    if (metaList.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = metaList.joinToString(" • "),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (!customer.address.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Address: ${customer.address}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!customer.notes.isNullOrBlank()) {
                if (customer.address.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Notes: ${customer.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onCall,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Call",
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Button(
                    onClick = onWhatsApp,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "WhatsApp",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                if (!isContactAlreadySaved) {
                    OutlinedButton(
                        onClick = onSaveContact,
                        modifier = Modifier.size(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContactPhone,
                            contentDescription = "Save Contact",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PrescriptionsTabContent(
    prescriptions: List<Prescription>,
    dateFormatter: DateTimeFormatter,
    onAddPrescription: () -> Unit,
    onEditPrescription: (Prescription) -> Unit,
    onDeletePrescription: (Prescription) -> Unit,
    onSharePrescription: (Prescription) -> Unit,
    onWhatsAppPrescription: (Prescription) -> Unit,
    onViewPrescription: (Prescription) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Prescription Records",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Button(
                onClick = onAddPrescription,
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add")
            }
        }

        if (prescriptions.isEmpty()) {
            EmptyStateView(
                title = "No prescriptions recorded",
                message = "Record eye test power details for specs or contact lenses.",
                icon = Icons.Default.Visibility,
                action = {
                    Button(onClick = onAddPrescription) {
                        Text("Add Prescription")
                    }
                }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(prescriptions, key = { it.id }) { prescription ->
                    PrescriptionCard(
                        prescription = prescription,
                        dateFormatter = dateFormatter,
                        onEdit = { onEditPrescription(prescription) },
                        onDelete = { onDeletePrescription(prescription) },
                        onShare = { onSharePrescription(prescription) },
                        onWhatsApp = { onWhatsAppPrescription(prescription) },
                        onView = { onViewPrescription(prescription) }
                    )
                }
            }
        }
    }
}

@Composable
fun PrescriptionCard(
    prescription: Prescription,
    dateFormatter: DateTimeFormatter,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onWhatsApp: () -> Unit,
    onView: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rightEyeMap = remember(prescription.rightEye) {
        EyePowerUtils.decodeEyeData(prescription.rightEye)
    }
    val leftEyeMap = remember(prescription.leftEye) {
        EyePowerUtils.decodeEyeData(prescription.leftEye)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${prescription.type ?: "Specs"} • ${prescription.date.format(dateFormatter)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Doctor: ${prescription.doctorName?.ifBlank { null } ?: "Arun Bhalla"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (prescription.isPreviousLens) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Rx as per patient's previous lenses",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Prescription",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Prescription",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Eye Power Table
            EyePowerTableView(rightEye = rightEyeMap, leftEye = leftEyeMap)

            // Additional Measurements (PD, PSM, FH)
            val additionalFields = mutableListOf<String>()
            if (!prescription.pd.isNullOrBlank()) additionalFields.add("PD: ${prescription.pd} mm")
            if (!prescription.psm.isNullOrBlank()) additionalFields.add("PSM: ${prescription.psm}")
            if (!prescription.fh.isNullOrBlank()) additionalFields.add("FH: ${prescription.fh}")

            if (additionalFields.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = additionalFields.joinToString("  |  "),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!prescription.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Notes: ${prescription.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons Row (WhatsApp, Share, View)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onWhatsApp,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .height(36.dp)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("WhatsApp Rx", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = onShare,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share", style = MaterialTheme.typography.labelSmall)
                }

                OutlinedButton(
                    onClick = onView,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    modifier = Modifier
                        .weight(0.9f)
                        .height(36.dp)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("View", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun EyePowerTableView(
    rightEye: Map<String, String>,
    leftEye: Map<String, String>,
    modifier: Modifier = Modifier
) {
    val allKeys = (rightEye.keys + leftEye.keys).distinct().ifEmpty {
        listOf("SPH", "CYL", "Axis", "Add", "V/A")
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        // Table Header
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Eye",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(60.dp)
            )
            allKeys.forEach { key ->
                Text(
                    text = key,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        // Right Eye (RE/OD)
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "RE(OD)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.width(60.dp)
            )
            allKeys.forEach { key ->
                Text(
                    text = rightEye[key]?.ifBlank { "-" } ?: "-",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Left Eye (LE/OS)
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "LE(OS)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.width(60.dp)
            )
            allKeys.forEach { key ->
                Text(
                    text = leftEye[key]?.ifBlank { "-" } ?: "-",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun SalesTabContent(
    sales: List<SaleWithDetails>,
    dateFormatter: DateTimeFormatter,
    onCreateSale: () -> Unit,
    onSaleClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Purchase History",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Button(
                onClick = onCreateSale,
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Sale")
            }
        }

        if (sales.isEmpty()) {
            EmptyStateView(
                title = "No sales history",
                message = "No purchases or bills recorded for this customer yet.",
                icon = Icons.Default.ReceiptLong,
                action = {
                    Button(onClick = onCreateSale) {
                        Text("Create Sale")
                    }
                }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(sales, key = { it.sale.id }) { saleWithDetails ->
                    val sale = saleWithDetails.sale
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSaleClick(sale.id) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Bill #${sale.id}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    StatusChip(status = sale.status)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Date: ${sale.saleDate.format(dateFormatter)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${saleWithDetails.items.size} items",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                CurrencyText(
                                    amount = sale.netAmount,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
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
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}
