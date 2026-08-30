package com.nanokernel.expensetracker.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object AddExpense : Screen("add_expense")
    data object ExpenseDetail : Screen("expense_detail")
    data object Calendar : Screen("calendar")
    data object Report : Screen("report")
    data object Insights : Screen("insights")
    data object BorrowList : Screen("borrow_list")
    data object AddBorrow : Screen("add_borrow")
    data object EventList : Screen("event_list")
    data object AddEvent : Screen("add_event")
    data object EventDetail : Screen("event_detail")
    data object AddEventExpense : Screen("add_event_expense")
    data object LentList : Screen("lent_list")
    data object AddLent : Screen("add_lent")
}
