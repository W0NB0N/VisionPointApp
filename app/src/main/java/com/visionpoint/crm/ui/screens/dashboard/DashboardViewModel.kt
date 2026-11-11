package com.visionpoint.crm.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.visionpoint.crm.data.model.Customer
import com.visionpoint.crm.data.model.SaleWithCustomer
import com.visionpoint.crm.data.repository.CustomerRepository
import com.visionpoint.crm.data.repository.SaleRepository
import com.visionpoint.crm.data.repository.SettingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class RecallInfo(
    val saleId: Int,
    val customerId: Int,
    val customerName: String,
    val customerPhone: String,
    val recallDate: LocalDate,
    val netAmount: Double,
    val status: String
)

data class DashboardState(
    val todaysSalesAmount: Double = 0.0,
    val todaysSalesCount: Int = 0,
    val thisMonthSalesAmount: Double = 0.0,
    val thisMonthSalesCount: Int = 0,
    val lastMonthSalesAmount: Double = 0.0,
    val lastMonthSalesCount: Int = 0,
    val totalDue: Double = 0.0,
    val totalSalesCount: Int = 0,
    val recentSales: List<SaleWithCustomer> = emptyList(),
    val recallsToday: List<RecallInfo> = emptyList(),
    val birthdaysToday: List<Customer> = emptyList(),
    val shopName: String = "Vision Point",
    val recallMsgTemplate: String = SettingRepository.DEFAULT_MSG_RECALL,
    val birthdayMsgTemplate: String = SettingRepository.DEFAULT_MSG_BIRTHDAY,
    val dueMsgTemplate: String = SettingRepository.DEFAULT_MSG_DUE,
    val currentDate: LocalDate = LocalDate.now(ZoneId.of("Asia/Kolkata")),
    val isLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val saleRepository: SaleRepository,
    private val customerRepository: CustomerRepository,
    private val settingRepository: SettingRepository
) : ViewModel() {

    private val istZone = ZoneId.of("Asia/Kolkata")

    val state: StateFlow<DashboardState> = combine(
        saleRepository.getAllSales(),
        customerRepository.getAllCustomers(),
        settingRepository.getAllSettings()
    ) { sales, customers, settings ->
        val today = LocalDate.now(istZone)

        // Settings lookup
        val settingsMap = settings.associate { it.key to it.value }
        val shopName = settingsMap[SettingRepository.KEY_SHOP_NAME] ?: "Vision Point"
        val recallTemplate = settingsMap[SettingRepository.KEY_MSG_RECALL] ?: SettingRepository.DEFAULT_MSG_RECALL
        val birthdayTemplate = settingsMap[SettingRepository.KEY_MSG_BIRTHDAY] ?: SettingRepository.DEFAULT_MSG_BIRTHDAY
        val dueTemplate = settingsMap[SettingRepository.KEY_MSG_DUE] ?: SettingRepository.DEFAULT_MSG_DUE

        // 1. Today's metrics
        val todaysSales = sales.filter { it.sale.saleDate == today }
        val todaysSalesAmount = todaysSales.sumOf { it.sale.netAmount }
        val todaysSalesCount = todaysSales.size

        // 2. This Month metrics (1st of this month to end of this month)
        val startOfThisMonth = today.withDayOfMonth(1)
        val endOfThisMonth = today.withDayOfMonth(today.lengthOfMonth())
        val thisMonthSales = sales.filter {
            !it.sale.saleDate.isBefore(startOfThisMonth) && !it.sale.saleDate.isAfter(endOfThisMonth)
        }
        val thisMonthSalesAmount = thisMonthSales.sumOf { it.sale.netAmount }
        val thisMonthSalesCount = thisMonthSales.size

        // 3. Last Month metrics (1st of last month to end of last month)
        val lastMonthDate = today.minusMonths(1)
        val startOfLastMonth = lastMonthDate.withDayOfMonth(1)
        val endOfLastMonth = lastMonthDate.withDayOfMonth(lastMonthDate.lengthOfMonth())
        val lastMonthSales = sales.filter {
            !it.sale.saleDate.isBefore(startOfLastMonth) && !it.sale.saleDate.isAfter(endOfLastMonth)
        }
        val lastMonthSalesAmount = lastMonthSales.sumOf { it.sale.netAmount }
        val lastMonthSalesCount = lastMonthSales.size

        // 4. Total Outstanding Due
        val totalDue = sales.sumOf { it.sale.dueAmount }

        // 5. Recent 5 Sales
        val recentSales = sales.take(5)

        // 6. Recalls Today
        val recallsToday = sales.filter { it.sale.recallDate == today }.map { saleWithCust ->
            RecallInfo(
                saleId = saleWithCust.sale.id,
                customerId = saleWithCust.customer.id,
                customerName = saleWithCust.customer.name,
                customerPhone = saleWithCust.customer.phone,
                recallDate = saleWithCust.sale.recallDate ?: today,
                netAmount = saleWithCust.sale.netAmount,
                status = saleWithCust.sale.status
            )
        }

        // 7. Birthdays Today
        val birthdaysToday = customers.filter { cust ->
            cust.dob?.let { dob ->
                dob.monthValue == today.monthValue && dob.dayOfMonth == today.dayOfMonth
            } ?: false
        }

        DashboardState(
            todaysSalesAmount = todaysSalesAmount,
            todaysSalesCount = todaysSalesCount,
            thisMonthSalesAmount = thisMonthSalesAmount,
            thisMonthSalesCount = thisMonthSalesCount,
            lastMonthSalesAmount = lastMonthSalesAmount,
            lastMonthSalesCount = lastMonthSalesCount,
            totalDue = totalDue,
            totalSalesCount = sales.size,
            recentSales = recentSales,
            recallsToday = recallsToday,
            birthdaysToday = birthdaysToday,
            shopName = shopName,
            recallMsgTemplate = recallTemplate,
            birthdayMsgTemplate = birthdayTemplate,
            dueMsgTemplate = dueTemplate,
            currentDate = today,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardState()
    )

    fun formatRecallMessage(customerName: String, shopName: String): String {
        return state.value.recallMsgTemplate
            .replace("{name}", customerName)
            .replace("{shop}", shopName)
    }

    fun formatBirthdayMessage(customerName: String, shopName: String): String {
        return state.value.birthdayMsgTemplate
            .replace("{name}", customerName)
            .replace("{shop}", shopName)
    }
}
