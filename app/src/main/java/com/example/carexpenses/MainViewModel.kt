package com.example.carexpenses

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.carexpenses.data.AppDatabase
import com.example.carexpenses.data.Expense
import com.example.carexpenses.data.Reminder
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.get(app)

    val expenses: StateFlow<List<Expense>> = db.expenseDao().observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val reminders: StateFlow<List<Reminder>> = db.reminderDao().observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun saveExpense(expense: Expense) {
        viewModelScope.launch { db.expenseDao().upsert(expense) }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch { db.expenseDao().delete(expense) }
    }

    fun saveReminder(reminder: Reminder) {
        viewModelScope.launch { db.reminderDao().upsert(reminder) }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch { db.reminderDao().delete(reminder) }
    }
}
