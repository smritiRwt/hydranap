package com.example.alarm_system.features.alarm_ringing

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarm_system.core.alarm.domain.model.Alarm
import com.example.alarm_system.core.alarm.domain.repository.AlarmRepository
import com.example.alarm_system.core.common.AlarmConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class AlarmRingingViewModel @Inject constructor(
    private val repository: AlarmRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _alarm = MutableStateFlow<Alarm?>(null)
    val alarm = _alarm.asStateFlow()

    fun loadAlarm(id: Int) {
        viewModelScope.launch {
            _alarm.value = repository.getAlarmById(id)
        }
    }

    fun snooze() {
        val currentAlarm = _alarm.value ?: return
        viewModelScope.launch {
            try {
                if (currentAlarm.title == "Water Reminder") {
                    val prefs = context.getSharedPreferences(AlarmConstants.PREFS_NAME, Context.MODE_PRIVATE)
                    val interval = prefs.getInt(AlarmConstants.PREF_WATER_INTERVAL, 20)
                    val nextWaterTime = LocalTime.now().plusMinutes(interval.toLong())
                    repository.updateAlarm(currentAlarm.copy(time = nextWaterTime, isEnabled = true))
                } else {
                    repository.snoozeAlarm(currentAlarm)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun dismiss() {
        val currentAlarm = _alarm.value ?: return
        viewModelScope.launch {
            try {
                if (currentAlarm.title == "Water Reminder") {
                    val prefs = context.getSharedPreferences(AlarmConstants.PREFS_NAME, Context.MODE_PRIVATE)
                    val interval = prefs.getInt(AlarmConstants.PREF_WATER_INTERVAL, 20)
                    
                    val nextWaterTime = LocalTime.now().plusMinutes(interval.toLong())
                    repository.updateAlarm(
                        currentAlarm.copy(
                            time = nextWaterTime,
                            isEnabled = true
                        )
                    )
                } else {
                    repository.updateAlarm(currentAlarm.copy(isEnabled = currentAlarm.repeatDays.isNotEmpty()))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
