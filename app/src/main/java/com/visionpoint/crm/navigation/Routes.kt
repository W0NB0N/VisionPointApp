package com.visionpoint.crm.navigation

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object CustomerList : Screen("customers")
    data object AddEditCustomer : Screen("customers/form?customerId={customerId}") {
        fun createRoute(customerId: Int? = null): String =
            if (customerId != null) "customers/form?customerId=$customerId" else "customers/form"
    }
    data object CustomerDetail : Screen("customers/{customerId}") {
        fun createRoute(customerId: Int): String = "customers/$customerId"
    }
    data object AddPrescription : Screen("customers/{customerId}/prescriptions/form?prescriptionId={prescriptionId}") {
        fun createRoute(customerId: Int, prescriptionId: Int? = null): String = buildString {
            append("customers/$customerId/prescriptions/form?")
            if (prescriptionId != null) append("prescriptionId=$prescriptionId&")
        }.removeSuffix("&").removeSuffix("?")
    }
    data object SalesList : Screen("sales")
    data object CreateEditSale : Screen("sales/form?saleId={saleId}&customerId={customerId}") {
        fun createRoute(saleId: Int? = null, customerId: Int? = null): String = buildString {
            append("sales/form?")
            if (saleId != null) append("saleId=$saleId&")
            if (customerId != null) append("customerId=$customerId&")
        }.removeSuffix("&").removeSuffix("?")
    }
    data object SaleDetail : Screen("sales/{saleId}") {
        fun createRoute(saleId: Int): String = "sales/$saleId"
    }
    data object Cashbook : Screen("cashbook")
    data object AddCashbookEntry : Screen("cashbook/add")
    data object Settings : Screen("settings")
}
