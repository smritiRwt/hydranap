package com.example.alarm_system.core.alarm.data.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.content.pm.ServiceInfo
import java.time.LocalTime
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import com.example.alarm_system.MainActivity
import com.example.alarm_system.R
import com.example.alarm_system.core.alarm.domain.repository.AlarmRepository
import com.example.alarm_system.core.common.AlarmConstants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AlarmService : Service() {

    @Inject
    lateinit var repository: AlarmRepository

    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val prefs by lazy { getSharedPreferences(AlarmConstants.PREFS_NAME, Context.MODE_PRIVATE) }
    
    private val activeNotifications = mutableSetOf<Int>()
    private var currentForegroundId = 0
    private val notificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    companion object {
        private const val CHANNEL_ID = "alarm_service_channel"
        private const val TIMER_CHANNEL_ID = "timer_service_channel"
        private const val STOPWATCH_CHANNEL_ID = "stopwatch_service_channel"
        private const val NOTIFICATION_ID = 1001
        private const val TIMER_NOTIFICATION_ID = 1002
        private const val STOPWATCH_NOTIFICATION_ID = 1004
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val alarmId = intent?.getIntExtra(AlarmConstants.EXTRA_ALARM_ID, -1) ?: -1
        
        if (action == "STOP_ALARM_ACTION") {
            stopAlarm()
            stopForegroundService(NOTIFICATION_ID)
            return START_NOT_STICKY
        }

        action?.let {
            when (it) {
                AlarmConstants.ACTION_START_TIMER -> {
                    val duration = intent.getLongExtra(AlarmConstants.EXTRA_TIMER_DURATION, 0L)
                    val endTime = System.currentTimeMillis() + duration
                    prefs.edit {
                        putLong(AlarmConstants.PREF_TIMER_END_TIME, endTime)
                        putLong(AlarmConstants.PREF_TIMER_TOTAL_TIME, duration)
                    }
                    startForegroundService(TIMER_NOTIFICATION_ID, createRunningTimerNotification(duration))
                    return START_STICKY
                }
                AlarmConstants.ACTION_STOP_TIMER -> {
                    prefs.edit { remove(AlarmConstants.PREF_TIMER_END_TIME) }
                    stopForegroundService(TIMER_NOTIFICATION_ID)
                    return START_NOT_STICKY
                }
                AlarmConstants.ACTION_START_STOPWATCH -> {
                    val startTime = intent.getLongExtra(AlarmConstants.EXTRA_STOPWATCH_START_TIME, System.currentTimeMillis())
                    prefs.edit {
                        putLong(AlarmConstants.PREF_STOPWATCH_START_TIME, startTime)
                        putBoolean(AlarmConstants.PREF_STOPWATCH_IS_RUNNING, true)
                    }
                    startForegroundService(STOPWATCH_NOTIFICATION_ID, createStopwatchNotification(startTime))
                    return START_STICKY
                }
                AlarmConstants.ACTION_STOP_STOPWATCH -> {
                    val pauseTime = intent.getLongExtra(AlarmConstants.PREF_STOPWATCH_PAUSE_TIME, 0L)
                    prefs.edit {
                        putBoolean(AlarmConstants.PREF_STOPWATCH_IS_RUNNING, false)
                        putLong(AlarmConstants.PREF_STOPWATCH_PAUSE_TIME, pauseTime)
                    }
                    stopForegroundService(STOPWATCH_NOTIFICATION_ID)
                    return START_NOT_STICKY
                }
                AlarmConstants.ACTION_DISMISS_ALARM -> {
                    if (alarmId != -1) {
                        serviceScope.launch {
                            try {
                                val alarm = repository.getAlarmById(alarmId)
                                if (alarm != null) {
                                    if (alarm.title == "Water Reminder") {
                                        val interval = prefs.getInt(AlarmConstants.PREF_WATER_INTERVAL, 20)
                                        val nextTime = LocalTime.now().plusMinutes(interval.toLong())
                                        repository.updateAlarm(alarm.copy(time = nextTime, isEnabled = true))
                                    } else {
                                        repository.updateAlarm(alarm.copy(isEnabled = alarm.repeatDays.isNotEmpty()))
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                    stopAlarm()
                    stopForegroundService(NOTIFICATION_ID)
                    return START_NOT_STICKY
                }
                AlarmConstants.ACTION_SNOOZE_ALARM -> {
                    if (alarmId != -1) {
                        serviceScope.launch {
                            try {
                                val alarm = repository.getAlarmById(alarmId)
                                if (alarm != null) {
                                    if (alarm.title == "Water Reminder") {
                                        val interval = prefs.getInt(AlarmConstants.PREF_WATER_INTERVAL, 20)
                                        val nextTime = LocalTime.now().plusMinutes(interval.toLong())
                                        repository.updateAlarm(alarm.copy(time = nextTime, isEnabled = true))
                                    } else {
                                        repository.snoozeAlarm(alarm)
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                    stopAlarm()
                    stopForegroundService(NOTIFICATION_ID)
                    return START_NOT_STICKY
                }
                AlarmConstants.ACTION_TIMER_EXPIRED -> {
                    startForegroundService(NOTIFICATION_ID, createTimerExpiredNotification())
                    startAlarm(null, true)
                    return START_STICKY
                }
            }
        }

        // Handle regular alarm firing
        if (alarmId != -1) {
            val alarmTitle = intent?.getStringExtra(AlarmConstants.EXTRA_ALARM_TITLE) ?: "Alarm"
            val soundUri = intent?.getStringExtra(AlarmConstants.EXTRA_ALARM_SOUND)
            val vibrate = intent?.getBooleanExtra(AlarmConstants.EXTRA_ALARM_VIBRATE, true) ?: true

            startForegroundService(NOTIFICATION_ID, createAlarmNotification(alarmId, alarmTitle))
            startAlarm(soundUri, vibrate)
        }

        return START_STICKY
    }

    private fun startForegroundService(id: Int, notification: Notification) {
        activeNotifications.add(id)
        try {
            if (currentForegroundId == 0 || currentForegroundId == id) {
                if (Build.VERSION.SDK_INT >= 29) { // Q and above
                    startForeground(
                        id,
                        notification,
                        @Suppress("InlinedApi")
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                    )
                } else {
                    startForeground(id, notification)
                }
                currentForegroundId = id
            } else {
                notificationManager.notify(id, notification)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            notificationManager.notify(id, notification)
        }
    }

    private fun stopForegroundService(id: Int) {
        activeNotifications.remove(id)
        notificationManager.cancel(id)
        
        if (currentForegroundId == id) {
            currentForegroundId = 0
            val nextId = activeNotifications.firstOrNull()
            if (nextId != null) {
                promoteToForeground(nextId)
            } else {
                stopForeground(STOP_FOREGROUND_REMOVE)
                if (ringtone == null) stopSelf()
            }
        } else if (activeNotifications.isEmpty() && ringtone == null) {
            stopSelf()
        }
    }

    private fun promoteToForeground(id: Int) {
        val notification = when (id) {
            TIMER_NOTIFICATION_ID -> {
                val endTime = prefs.getLong(AlarmConstants.PREF_TIMER_END_TIME, 0L)
                val remaining = endTime - System.currentTimeMillis()
                if (remaining > 0) createRunningTimerNotification(remaining) else null
            }
            STOPWATCH_NOTIFICATION_ID -> {
                val startTime = prefs.getLong(AlarmConstants.PREF_STOPWATCH_START_TIME, 0L)
                if (startTime > 0) createStopwatchNotification(startTime) else null
            }
            else -> null
        }

        if (notification != null) {
            try {
                if (Build.VERSION.SDK_INT >= 29) {
                    @Suppress("InlinedApi")
                    startForeground(id, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
                } else {
                    startForeground(id, notification)
                }
                currentForegroundId = id
            } catch (e: Exception) {
                e.printStackTrace()
                notificationManager.notify(id, notification)
            }
        } else {
            activeNotifications.remove(id)
            val nextId = activeNotifications.firstOrNull()
            if (nextId != null) promoteToForeground(nextId)
            else {
                stopForeground(STOP_FOREGROUND_REMOVE)
                if (ringtone == null) stopSelf()
            }
        }
    }

    private fun createRunningTimerNotification(remainingMillis: Long): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "timer")
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, AlarmService::class.java).apply {
            action = AlarmConstants.ACTION_STOP_TIMER
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, TIMER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle("Timer Running")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_alarm, "Stop", stopPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setWhen(System.currentTimeMillis() + remainingMillis)
            .build()
    }

    private fun createStopwatchNotification(startTime: Long): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "stopwatch")
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 4, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, AlarmService::class.java).apply {
            action = AlarmConstants.ACTION_STOP_STOPWATCH
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 4, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, STOPWATCH_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle("Stopwatch Running")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_alarm, "Stop", stopPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setUsesChronometer(true)
            .setWhen(startTime)
            .build()
    }

    private fun startAlarm(soundUri: String?, vibrate: Boolean) {
        stopAlarm() // Stop any previous alarm
        
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val alarmVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
        if (alarmVolume == 0) {
            audioManager.setStreamVolume(
                AudioManager.STREAM_ALARM,
                (audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM) * 0.7).toInt(),
                0
            )
        }

        val alarmUri: Uri = soundUri?.let { Uri.parse(it) }
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        
        try {
            ringtone = RingtoneManager.getRingtone(this, alarmUri)
            if (ringtone == null) {
                // Fallback to default alarm sound if the specified one fails
                val defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ringtone = RingtoneManager.getRingtone(this, defaultUri)
            }

            ringtone?.apply {
                audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    isLooping = true
                }
                play()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Last resort: try playing the default ringtone if alarm sound fails
            try {
                val fallbackUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                val fallbackRingtone = RingtoneManager.getRingtone(this, fallbackUri)
                fallbackRingtone?.play()
                ringtone = fallbackRingtone
            } catch (inner: Exception) {
                inner.printStackTrace()
            }
        }

        if (vibrate) {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            val pattern = longArrayOf(0, 1000, 1000)
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        }
    }

    private fun stopAlarm() {
        ringtone?.let {
            if (it.isPlaying) it.stop()
        }
        ringtone = null
        vibrator?.cancel()
        vibrator = null
    }

    override fun onDestroy() {
        stopAlarm()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createAlarmNotification(alarmId: Int, title: String): Notification {
        val fullScreenIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(AlarmConstants.EXTRA_ALARM_ID, alarmId)
            putExtra("is_ringing", true)
        }
        
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this, 0, fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = Intent(this, AlarmService::class.java).apply {
            action = AlarmConstants.ACTION_DISMISS_ALARM
            putExtra(AlarmConstants.EXTRA_ALARM_ID, alarmId)
        }
        val dismissPendingIntent = PendingIntent.getService(
            this, alarmId, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(this, AlarmService::class.java).apply {
            action = AlarmConstants.ACTION_SNOOZE_ALARM
            putExtra(AlarmConstants.EXTRA_ALARM_ID, alarmId)
        }
        val snoozePendingIntent = PendingIntent.getService(
            this, alarmId, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle("Alarm Ringing")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(R.drawable.ic_alarm, "Dismiss", dismissPendingIntent)
            .addAction(R.drawable.ic_alarm, "Snooze", snoozePendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun createTimerExpiredNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "timer")
            putExtra("is_timer_expired", true)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, AlarmService::class.java).apply {
            action = "STOP_ALARM_ACTION"
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 2, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle("Timer Expired")
            .setContentText("Your timer has finished")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pendingIntent, true)
            .addAction(R.drawable.ic_alarm, "Stop", stopPendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java)
        
        // Alarm Channel
        val alarmChannel = NotificationChannel(
            CHANNEL_ID,
            "Alarm Service Channel",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Channel for Alarms and Expired Timers"
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setSound(null, null)
            enableVibration(false)
        }
        manager.createNotificationChannel(alarmChannel)

        // Timer Channel
        val timerChannel = NotificationChannel(
            TIMER_CHANNEL_ID,
            "Timer Service Channel",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Channel for Running Timers"
            setShowBadge(false)
        }
        manager.createNotificationChannel(timerChannel)

        // Stopwatch Channel
        val stopwatchChannel = NotificationChannel(
            STOPWATCH_CHANNEL_ID,
            "Stopwatch Service Channel",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Channel for Running Stopwatch"
            setShowBadge(false)
        }
        manager.createNotificationChannel(stopwatchChannel)
    }
}
