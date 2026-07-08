package com.example.alarm_system.core.alarm.domain.repository

import com.example.alarm_system.core.alarm.domain.model.Alarm
import kotlinx.coroutines.flow.Flow

interface AlarmRepository {
    fun getAlarms(): Flow<List<Alarm>>
    suspend fun getAlarmById(id: Int): Alarm?
    suspend fun insertAlarm(alarm: Alarm): Long
    suspend fun updateAlarm(alarm: Alarm)
    suspend fun deleteAlarm(alarm: Alarm)
    suspend fun toggleAlarm(id: Int, isEnabled: Boolean)
    
    // Coordination with AlarmManager (to be implemented in next step)
    suspend fun scheduleAlarm(alarm: Alarm)
    suspend fun cancelAlarm(alarm: Alarm)
    suspend fun snoozeAlarm(alarm: Alarm)
    suspend fun rescheduleAllEnabledAlarms()
}
