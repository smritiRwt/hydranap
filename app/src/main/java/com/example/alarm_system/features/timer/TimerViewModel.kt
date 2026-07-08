package com.example.alarm_system.features.timer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarm_system.core.alarm.data.service.AlarmService
import com.example.alarm_system.core.common.AlarmConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val prefs = context.getSharedPreferences(AlarmConstants.PREFS_NAME, Context.MODE_PRIVATE)

    private val _remainingTime = MutableStateFlow(0L)
    val remainingTime = _remainingTime.asStateFlow()

    private val _totalTime = MutableStateFlow(0L)
    val totalTime = _totalTime.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning = _isRunning.asStateFlow()

    private var timerJob: Job? = null
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private val preferenceChangeListener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        if (key == AlarmConstants.PREF_TIMER_END_TIME) {
            val endTime = sharedPreferences.getLong(key, 0L)
            if (endTime == 0L && _isRunning.value) {
                // If timer was cleared from outside (notification), stop local state
                _isRunning.value = false
                timerJob?.cancel()
                _remainingTime.value = _totalTime.value
            }
        }
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
        syncWithService()
    }

    private fun syncWithService() {
        val endTime = prefs.getLong(AlarmConstants.PREF_TIMER_END_TIME, 0L)
        val total = prefs.getLong(AlarmConstants.PREF_TIMER_TOTAL_TIME, 0L)
        
        if (endTime > System.currentTimeMillis()) {
            _totalTime.value = total
            _remainingTime.value = endTime - System.currentTimeMillis()
            start(isSync = true)
        } else if (total > 0) {
            _totalTime.value = total
            _remainingTime.value = total
        }
    }

    fun setTimer(hours: Int, minutes: Int, seconds: Int) {
        val totalMillis = (hours * 3600 + minutes * 60 + seconds) * 1000L
        _totalTime.value = totalMillis
        _remainingTime.value = totalMillis
    }

    fun toggle() {
        if (_isRunning.value) {
            pause()
        } else {
            start()
        }
    }

    private fun start(isSync: Boolean = false) {
        if (_remainingTime.value <= 0) return
        
        _isRunning.value = true
        
        if (!isSync) {
            // Start foreground service for the timer notification
            val serviceIntent = Intent(context, AlarmService::class.java).apply {
                action = AlarmConstants.ACTION_START_TIMER
                putExtra(AlarmConstants.EXTRA_TIMER_DURATION, _remainingTime.value)
            }
            context.startForegroundService(serviceIntent)
            
            // Schedule AlarmManager for when the timer expires
            val intent = Intent(context, AlarmService::class.java).apply {
                action = AlarmConstants.ACTION_TIMER_EXPIRED
            }
            val pendingIntent = PendingIntent.getService(
                context, 999, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + _remainingTime.value,
                pendingIntent
            )
        }

        timerJob = viewModelScope.launch(Dispatchers.Default) {
            val endTime = if (isSync) {
                prefs.getLong(AlarmConstants.PREF_TIMER_END_TIME, System.currentTimeMillis() + _remainingTime.value)
            } else {
                System.currentTimeMillis() + _remainingTime.value
            }

            while (isActive) {
                val now = System.currentTimeMillis()
                _remainingTime.value = (endTime - now).coerceAtLeast(0)
                if (_remainingTime.value <= 0L) {
                    _isRunning.value = false
                    break
                }
                delay(100)
            }
        }
    }

    private fun pause() {
        _isRunning.value = false
        timerJob?.cancel()
        cancelAlarm()
        
        // Stop the foreground timer notification
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            action = AlarmConstants.ACTION_STOP_TIMER
        }
        context.startService(serviceIntent)
    }

    private fun cancelAlarm() {
        val intent = Intent(context, AlarmService::class.java).apply {
            action = AlarmConstants.ACTION_TIMER_EXPIRED
        }
        val pendingIntent = PendingIntent.getService(
            context, 999, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun reset() {
        pause()
        _remainingTime.value = _totalTime.value
        prefs.edit { remove(AlarmConstants.PREF_TIMER_END_TIME) }
    }

    fun clear() {
        pause()
        _remainingTime.value = 0
        _totalTime.value = 0
        prefs.edit {
            remove(AlarmConstants.PREF_TIMER_END_TIME)
            remove(AlarmConstants.PREF_TIMER_TOTAL_TIME)
        }
    }

    override fun onCleared() {
        super.onCleared()
        prefs.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
        timerJob?.cancel()
    }
}
