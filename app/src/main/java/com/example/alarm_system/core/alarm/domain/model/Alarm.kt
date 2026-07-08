package com.example.alarm_system.core.alarm.domain.model

import java.time.DayOfWeek
import java.time.LocalTime

data class Alarm(
    val id: Int = 0,
    val title: String,
    val description: String,
    val time: LocalTime,
    val repeatType: RepeatType,
    val repeatDays: Set<DayOfWeek>,
    val isEnabled: Boolean,
    val soundUri: String?,
    val isVibrationEnabled: Boolean,
    val snoozeMinutes: Int,
    val nextTriggerTime: Long
)

enum class RepeatType {
    ONCE, DAILY, WEEKLY, CUSTOM
}
