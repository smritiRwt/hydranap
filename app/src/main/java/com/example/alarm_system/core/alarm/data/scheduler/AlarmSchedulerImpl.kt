package com.example.alarm_system.core.alarm.data.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.alarm_system.core.alarm.domain.model.Alarm
import com.example.alarm_system.core.alarm.domain.repository.AlarmScheduler
import com.example.alarm_system.core.common.AlarmConstants
import com.example.alarm_system.core.receiver.AlarmReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AlarmSchedulerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AlarmScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun schedule(alarm: Alarm) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmConstants.ACTION_FIRE_ALARM
            putExtra(AlarmConstants.EXTRA_ALARM_ID, alarm.id)
            putExtra(AlarmConstants.EXTRA_ALARM_TITLE, alarm.title)
            putExtra(AlarmConstants.EXTRA_ALARM_SOUND, alarm.soundUri)
            putExtra(AlarmConstants.EXTRA_ALARM_VIBRATE, alarm.isVibrationEnabled)
        }

        // Using alarm.id as requestCode to ensure each alarm has a unique PendingIntent
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = alarm.nextTriggerTime

        // AlarmClockInfo is the gold standard for production alarms.
        // It provides high priority and shows the alarm icon in the status bar.
        val alarmClockInfo = AlarmManager.AlarmClockInfo(
            triggerTime,
            pendingIntent // In a full app, this might be a 'Show/Edit Alarm' activity
        )

        try {
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
        } catch (e: SecurityException) {
            // Handle cases where exact alarm permission is missing (Android 12+)
            e.printStackTrace()
        }
    }

    override fun cancel(alarm: Alarm) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmConstants.ACTION_FIRE_ALARM
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
