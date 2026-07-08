package com.example.alarm_system.core.alarm.data.scheduler

import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters

object AlarmDateTimeUtils {

    fun getNextTriggerMillis(time: LocalTime, days: Set<DayOfWeek>): Long {
        val now = LocalDateTime.now()
        val today = now.toLocalDate()
        
        // If no days are selected, it's a one-time alarm
        if (days.isEmpty()) {
            var alarmDateTime = LocalDateTime.of(today, time)
            if (alarmDateTime.isBefore(now)) {
                alarmDateTime = alarmDateTime.plusDays(1)
            }
            return alarmDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }

        // For repeating alarms, find the earliest upcoming day
        val nextOccurrences = days.map { day ->
            var nextDate = today.with(TemporalAdjusters.nextOrSame(day))
            var alarmDateTime = LocalDateTime.of(nextDate, time)
            
            // If the calculated time for today has already passed, move to next week
            if (alarmDateTime.isBefore(now)) {
                nextDate = today.with(TemporalAdjusters.next(day))
                alarmDateTime = LocalDateTime.of(nextDate, time)
            }
            alarmDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }

        return nextOccurrences.minOrNull() ?: 0L
    }
}
