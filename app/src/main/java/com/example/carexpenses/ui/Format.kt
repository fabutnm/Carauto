package com.example.carexpenses.ui

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val RU = Locale("ru", "RU")
private val dateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", RU)

fun formatMoney(value: Double): String = String.format(RU, "%,.0f ₽", value)

fun formatKm(value: Int): String = String.format(RU, "%,d км", value)

fun fmt1(value: Double): String = String.format(RU, "%.1f", value)

fun formatDate(epochDay: Long): String = LocalDate.ofEpochDay(epochDay).format(dateFormatter)

/** Принимает и «12,5», и «12.5». */
fun String.toDecimal(): Double? = trim().replace(',', '.').toDoubleOrNull()

/** Число для поля ввода: без хвоста «.0». */
fun fmtInput(value: Double): String =
    if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()
