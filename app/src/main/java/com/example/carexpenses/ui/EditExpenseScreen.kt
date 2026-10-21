package com.example.carexpenses.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.carexpenses.data.Expense
import com.example.carexpenses.data.ExpenseType
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExpenseScreen(
    initial: Expense?,
    lastOdometer: Int?,
    onSave: (Expense) -> Unit,
    onDelete: (Expense) -> Unit,
    onClose: () -> Unit
) {
    BackHandler(onBack = onClose)

    var type by remember { mutableStateOf(initial?.type ?: ExpenseType.FUEL) }
    var date by remember {
        mutableStateOf(initial?.let { LocalDate.ofEpochDay(it.dateEpochDay) } ?: LocalDate.now())
    }
    var amount by remember { mutableStateOf(initial?.amount?.let(::fmtInput) ?: "") }
    var odometer by remember { mutableStateOf(initial?.odometer?.toString() ?: "") }
    var liters by remember { mutableStateOf(initial?.liters?.let(::fmtInput) ?: "") }
    var fullTank by remember { mutableStateOf(initial?.fullTank ?: true) }
    var note by remember { mutableStateOf(initial?.note ?: "") }
    var confirmDelete by remember { mutableStateOf(false) }

    val amountValue = amount.toDecimal()
    val litersValue = liters.toDecimal()
    val odometerValue = odometer.trim().toIntOrNull()
    val canSave = amountValue != null && amountValue > 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (initial == null) "Новый расход" else "Редактирование") },
                navigationIcon = { TextButton(onClick = onClose) { Text("Отмена") } },
                actions = {
                    if (initial != null) {
                        IconButton(onClick = { confirmDelete = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Удалить")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExpenseType.values().forEach { t ->
                    FilterChip(
                        selected = type == t,
                        onClick = { type = t },
                        label = { Text("${t.emoji} ${t.label}") }
                    )
                }
            }

            DateField("Дата", date, onChange = { it?.let { d -> date = d } })

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Сумма, ₽") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = odometer,
                onValueChange = { odometer = it.filter(Char::isDigit) },
                label = { Text("Пробег, км (необязательно)") },
                supportingText = {
                    if (lastOdometer != null) Text("Последний пробег: ${formatKm(lastOdometer)}")
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            if (type == ExpenseType.FUEL) {
                OutlinedTextField(
                    value = liters,
                    onValueChange = { liters = it },
                    label = { Text("Литры") },
                    supportingText = {
                        if (amountValue != null && litersValue != null && litersValue > 0) {
                            Text("Цена литра: ${fmt1(amountValue / litersValue)} ₽")
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Полный бак", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Нужно для точного расчёта расхода",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = fullTank, onCheckedChange = { fullTank = it })
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Заметка") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    onSave(
                        Expense(
                            id = initial?.id ?: 0,
                            type = type,
                            dateEpochDay = date.toEpochDay(),
                            amount = amountValue ?: 0.0,
                            odometer = odometerValue,
                            liters = if (type == ExpenseType.FUEL) litersValue else null,
                            fullTank = fullTank,
                            note = note.trim()
                        )
                    )
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Сохранить") }
        }
    }

    if (confirmDelete && initial != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Удалить запись?") },
            text = { Text("Это действие нельзя отменить.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDelete(initial)
                }) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Отмена") }
            }
        )
    }
}
