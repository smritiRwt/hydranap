package com.example.alarm_system.features.stopwatch

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
class StopwatchViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val prefs = context.getSharedPreferences(AlarmConstants.PREFS_NAME, Context.MODE_PRIVATE)

    private val _timeMillis = MutableStateFlow(0L)
    val timeMillis = _timeMillis.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning = _isRunning.asStateFlow()

    private val _laps = MutableStateFlow<List<Long>>(emptyList())
    val laps = _laps.asStateFlow()

    private var stopwatchJob: Job? = null

    private val preferenceChangeListener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        if (key == AlarmConstants.PREF_STOPWATCH_IS_RUNNING) {
            val isRunning = sharedPreferences.getBoolean(key, false)
            if (!isRunning && _isRunning.value) {
                // If stopwatch was stopped from outside (notification), stop local state
                _isRunning.value = false
                stopwatchJob?.cancel()
                _timeMillis.value = sharedPreferences.getLong(AlarmConstants.PREF_STOPWATCH_PAUSE_TIME, _timeMillis.value)
            }
        }
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
        syncWithService()
    }

    private fun syncWithService() {
        val isRunning = prefs.getBoolean(AlarmConstants.PREF_STOPWATCH_IS_RUNNING, false)
        val startTime = prefs.getLong(AlarmConstants.PREF_STOPWATCH_START_TIME, 0L)
        val pauseTime = prefs.getLong(AlarmConstants.PREF_STOPWATCH_PAUSE_TIME, 0L)

        if (isRunning && startTime > 0) {
            _timeMillis.value = System.currentTimeMillis() - startTime
            start(isSync = true)
        } else {
            _timeMillis.value = pauseTime
        }
    }

    fun toggle() {
        if (_isRunning.value) {
            pause()
        } else {
            start()
        }
    }

    private fun start(isSync: Boolean = false) {
        _isRunning.value = true
        val startTimeMillis = if (isSync) {
            prefs.getLong(AlarmConstants.PREF_STOPWATCH_START_TIME, System.currentTimeMillis() - _timeMillis.value)
        } else {
            System.currentTimeMillis() - _timeMillis.value
        }
        
        if (!isSync) {
            // Start foreground service for stopwatch
            val serviceIntent = Intent(context, AlarmService::class.java).apply {
                action = AlarmConstants.ACTION_START_STOPWATCH
                putExtra(AlarmConstants.EXTRA_STOPWATCH_START_TIME, startTimeMillis)
            }
            context.startForegroundService(serviceIntent)
        }

        stopwatchJob = viewModelScope.launch(Dispatchers.Default) {
            while (isActive) {
                _timeMillis.value = System.currentTimeMillis() - startTimeMillis
                delay(10)
            }
        }
    }

    private fun pause() {
        _isRunning.value = false
        stopwatchJob?.cancel()
        
        // Stop the foreground stopwatch notification
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            action = AlarmConstants.ACTION_STOP_STOPWATCH
            putExtra(AlarmConstants.PREF_STOPWATCH_PAUSE_TIME, _timeMillis.value)
        }
        context.startService(serviceIntent)
    }

    fun reset() {
        pause()
        _timeMillis.value = 0L
        _laps.value = emptyList()
        prefs.edit {
            remove(AlarmConstants.PREF_STOPWATCH_START_TIME)
            remove(AlarmConstants.PREF_STOPWATCH_PAUSE_TIME)
            remove(AlarmConstants.PREF_STOPWATCH_IS_RUNNING)
        }
    }

    fun lap() {
        if (_timeMillis.value > 0) {
            _laps.value = listOf(_timeMillis.value) + _laps.value
        }
    }

    override fun onCleared() {
        super.onCleared()
        prefs.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
        stopwatchJob?.cancel()
    }
}
