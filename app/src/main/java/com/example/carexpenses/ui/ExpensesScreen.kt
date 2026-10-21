package com.example.carexpenses.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carexpenses.data.Expense
import com.example.carexpenses.data.ExpenseType
import java.time.LocalDate

@Composable
fun ExpensesScreen(
    expenses: List<Expense>,
    onEdit: (Expense) -> Unit,
    modifier: Modifier = Modifier
) {
    if (expenses.isEmpty()) {
        Box(modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Text(
                "Пока нет записей.\nНажмите «+», чтобы добавить первый расход.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        return
    }

    val today = LocalDate.now()
    val monthTotal = expenses
        .filter {
            val d = LocalDate.ofEpochDay(it.dateEpochDay)
            d.year == today.year && d.month == today.month
        }
        .sumOf { it.amount }
    val lastOdometer = expenses.mapNotNull { it.odometer }.maxOrNull()

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Card(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("В этом месяце", style = MaterialTheme.typography.labelMedium)
                        Text(formatMoney(monthTotal), style = MaterialTheme.typography.headlineSmall)
                    }
                    if (lastOdometer != null) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Пробег", style = MaterialTheme.typography.labelMedium)
                            Text(formatKm(lastOdometer), style = MaterialTheme.typography.headlineSmall)
                        }
                    }
                }
            }
        }
        items(expenses, key = { it.id }) { expense ->
            ExpenseRow(expense, onClick = { onEdit(expense) })
        }
    }
}

@Composable
private fun ExpenseRow(expense: Expense, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(expense.type.emoji, fontSize = 26.sp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(expense.type.label, style = MaterialTheme.typography.titleMedium)
                Text(
                    details(expense),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(formatMoney(expense.amount), style = MaterialTheme.typography.titleMedium)
        }
    }
}

private fun details(e: Expense): String {
    val parts = mutableListOf(formatDate(e.dateEpochDay))
    val liters = e.liters
    if (e.type == ExpenseType.FUEL && liters != null && liters > 0) {
        parts += "${fmt1(liters)} л · ${fmt1(e.amount / liters)} ₽/л" + if (e.fullTank) "" else " · неполный"
    }
    e.odometer?.let { parts += formatKm(it) }
    if (e.note.isNotBlank()) parts += e.note
    return parts.joinToString(" · ")
}
