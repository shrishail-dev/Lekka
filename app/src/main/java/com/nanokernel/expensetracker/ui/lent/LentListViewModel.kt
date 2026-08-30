package com.nanokernel.expensetracker.ui.lent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nanokernel.expensetracker.data.local.LentEntity
import com.nanokernel.expensetracker.data.repository.LentRepository
import com.nanokernel.expensetracker.data.repository.SettingsRepository
import com.nanokernel.expensetracker.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth

data class LentListUiState(
    val month: YearMonth = YearMonth.now(),
    val entries: List<LentEntity> = emptyList(),
    val monthTotal: Double = 0.0,
    val overallTotal: Double = 0.0,
    val currencySymbol: String = SettingsRepository.DEFAULT_CURRENCY_SYMBOL
)

class LentListViewModel(
    private val repository: LentRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    private val monthFlow = MutableStateFlow(YearMonth.now())

    val uiState: StateFlow<LentListUiState> = combine(
        repository.allLent,
        monthFlow,
        settingsRepository.currencySymbolFlow
    ) { lent, month, symbol ->
        val (monthStart, monthEnd) = DateUtils.monthRange(month)
        val entries = lent.filter { it.timestampMillis in monthStart..monthEnd }
        LentListUiState(
            month = month,
            entries = entries,
            monthTotal = entries.sumOf { it.amount },
            overallTotal = lent.sumOf { it.amount },
            currencySymbol = symbol
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LentListUiState()
    )

    fun previousMonth() {
        monthFlow.value = monthFlow.value.minusMonths(1)
    }

    fun nextMonth() {
        monthFlow.value = monthFlow.value.plusMonths(1)
    }

    fun deleteLent(lent: LentEntity) {
        viewModelScope.launch { repository.deleteLent(lent) }
    }
}
