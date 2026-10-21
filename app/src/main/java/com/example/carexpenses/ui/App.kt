package com.example.carexpenses.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.carexpenses.MainViewModel
import com.example.carexpenses.data.Expense

private val tabs = listOf(
    "⛽" to "Расходы",
    "📊" to "Статистика",
    "🔔" to "ТО"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(viewModel: MainViewModel) {
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()

    var tab by rememberSaveable { mutableIntStateOf(0) }
    var creating by rememberSaveable { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Expense?>(null) }

    val lastOdometer = expenses.mapNotNull { it.odometer }.maxOrNull()

    if (creating || editing != null) {
        EditExpenseScreen(
            initial = editing,
            lastOdometer = lastOdometer,
            onSave = {
                viewModel.saveExpense(it)
                creating = false
                editing = null
            },
            onDelete = {
                viewModel.deleteExpense(it)
                creating = false
                editing = null
            },
            onClose = {
                creating = false
                editing = null
            }
        )
        return
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Расходы на авто") }) },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, (emoji, label) ->
                    NavigationBarItem(
                        selected = tab == index,
                        onClick = { tab = index },
                        icon = { Text(emoji, fontSize = 20.sp) },
                        label = { Text(label) }
                    )
                }
            }
        },
        floatingActionButton = {
            if (tab == 0) {
                FloatingActionButton(onClick = { creating = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить расход")
                }
            }
        }
    ) { padding ->
        val contentModifier = Modifier.padding(padding)
        when (tab) {
            0 -> ExpensesScreen(expenses, onEdit = { editing = it }, modifier = contentModifier)
            1 -> StatsScreen(expenses, modifier = contentModifier)
            else -> RemindersScreen(
                reminders = reminders,
                currentOdometer = lastOdometer,
                onSave = viewModel::saveReminder,
                onDelete = viewModel::deleteReminder,
                modifier = contentModifier
            )
        }
    }
}
