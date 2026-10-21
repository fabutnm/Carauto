package com.example.carexpenses

import com.example.carexpenses.data.Expense
import com.example.carexpenses.data.ExpenseType
import java.time.LocalDate
import java.time.YearMonth

enum class Period(val label: String) {
    ALL("Всё время"),
    YEAR("12 месяцев"),
    QUARTER("90 дней")
}

data class MonthTotal(val month: YearMonth, val total: Double)

data class Summary(
    val total: Double,
    val fuelCost: Double,
    val avgConsumption: Double?,      // л/100 км
    val consumptionSeries: List<Double>,
    val avgPricePerLiter: Double?,
    val distanceKm: Int?,
    val costPerKm: Double?,
    val byType: List<Pair<ExpenseType, Double>>,
    val byMonth: List<MonthTotal>
)

/**
 * Расход топлива методом «до полного бака».
 * Первая полная заправка — точка отсчёта. Дальше литры всех заправок
 * (включая неполные) между двумя полными делятся на пройденное расстояние.
 */
fun fuelConsumption(expenses: List<Expense>): Pair<List<Double>, Double?> {
    val fills = expenses
        .filter { it.type == ExpenseType.FUEL && it.odometer != null && (it.liters ?: 0.0) > 0.0 }
        .sortedBy { it.odometer }

    val series = mutableListOf<Double>()
    var lastFullOdo: Int? = null
    var litersAcc = 0.0
    var sumLiters = 0.0
    var sumDistance = 0.0

    for (fill in fills) {
        val odo = fill.odometer ?: continue
        val liters = fill.liters ?: continue
        val base = lastFullOdo
        if (base == null) {
            if (fill.fullTank) {
                lastFullOdo = odo
                litersAcc = 0.0
            }
            continue
        }
        litersAcc += liters
        if (fill.fullTank) {
            val distance = odo - base
            if (distance > 0) {
                series += litersAcc / distance * 100.0
                sumLiters += litersAcc
                sumDistance += distance
            }
            lastFullOdo = odo
            litersAcc = 0.0
        }
    }
    val avg = if (sumDistance > 0) sumLiters / sumDistance * 100.0 else null
    return series to avg
}

fun computeSummary(all: List<Expense>, period: Period, today: LocalDate = LocalDate.now()): Summary {
    val from = when (period) {
        Period.ALL -> Long.MIN_VALUE
        Period.YEAR -> today.minusYears(1).toEpochDay()
        Period.QUARTER -> today.minusDays(90).toEpochDay()
    }
    val list = all.filter { it.dateEpochDay >= from }

    val total = list.sumOf { it.amount }
    val fuel = list.filter { it.type == ExpenseType.FUEL }
    val fuelCost = fuel.sumOf { it.amount }
    val fuelLiters = fuel.sumOf { it.liters ?: 0.0 }
    val avgPrice = if (fuelLiters > 0) fuel.filter { (it.liters ?: 0.0) > 0 }.sumOf { it.amount } / fuelLiters else null

    val (series, avgConsumption) = fuelConsumption(list)

    val odometers = list.mapNotNull { it.odometer }
    val distance = if (odometers.size >= 2) odometers.max() - odometers.min() else null
    val costPerKm = if (distance != null && distance > 0) total / distance else null

    val byType = list.groupBy { it.type }
        .map { (type, items) -> type to items.sumOf { it.amount } }
        .sortedByDescending { it.second }

    val currentMonth = YearMonth.from(today)
    val byMonth = (11 downTo 0).map { back ->
        val month = currentMonth.minusMonths(back.toLong())
        val sum = list
            .filter { YearMonth.from(LocalDate.ofEpochDay(it.dateEpochDay)) == month }
            .sumOf { it.amount }
        MonthTotal(month, sum)
    }

    return Summary(
        total = total,
        fuelCost = fuelCost,
        avgConsumption = avgConsumption,
        consumptionSeries = series,
        avgPricePerLiter = avgPrice,
        distanceKm = distance,
        costPerKm = costPerKm,
        byType = byType,
        byMonth = byMonth
    )
}
