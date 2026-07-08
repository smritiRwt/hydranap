package com.example.alarm_system.core.alarm.data.mapper

import com.example.alarm_system.core.alarm.domain.model.Alarm
import com.example.alarm_system.core.alarm.domain.model.RepeatType
import com.example.alarm_system.core.database.entity.AlarmEntity
import java.time.DayOfWeek
import java.time.LocalTime

fun AlarmEntity.toDomain(): Alarm {
    return Alarm(
        id = id,
        title = title,
        description = description,
        time = LocalTime.of(hour, minute),
        repeatType = RepeatType.entries[repeatType],
        repeatDays = if (days.isEmpty()) emptySet() else days.split(",").map { DayOfWeek.of(it.toInt()) }.toSet(),
        isEnabled = isEnabled,
        soundUri = soundUri,
        isVibrationEnabled = isVibrationEnabled,
        snoozeMinutes = snoozeMinutes,
        nextTriggerTime = nextTriggerTime
    )
}

fun Alarm.toEntity(): AlarmEntity {
    return AlarmEntity(
        id = id,
        title = title,
        description = description,
        hour = time.hour,
        minute = time.minute,
        repeatType = repeatType.ordinal,
        days = repeatDays.map { it.value }.joinToString(","),
        isEnabled = isEnabled,
        soundUri = soundUri,
        isVibrationEnabled = isVibrationEnabled,
        snoozeMinutes = snoozeMinutes,
        nextTriggerTime = nextTriggerTime
    )
}
