package com.example.alarm_system.features.alarm_edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarm_system.core.alarm.domain.model.Alarm
import com.example.alarm_system.core.alarm.domain.model.RepeatType
import com.example.alarm_system.core.alarm.domain.repository.AlarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class AlarmEditViewModel @Inject constructor(
    private val repository: AlarmRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlarmEditUiState())
    val uiState = _uiState.asStateFlow()

    fun loadAlarm(id: Int) {
        if (id == -1) return
        viewModelScope.launch {
            repository.getAlarmById(id)?.let { alarm ->
                _uiState.value = AlarmEditUiState(
                    id = alarm.id,
                    title = alarm.title,
                    description = alarm.description,
                    time = alarm.time,
                    repeatDays = alarm.repeatDays,
                    isVibrationEnabled = alarm.isVibrationEnabled,
                    snoozeMinutes = alarm.snoozeMinutes,
                    soundUri = alarm.soundUri
                )
            }
        }
    }

    fun updateSound(uri: String?) {
        _uiState.value = _uiState.value.copy(soundUri = uri)
    }

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun updateVibration(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isVibrationEnabled = enabled)
    }

    fun updateSnooze(minutes: Int) {
        _uiState.value = _uiState.value.copy(snoozeMinutes = minutes)
    }

    fun updateTime(time: LocalTime) {
        _uiState.value = _uiState.value.copy(time = time)
    }

    fun toggleDay(day: DayOfWeek) {
        val currentDays = _uiState.value.repeatDays.toMutableSet()
        if (currentDays.contains(day)) {
            currentDays.remove(day)
        } else {
            currentDays.add(day)
        }
        _uiState.value = _uiState.value.copy(repeatDays = currentDays)
    }

    fun saveAlarm(onSuccess: () -> Unit) {
        val state = _uiState.value
        val alarm = Alarm(
            id = state.id,
            title = state.title,
            description = state.description,
            time = state.time,
            repeatType = if (state.repeatDays.isEmpty()) RepeatType.ONCE else RepeatType.CUSTOM,
            repeatDays = state.repeatDays,
            isEnabled = true,
            soundUri = state.soundUri,
            isVibrationEnabled = state.isVibrationEnabled,
            snoozeMinutes = state.snoozeMinutes,
            nextTriggerTime = 0L
        )

        viewModelScope.launch {
            if (alarm.id == 0) {
                repository.insertAlarm(alarm)
            } else {
                repository.updateAlarm(alarm)
            }
            onSuccess()
        }
    }
}

data class AlarmEditUiState(
    val id: Int = 0,
    val title: String = "",
    val description: String = "",
    val time: LocalTime = LocalTime.now(),
    val repeatDays: Set<DayOfWeek> = emptySet(),
    val isVibrationEnabled: Boolean = true,
    val snoozeMinutes: Int = 5,
    val soundUri: String? = null
)
