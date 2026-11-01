package com.floratend.app

import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

object Recurrence {
    fun next(completed: LocalDate, interval: Int): LocalDate { require(interval in 1..3650); return completed.plusDays(interval.toLong()) }
    fun reminder(date: LocalDate, zone: ZoneId): ZonedDateTime = date.atTime(9, 0).atZone(zone)
}
