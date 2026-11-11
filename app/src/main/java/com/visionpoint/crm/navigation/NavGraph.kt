package com.visionpoint.crm.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.visionpoint.crm.ui.screens.cashbook.AddCashbookEntryScreen
import com.visionpoint.crm.ui.screens.cashbook.CashbookScreen
import com.visionpoint.crm.ui.screens.cashbook.CashbookViewModel
import com.visionpoint.crm.ui.screens.customers.AddEditCustomerScreen
import com.visionpoint.crm.ui.screens.customers.CustomerDetailScreen
import com.visionpoint.crm.ui.screens.customers.CustomerListScreen
import com.visionpoint.crm.ui.screens.customers.CustomerViewModel
import com.visionpoint.crm.ui.screens.dashboard.DashboardScreen
import com.visionpoint.crm.ui.screens.dashboard.DashboardViewModel
import com.visionpoint.crm.ui.screens.prescriptions.AddPrescriptionScreen
import com.visionpoint.crm.ui.screens.prescriptions.PrescriptionViewModel
import com.visionpoint.crm.ui.screens.sales.CreateEditSaleScreen
import com.visionpoint.crm.ui.screens.sales.SaleDetailScreen
import com.visionpoint.crm.ui.screens.sales.SalesListScreen
import com.visionpoint.crm.ui.screens.sales.SaleViewModel
import com.visionpoint.crm.ui.screens.settings.SettingsScreen
import com.visionpoint.crm.ui.screens.settings.SettingsViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        // Dashboard (Phase 4)
        composable(Screen.Dashboard.route) {
            val dashboardViewModel: DashboardViewModel = hiltViewModel()
            DashboardScreen(
                viewModel = dashboardViewModel,
                onNavigateToSaleDetail = { saleId ->
                    navController.navigate(Screen.SaleDetail.createRoute(saleId))
                },
                onNavigateToCustomerDetail = { customerId ->
                    navController.navigate(Screen.CustomerDetail.createRoute(customerId))
                },
                onNavigateToSalesList = {
                    navController.navigate(Screen.SalesList.route)
                },
                onNavigateToCreateSale = {
                    navController.navigate(Screen.CreateEditSale.createRoute())
                },
                onNavigateToAddCustomer = {
                    navController.navigate(Screen.AddEditCustomer.createRoute())
                },
                onNavigateToCashbook = {
                    navController.navigate(Screen.Cashbook.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        // Customer List
        composable(Screen.CustomerList.route) {
            val customerViewModel: CustomerViewModel = hiltViewModel()
            CustomerListScreen(
                viewModel = customerViewModel,
                onNavigateToDetail = { customerId ->
                    navController.navigate(Screen.CustomerDetail.createRoute(customerId))
                },
                onNavigateToAdd = {
                    navController.navigate(Screen.AddEditCustomer.createRoute())
                }
            )
        }

        // Add / Edit Customer
        composable(
            route = Screen.AddEditCustomer.route,
            arguments = listOf(
                navArgument("customerId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val customerViewModel: CustomerViewModel = hiltViewModel()
            val customerId = backStackEntry.arguments?.getString("customerId")?.toIntOrNull()
            AddEditCustomerScreen(
                viewModel = customerViewModel,
                customerId = customerId,
                onNavigateBack = { navController.popBackStack() },
                onSaved = { savedId ->
                    navController.popBackStack()
                    if (customerId == null) {
                        navController.navigate(Screen.CustomerDetail.createRoute(savedId))
                    }
                }
            )
        }

        // Customer Detail
        composable(
            route = Screen.CustomerDetail.route,
            arguments = listOf(
                navArgument("customerId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val customerViewModel: CustomerViewModel = hiltViewModel()
            val customerId = backStackEntry.arguments?.getInt("customerId") ?: 0
            CustomerDetailScreen(
                customerId = customerId,
                viewModel = customerViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    navController.navigate(Screen.AddEditCustomer.createRoute(id))
                },
                onNavigateToAddPrescription = { id ->
                    navController.navigate(Screen.AddPrescription.createRoute(id))
                },
                onNavigateToEditPrescription = { custId, rxId ->
                    navController.navigate(Screen.AddPrescription.createRoute(customerId = custId, prescriptionId = rxId))
                },
                onNavigateToCreateSale = { id ->
                    navController.navigate(Screen.CreateEditSale.createRoute(customerId = id))
                },
                onNavigateToSaleDetail = { saleId ->
                    navController.navigate(Screen.SaleDetail.createRoute(saleId))
                }
            )
        }

        // Add / Edit Prescription
        composable(
            route = Screen.AddPrescription.route,
            arguments = listOf(
                navArgument("customerId") { type = NavType.IntType },
                navArgument("prescriptionId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val prescriptionViewModel: PrescriptionViewModel = hiltViewModel()
            val customerId = backStackEntry.arguments?.getInt("customerId") ?: 0
            val prescriptionId = backStackEntry.arguments?.getString("prescriptionId")?.toIntOrNull()
            AddPrescriptionScreen(
                customerId = customerId,
                prescriptionId = prescriptionId,
                viewModel = prescriptionViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Sales List
        composable(Screen.SalesList.route) {
            val saleViewModel: SaleViewModel = hiltViewModel()
            SalesListScreen(
                viewModel = saleViewModel,
                onNavigateToDetail = { saleId ->
                    navController.navigate(Screen.SaleDetail.createRoute(saleId))
                },
                onNavigateToCreate = {
                    navController.navigate(Screen.CreateEditSale.createRoute())
                }
            )
        }

        // Create / Edit Sale
        composable(
            route = Screen.CreateEditSale.route,
            arguments = listOf(
                navArgument("saleId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("customerId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val saleViewModel: SaleViewModel = hiltViewModel()
            val saleId = backStackEntry.arguments?.getString("saleId")?.toIntOrNull()
            val customerId = backStackEntry.arguments?.getString("customerId")?.toIntOrNull()
            CreateEditSaleScreen(
                viewModel = saleViewModel,
                saleId = saleId,
                preselectedCustomerId = customerId,
                onNavigateBack = { navController.popBackStack() },
                onSaved = { savedSaleId ->
                    navController.popBackStack()
                    navController.navigate(Screen.SaleDetail.createRoute(savedSaleId))
                }
            )
        }

        // Sale Detail
        composable(
            route = Screen.SaleDetail.route,
            arguments = listOf(
                navArgument("saleId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val saleViewModel: SaleViewModel = hiltViewModel()
            val saleId = backStackEntry.arguments?.getInt("saleId") ?: 0
            SaleDetailScreen(
                saleId = saleId,
                viewModel = saleViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    navController.navigate(Screen.CreateEditSale.createRoute(saleId = id))
                },
                onNavigateToCustomer = { custId ->
                    navController.navigate(Screen.CustomerDetail.createRoute(custId))
                }
            )
        }

        // Cashbook
        composable(Screen.Cashbook.route) {
            val cashbookViewModel: CashbookViewModel = hiltViewModel()
            CashbookScreen(
                viewModel = cashbookViewModel,
                onNavigateToAddEntry = {
                    navController.navigate(Screen.AddCashbookEntry.route)
                }
            )
        }

        // Add Cashbook Entry
        composable(Screen.AddCashbookEntry.route) {
            val cashbookViewModel: CashbookViewModel = hiltViewModel()
            AddCashbookEntryScreen(
                viewModel = cashbookViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Settings
        composable(Screen.Settings.route) {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            SettingsScreen(viewModel = settingsViewModel)
        }
    }
}
