package com.nanokernel.expensetracker.ui.borrow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nanokernel.expensetracker.data.local.BorrowEntity
import com.nanokernel.expensetracker.data.repository.BorrowRepository
import com.nanokernel.expensetracker.data.repository.SettingsRepository
import com.nanokernel.expensetracker.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth

data class BorrowListUiState(
    val month: YearMonth = YearMonth.now(),
    val entries: List<BorrowEntity> = emptyList(),
    val monthTotal: Double = 0.0,
    val overallTotal: Double = 0.0,
    val currencySymbol: String = SettingsRepository.DEFAULT_CURRENCY_SYMBOL
)

class BorrowListViewModel(
    private val repository: BorrowRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    private val monthFlow = MutableStateFlow(YearMonth.now())

    val uiState: StateFlow<BorrowListUiState> = combine(
        repository.allBorrows,
        monthFlow,
        settingsRepository.currencySymbolFlow
    ) { borrows, month, symbol ->
        val (monthStart, monthEnd) = DateUtils.monthRange(month)
        val entries = borrows.filter { it.timestampMillis in monthStart..monthEnd }
        BorrowListUiState(
            month = month,
            entries = entries,
            monthTotal = entries.sumOf { it.amount },
            overallTotal = borrows.sumOf { it.amount },
            currencySymbol = symbol
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BorrowListUiState()
    )

    fun previousMonth() {
        monthFlow.value = monthFlow.value.minusMonths(1)
    }

    fun nextMonth() {
        monthFlow.value = monthFlow.value.plusMonths(1)
    }

    fun deleteBorrow(borrow: BorrowEntity) {
        viewModelScope.launch { repository.deleteBorrow(borrow) }
    }
}
