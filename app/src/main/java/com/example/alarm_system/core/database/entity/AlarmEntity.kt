package com.example.alarm_system.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "alarms",
    indices = [Index(value = ["isEnabled", "nextTriggerTime"])]
)
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val hour: Int,
    val minute: Int,
    val repeatType: Int, // 0: One-time, 1: Daily, 2: Weekly (Specific Days), 3: Custom
    val days: String, // Comma-separated integers (1-7) representing days of week
    val isEnabled: Boolean = true,
    val soundUri: String?,
    val isVibrationEnabled: Boolean = true,
    val snoozeMinutes: Int = 10,
    val createdTime: Long = System.currentTimeMillis(),
    val updatedTime: Long = System.currentTimeMillis(),
    val nextTriggerTime: Long // Epoch millis for when the alarm should fire next
)
