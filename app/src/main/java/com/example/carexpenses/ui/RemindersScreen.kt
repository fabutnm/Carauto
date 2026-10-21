package com.example.carexpenses.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.carexpenses.data.Reminder
import java.time.LocalDate

@Composable
fun RemindersScreen(
    reminders: List<Reminder>,
    currentOdometer: Int?,
    onSave: (Reminder) -> Unit,
    onDelete: (Reminder) -> Unit,
    modifier: Modifier = Modifier
) {
    var adding by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Reminder?>(null) }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Button(onClick = { adding = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Добавить напоминание")
            }
        }
        if (reminders.isEmpty()) {
            item {
                Text(
                    "Напоминаний нет. Добавьте, например, «Замена масла» с пробегом или датой.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
        items(reminders, key = { it.id }) { reminder ->
            ReminderRow(reminder, currentOdometer, onClick = { editing = reminder })
        }
    }

    if (adding) {
        ReminderDialog(
            initial = null,
            onDismiss = { adding = false },
            onSave = { onSave(it); adding = false },
            onDelete = {}
        )
    }
    editing?.let { reminder ->
        ReminderDialog(
            initial = reminder,
            onDismiss = { editing = null },
            onSave = { onSave(it); editing = null },
            onDelete = { onDelete(it); editing = null }
        )
    }
}

@Composable
private fun ReminderRow(reminder: Reminder, currentOdometer: Int?, onClick: () -> Unit) {
    val kmLeft = reminder.dueOdometer?.let { due -> currentOdometer?.let { due - it } }
    val daysLeft = reminder.dueDateEpochDay?.let { it - LocalDate.now().toEpochDay() }

    val overdue = (kmLeft != null && kmLeft <= 0) || (daysLeft != null && daysLeft <= 0)
    val soon = (kmLeft != null && kmLeft <= 1000) || (daysLeft != null && daysLeft <= 30)

    val statusColor: Color = when {
        overdue -> MaterialTheme.colorScheme.error
        soon -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val status = buildList {
        reminder.dueOdometer?.let { due ->
            add(
                when {
                    kmLeft == null -> "при пробеге ${formatKm(due)}"
                    kmLeft > 0 -> "осталось ${formatKm(kmLeft)}"
                    else -> "просрочено на ${formatKm(-kmLeft)}"
                }
            )
        }
        reminder.dueDateEpochDay?.let { due ->
            val text = when {
                daysLeft == null -> ""
                daysLeft > 0 -> "через $daysLeft дн."
                daysLeft == 0L -> "сегодня"
                else -> "просрочено на ${-daysLeft} дн."
            }
            add("${formatDate(due)} ($text)")
        }
    }.joinToString(" · ")

    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(reminder.title, style = MaterialTheme.typography.titleMedium)
            Text(status, style = MaterialTheme.typography.bodyMedium, color = statusColor)
        }
    }
}

@Composable
private fun ReminderDialog(
    initial: Reminder?,
    onDismiss: () -> Unit,
    onSave: (Reminder) -> Unit,
    onDelete: (Reminder) -> Unit
) {
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var odometer by remember { mutableStateOf(initial?.dueOdometer?.toString() ?: "") }
    var date by remember { mutableStateOf(initial?.dueDateEpochDay?.let(LocalDate::ofEpochDay)) }

    val odometerValue = odometer.trim().toIntOrNull()
    val canSave = title.isNotBlank() && (odometerValue != null || date != null)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Новое напоминание" else "Напоминание") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Что сделать") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = odometer,
                    onValueChange = { odometer = it.filter(Char::isDigit) },
                    label = { Text("При пробеге, км") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                DateField(
                    label = "До даты",
                    date = date,
                    onChange = { date = it },
                    clearable = true
                )
                Text(
                    "Укажите пробег, дату или оба условия.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (initial != null) {
                    TextButton(onClick = { onDelete(initial) }) {
                        Text("Удалить напоминание", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = canSave,
                onClick = {
                    onSave(
                        Reminder(
                            id = initial?.id ?: 0,
                            title = title.trim(),
                            dueOdometer = odometerValue,
                            dueDateEpochDay = date?.toEpochDay()
                        )
                    )
                }
            ) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}
