package com.nanokernel.expensetracker.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nanokernel.expensetracker.ui.addexpense.AddExpenseScreen
import com.nanokernel.expensetracker.ui.borrow.AddBorrowScreen
import com.nanokernel.expensetracker.ui.borrow.BorrowListScreen
import com.nanokernel.expensetracker.ui.calendar.CalendarScreen
import com.nanokernel.expensetracker.ui.event.AddEventExpenseScreen
import com.nanokernel.expensetracker.ui.event.AddEventScreen
import com.nanokernel.expensetracker.ui.event.EventDetailScreen
import com.nanokernel.expensetracker.ui.event.EventListScreen
import com.nanokernel.expensetracker.ui.expensedetail.ExpenseDetailScreen
import com.nanokernel.expensetracker.ui.home.HomeScreen
import com.nanokernel.expensetracker.ui.insights.InsightsScreen
import com.nanokernel.expensetracker.ui.report.MonthlyReportScreen

private data class BottomTab(val screen: Screen, val label: String, val icon: ImageVector)

// "Add" sits in the bottom nav like every other destination rather than as a floating overlay
// button — it's then reachable identically from any screen, not just Home.
private val bottomTabs = listOf(
    BottomTab(Screen.Home, "Home", Icons.Filled.Home),
    BottomTab(Screen.Calendar, "Calendar", Icons.Filled.CalendarMonth),
    BottomTab(Screen.AddExpense, "Add", Icons.Filled.Add),
    BottomTab(Screen.Report, "Report", Icons.Filled.BarChart),
    BottomTab(Screen.Insights, "Insights", Icons.Filled.Lightbulb)
)

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    var bottomBarVisible by remember { mutableStateOf(true) }
    // Scrolling a list down (finger moving up, negative y) hides the bar; scrolling up reveals
    // it again — the bar always reappears on a fresh screen since currentRoute resets it below.
    val bottomBarScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < -4f) bottomBarVisible = false
                else if (available.y > 4f) bottomBarVisible = true
                return Offset.Zero
            }
        }
    }
    LaunchedEffect(currentRoute) { bottomBarVisible = true }

    // Only the 5 top-level tabs show the bottom nav. Sub-screens reached by drilling in (Borrow
    // list, Event list/detail, Expense detail, ...) hide it — otherwise the nav bar's own "+"
    // (always "add a regular monthly expense") sits alongside a screen-specific "+" that means
    // something else (e.g. "add an expense to this event"), which is easy to tap by mistake.
    val isTopLevelRoute = bottomTabs.any { tab -> currentRoute?.startsWith(tab.screen.route) == true }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = bottomBarVisible && isTopLevelRoute,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
            NavigationBar {
                bottomTabs.forEach { tab ->
                    val isAdd = tab.screen == Screen.AddExpense
                    NavigationBarItem(
                        selected = currentRoute?.startsWith(tab.screen.route) == true,
                        onClick = {
                            if (tab.screen == Screen.Home) {
                                // Home may be reached from screens pushed on top of it (e.g. Borrow
                                // list/detail, Expense detail) that aren't part of the back stack's
                                // saved-state entries popUpTo below relies on — popping straight to
                                // the existing Home entry works regardless of how deep we are.
                                navController.popBackStack(Screen.Home.route, inclusive = false)
                            } else {
                                navController.navigate(tab.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            if (isAdd) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.secondary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        tab.icon,
                                        contentDescription = tab.label,
                                        tint = MaterialTheme.colorScheme.onSecondary
                                    )
                                }
                            } else {
                                Icon(tab.icon, contentDescription = tab.label)
                            }
                        },
                        label = { Text(tab.label) },
                        colors = if (isAdd) {
                            NavigationBarItemDefaults.colors(indicatorColor = MaterialTheme.colorScheme.surface)
                        } else {
                            NavigationBarItemDefaults.colors()
                        }
                    )
                }
            }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier
                .padding(padding)
                .nestedScroll(bottomBarScrollConnection)
        ) {
            val onExpenseClick: (Long) -> Unit = { id ->
                navController.navigate("${Screen.ExpenseDetail.route}/$id")
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    onExpenseClick = onExpenseClick,
                    onBorrowedClick = { navController.navigate(Screen.BorrowList.route) },
                    onEventsClick = { navController.navigate(Screen.EventList.route) }
                )
            }
            composable(
                route = "${Screen.AddExpense.route}?date={date}&id={id}",
                arguments = listOf(
                    navArgument("date") { type = NavType.StringType; nullable = true },
                    navArgument("id") { type = NavType.StringType; nullable = true }
                )
            ) { backStackEntry ->
                val dateArg = backStackEntry.arguments?.getString("date")
                val idArg = backStackEntry.arguments?.getString("id")
                AddExpenseScreen(
                    onDone = { navController.popBackStack() },
                    initialDateMillis = dateArg?.toLongOrNull(),
                    editingExpenseId = idArg?.toLongOrNull()
                )
            }
            composable(
                route = "${Screen.ExpenseDetail.route}/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("id") ?: return@composable
                ExpenseDetailScreen(
                    expenseId = id,
                    onBack = { navController.popBackStack() },
                    onEdit = { expenseId -> navController.navigate("${Screen.AddExpense.route}?id=$expenseId") }
                )
            }
            composable(Screen.Calendar.route) {
                CalendarScreen(onExpenseClick = onExpenseClick)
            }
            composable(Screen.Report.route) {
                MonthlyReportScreen(onExpenseClick = onExpenseClick)
            }
            composable(Screen.Insights.route) {
                InsightsScreen()
            }
            composable(Screen.BorrowList.route) {
                BorrowListScreen(
                    onBack = { navController.popBackStack() },
                    onAddBorrow = { navController.navigate(Screen.AddBorrow.route) },
                    onEditBorrow = { id -> navController.navigate("${Screen.AddBorrow.route}?id=$id") }
                )
            }
            composable(
                route = "${Screen.AddBorrow.route}?id={id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType; nullable = true })
            ) { backStackEntry ->
                val idArg = backStackEntry.arguments?.getString("id")
                AddBorrowScreen(
                    onDone = { navController.popBackStack() },
                    editingBorrowId = idArg?.toLongOrNull()
                )
            }
            composable(Screen.EventList.route) {
                EventListScreen(
                    onBack = { navController.popBackStack() },
                    onAddEvent = { navController.navigate(Screen.AddEvent.route) },
                    onEventClick = { id -> navController.navigate("${Screen.EventDetail.route}/$id") }
                )
            }
            composable(
                route = "${Screen.AddEvent.route}?id={id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType; nullable = true })
            ) { backStackEntry ->
                val idArg = backStackEntry.arguments?.getString("id")
                AddEventScreen(
                    onDone = { navController.popBackStack() },
                    editingEventId = idArg?.toLongOrNull()
                )
            }
            composable(
                route = "${Screen.EventDetail.route}/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("id") ?: return@composable
                EventDetailScreen(
                    eventId = id,
                    onBack = { navController.popBackStack() },
                    onEditEvent = { eventId -> navController.navigate("${Screen.AddEvent.route}?id=$eventId") },
                    onAddExpense = { eventId -> navController.navigate("${Screen.AddEventExpense.route}/$eventId") },
                    onEditExpense = { expenseId ->
                        navController.navigate("${Screen.AddEventExpense.route}/$id?id=$expenseId")
                    }
                )
            }
            composable(
                route = "${Screen.AddEventExpense.route}/{eventId}?id={id}",
                arguments = listOf(
                    navArgument("eventId") { type = NavType.LongType },
                    navArgument("id") { type = NavType.StringType; nullable = true }
                )
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getLong("eventId") ?: return@composable
                val idArg = backStackEntry.arguments?.getString("id")
                AddEventExpenseScreen(
                    eventId = eventId,
                    onDone = { navController.popBackStack() },
                    editingExpenseId = idArg?.toLongOrNull()
                )
            }
        }
    }
}
