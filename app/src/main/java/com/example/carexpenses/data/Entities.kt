package com.example.carexpenses.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ExpenseType(val label: String, val emoji: String) {
    FUEL("Заправка", "⛽"),
    SERVICE("ТО", "🛠"),
    REPAIR("Ремонт", "🔧"),
    INSURANCE("Страховка", "🛡"),
    TAX("Налог", "🏛"),
    PARKING("Парковка", "🅿"),
    WASH("Мойка", "🚿"),
    FINE("Штраф", "🚨"),
    OTHER("Другое", "📦")
}

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: ExpenseType,
    val dateEpochDay: Long,
    val amount: Double,
    val odometer: Int? = null,
    val liters: Double? = null,
    val fullTank: Boolean = true,
    val note: String = ""
)

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val dueOdometer: Int? = null,
    val dueDateEpochDay: Long? = null
)
