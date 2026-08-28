package com.nanokernel.expensetracker.ui.expensedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nanokernel.expensetracker.data.local.ExpenseEntity
import com.nanokernel.expensetracker.data.model.CategoryInfo
import com.nanokernel.expensetracker.data.model.DefaultCategories
import com.nanokernel.expensetracker.data.repository.ExpenseRepository
import com.nanokernel.expensetracker.data.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ExpenseDetailUiState(
    val expense: ExpenseEntity? = null,
    val categories: List<CategoryInfo> = DefaultCategories.list,
    val currencySymbol: String = SettingsRepository.DEFAULT_CURRENCY_SYMBOL
)

class ExpenseDetailViewModel(
    repository: ExpenseRepository,
    settingsRepository: SettingsRepository,
    expenseId: Long
) : ViewModel() {

    val uiState: StateFlow<ExpenseDetailUiState> = combine(
        repository.allExpenses,
        settingsRepository.allCategoriesFlow,
        settingsRepository.currencySymbolFlow
    ) { expenses, categories, symbol ->
        ExpenseDetailUiState(
            expense = expenses.find { it.id == expenseId },
            categories = categories,
            currencySymbol = symbol
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ExpenseDetailUiState())
}
