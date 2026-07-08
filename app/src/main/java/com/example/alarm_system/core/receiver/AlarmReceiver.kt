package com.example.alarm_system.core.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.alarm_system.core.common.AlarmConstants

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntOf(AlarmConstants.EXTRA_ALARM_ID, -1)
        val alarmTitle = intent.getStringExtra(AlarmConstants.EXTRA_ALARM_TITLE) ?: "Alarm"

        if (alarmId == -1) return

        Log.d("AlarmReceiver", "Alarm Fired: $alarmId - $alarmTitle")

        val serviceIntent = Intent(context, com.example.alarm_system.core.alarm.data.service.AlarmService::class.java).apply {
            action = AlarmConstants.ACTION_FIRE_ALARM
            putExtra(AlarmConstants.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmConstants.EXTRA_ALARM_TITLE, alarmTitle)
            putExtra(AlarmConstants.EXTRA_ALARM_SOUND, intent.getStringExtra(AlarmConstants.EXTRA_ALARM_SOUND))
            putExtra(AlarmConstants.EXTRA_ALARM_VIBRATE, intent.getBooleanExtra(AlarmConstants.EXTRA_ALARM_VIBRATE, true))
        }

        context.startForegroundService(serviceIntent)
    }

    private fun Intent.getIntOf(key: String, defaultValue: Int): Int {
        return if (hasExtra(key)) getIntExtra(key, defaultValue) else defaultValue
    }
}
