package com.example.alarm_system.core.alarm.data.repository

import com.example.alarm_system.core.alarm.data.mapper.toDomain
import com.example.alarm_system.core.alarm.data.mapper.toEntity
import com.example.alarm_system.core.alarm.data.scheduler.AlarmDateTimeUtils
import com.example.alarm_system.core.alarm.domain.model.Alarm
import com.example.alarm_system.core.alarm.domain.repository.AlarmRepository
import com.example.alarm_system.core.alarm.domain.repository.AlarmScheduler
import com.example.alarm_system.core.database.dao.AlarmDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AlarmRepositoryImpl @Inject constructor(
    private val alarmDao: AlarmDao,
    private val alarmScheduler: AlarmScheduler
) : AlarmRepository {

    override fun getAlarms(): Flow<List<Alarm>> {
        return alarmDao.getAllAlarms().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getAlarmById(id: Int): Alarm? {
        return alarmDao.getAlarmById(id)?.toDomain()
    }

    override suspend fun insertAlarm(alarm: Alarm): Long {
        val nextTrigger = AlarmDateTimeUtils.getNextTriggerMillis(alarm.time, alarm.repeatDays)
        val alarmToSave = alarm.copy(nextTriggerTime = nextTrigger, isEnabled = true)
        val id = alarmDao.insertAlarm(alarmToSave.toEntity())
        
        // Reschedule with the new ID
        alarmScheduler.schedule(alarmToSave.copy(id = id.toInt()))
        return id
    }

    override suspend fun updateAlarm(alarm: Alarm) {
        val nextTrigger = AlarmDateTimeUtils.getNextTriggerMillis(alarm.time, alarm.repeatDays)
        val alarmToUpdate = alarm.copy(nextTriggerTime = nextTrigger)
        alarmDao.updateAlarm(alarmToUpdate.toEntity())
        
        if (alarmToUpdate.isEnabled) {
            alarmScheduler.schedule(alarmToUpdate)
        } else {
            alarmScheduler.cancel(alarmToUpdate)
        }
    }

    override suspend fun deleteAlarm(alarm: Alarm) {
        alarmDao.deleteAlarm(alarm.toEntity())
        alarmScheduler.cancel(alarm)
    }

    override suspend fun toggleAlarm(id: Int, isEnabled: Boolean) {
        val alarmEntity = alarmDao.getAlarmById(id) ?: return
        val alarm = alarmEntity.toDomain().copy(isEnabled = isEnabled)
        
        if (isEnabled) {
            val nextTrigger = AlarmDateTimeUtils.getNextTriggerMillis(alarm.time, alarm.repeatDays)
            val updatedAlarm = alarm.copy(nextTriggerTime = nextTrigger)
            alarmDao.updateAlarm(updatedAlarm.toEntity())
            alarmScheduler.schedule(updatedAlarm)
        } else {
            alarmDao.updateAlarmEnabledState(id, false)
            alarmScheduler.cancel(alarm)
        }
    }

    override suspend fun scheduleAlarm(alarm: Alarm) {
        alarmScheduler.schedule(alarm)
    }

    override suspend fun cancelAlarm(alarm: Alarm) {
        alarmScheduler.cancel(alarm)
    }

    override suspend fun snoozeAlarm(alarm: Alarm) {
        val snoozeTime = System.currentTimeMillis() + (alarm.snoozeMinutes * 60 * 1000)
        val snoozedAlarm = alarm.copy(nextTriggerTime = snoozeTime)
        // We don't necessarily update the DB for snooze to keep the original time intact,
        // but we schedule a one-time trigger.
        alarmScheduler.schedule(snoozedAlarm)
    }

    override suspend fun rescheduleAllEnabledAlarms() {
        val enabledAlarms = alarmDao.getEnabledAlarms().first()
        enabledAlarms.forEach { entity ->
            val alarm = entity.toDomain()
            val nextTrigger = AlarmDateTimeUtils.getNextTriggerMillis(alarm.time, alarm.repeatDays)
            val updatedAlarm = alarm.copy(nextTriggerTime = nextTrigger)
            alarmDao.updateAlarm(updatedAlarm.toEntity())
            alarmScheduler.schedule(updatedAlarm)
        }
    }
}
