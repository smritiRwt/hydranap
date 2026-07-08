package com.example.alarm_system.core.common

object AlarmConstants {
    const val ACTION_FIRE_ALARM = "com.example.alarm_system.ACTION_FIRE_ALARM"
    const val ACTION_SNOOZE_ALARM = "com.example.alarm_system.ACTION_SNOOZE_ALARM"
    const val ACTION_DISMISS_ALARM = "com.example.alarm_system.ACTION_DISMISS_ALARM"
    const val ACTION_TIMER_EXPIRED = "com.example.alarm_system.ACTION_TIMER_EXPIRED"
    const val ACTION_START_TIMER = "com.example.alarm_system.ACTION_START_TIMER"
    const val ACTION_STOP_TIMER = "com.example.alarm_system.ACTION_STOP_TIMER"
    const val ACTION_START_STOPWATCH = "com.example.alarm_system.ACTION_START_STOPWATCH"
    const val ACTION_STOP_STOPWATCH = "com.example.alarm_system.ACTION_STOP_STOPWATCH"
    
    const val EXTRA_ALARM_ID = "extra_alarm_id"
    const val EXTRA_TIMER_DURATION = "extra_timer_duration"
    const val EXTRA_STOPWATCH_START_TIME = "extra_stopwatch_start_time"
    const val EXTRA_ALARM_TITLE = "extra_alarm_title"
    const val EXTRA_ALARM_SOUND = "extra_alarm_sound"
    const val EXTRA_ALARM_VIBRATE = "extra_alarm_vibrate"

    // Preferences Keys
    const val PREFS_NAME = "alarm_system_prefs"
    const val PREF_ONBOARDING_COMPLETED = "pref_onboarding_completed"
    const val PREF_WATER_INTERVAL = "pref_water_interval"
    const val PREF_SLEEP_START_TIME = "pref_sleep_start_time"
    const val PREF_SLEEP_END_TIME = "pref_sleep_end_time"
    const val PREF_TIMER_END_TIME = "pref_timer_end_time"
    const val PREF_TIMER_TOTAL_TIME = "pref_timer_total_time"
    const val PREF_STOPWATCH_START_TIME = "pref_stopwatch_start_time"
    const val PREF_STOPWATCH_PAUSE_TIME = "pref_stopwatch_pause_time"
    const val PREF_STOPWATCH_IS_RUNNING = "pref_stopwatch_is_running"
}
