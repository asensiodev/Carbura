package com.asensiodev.carbura.core.model

@JvmInline
value class CalendarDate(
    val iso8601: String,
) : Comparable<CalendarDate> {
    init {
        require(iso8601.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
            "CalendarDate must use yyyy-MM-dd format"
        }
        val year = iso8601.substring(0, 4).toInt()
        val month = iso8601.substring(5, 7).toInt()
        val day = iso8601.substring(8, 10).toInt()
        require(month in 1..12 && day in 1..daysInMonth(year, month)) {
            "CalendarDate must be a valid calendar date"
        }
    }

    override fun compareTo(other: CalendarDate): Int = iso8601.compareTo(other.iso8601)
}

private fun daysInMonth(
    year: Int,
    month: Int,
): Int =
    when (month) {
        2 -> if (year.isLeapYear()) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }

private fun Int.isLeapYear(): Boolean = this % 4 == 0 && (this % 100 != 0 || this % 400 == 0)
