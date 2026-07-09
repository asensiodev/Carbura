package com.asensiodev.carbura.core.model

@JvmInline
value class CalendarDate(val iso8601: String) : Comparable<CalendarDate> {
    init {
        require(iso8601.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
            "CalendarDate must use yyyy-MM-dd format"
        }
    }

    override fun compareTo(other: CalendarDate): Int = iso8601.compareTo(other.iso8601)
}
