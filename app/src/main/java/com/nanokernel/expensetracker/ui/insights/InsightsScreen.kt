package com.nanokernel.expensetracker.ui.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.nanokernel.expensetracker.ExpenseTrackerApp
import com.nanokernel.expensetracker.ui.components.InsightCard
import com.nanokernel.expensetracker.ui.components.ScreenHeader

@Composable
fun InsightsScreen() {
    val context = LocalContext.current
    val app = context.applicationContext as ExpenseTrackerApp
    val viewModel: InsightsViewModel = viewModel(
        factory = viewModelFactory {
            initializer { InsightsViewModel(app.repository, app.settingsRepository) }
        }
    )
    val state by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { ScreenHeader("Insights") }

        item { Text("Needs vs Wants", style = MaterialTheme.typography.titleMedium) }

        item { NeedsWantsBar(needsPercent = state.needsPercent, wantsPercent = state.wantsPercent) }

        item { Text("Insights", style = MaterialTheme.typography.titleMedium) }

        if (state.insights.isEmpty()) {
            item { Text("Add a few expenses this month to see personalized insights.") }
        } else {
            items(state.insights) { insight -> InsightCard(emoji = insight.emoji, text = insight.text) }
        }
    }
}

@Composable
private fun NeedsWantsBar(needsPercent: Int, wantsPercent: Int) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
        ) {
            if (needsPercent > 0) {
                Box(
                    modifier = Modifier
                        .weight(needsPercent.toFloat().coerceAtLeast(0.01f))
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
            if (wantsPercent > 0) {
                Box(
                    modifier = Modifier
                        .weight(wantsPercent.toFloat().coerceAtLeast(0.01f))
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.secondary)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Needs $needsPercent%", style = MaterialTheme.typography.bodyMedium)
            Text("Wants $wantsPercent%", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
