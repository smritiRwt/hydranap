package com.example.alarm_system.features.onboarding

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarm_system.core.alarm.domain.model.Alarm
import com.example.alarm_system.core.alarm.domain.model.RepeatType
import com.example.alarm_system.core.alarm.domain.repository.AlarmRepository
import com.example.alarm_system.core.common.AlarmConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: AlarmRepository
) : ViewModel() {

    private val prefs = context.getSharedPreferences(AlarmConstants.PREFS_NAME, Context.MODE_PRIVATE)

    var waterIntervalMinutes by mutableStateOf(20)
    var sleepStartTime by mutableStateOf(LocalTime.of(22, 0))
    var sleepEndTime by mutableStateOf(LocalTime.of(7, 0))

    fun completeOnboarding(onComplete: () -> Unit) {
        viewModelScope.launch {
            // Save preferences
            prefs.edit {
                putInt(AlarmConstants.PREF_WATER_INTERVAL, waterIntervalMinutes)
                putString(AlarmConstants.PREF_SLEEP_START_TIME, sleepStartTime.toString())
                putString(AlarmConstants.PREF_SLEEP_END_TIME, sleepEndTime.toString())
                putBoolean(AlarmConstants.PREF_ONBOARDING_COMPLETED, true)
            }

            // Schedule Water Alarms (Simplified: Schedule multiple throughout the day or use a different mechanism)
            // For now, let's just schedule a few or the first one. 
            // Better yet, create special alarms in the database.
            
            val waterSoundUri = "android.resource://${context.packageName}/raw/drink_water"
            val sleepSoundUri = "android.resource://${context.packageName}/raw/bedtime"

            // Schedule Water Alarm (the first one starting now + interval)
            val firstWaterTime = LocalTime.now().plusMinutes(waterIntervalMinutes.toLong())
            repository.insertAlarm(
                Alarm(
                    title = "Water Reminder",
                    description = "Time to drink water!",
                    time = firstWaterTime,
                    repeatType = RepeatType.CUSTOM,
                    repeatDays = emptySet(),
                    isEnabled = true,
                    soundUri = waterSoundUri,
                    isVibrationEnabled = true,
                    snoozeMinutes = 5,
                    nextTriggerTime = 0
                )
            )

            // Schedule Sleep Alarms
            repository.insertAlarm(
                Alarm(
                    title = "Sleep Time",
                    description = "Time to go to bed",
                    time = sleepStartTime,
                    repeatType = RepeatType.CUSTOM,
                    repeatDays = DayOfWeek.values().toSet(),
                    isEnabled = true,
                    soundUri = sleepSoundUri,
                    isVibrationEnabled = true,
                    snoozeMinutes = 5,
                    nextTriggerTime = 0
                )
            )

            repository.insertAlarm(
                Alarm(
                    title = "Wake Up",
                    description = "Good morning!",
                    time = sleepEndTime,
                    repeatType = RepeatType.CUSTOM,
                    repeatDays = DayOfWeek.values().toSet(),
                    isEnabled = true,
                    soundUri = sleepSoundUri,
                    isVibrationEnabled = true,
                    snoozeMinutes = 5,
                    nextTriggerTime = 0
                )
            )

            onComplete()
        }
    }
}
