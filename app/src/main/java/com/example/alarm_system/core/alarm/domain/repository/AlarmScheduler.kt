package com.example.alarm_system.core.alarm.domain.repository

import com.example.alarm_system.core.alarm.domain.model.Alarm

interface AlarmScheduler {
    fun schedule(alarm: Alarm)
    fun cancel(alarm: Alarm)
}
