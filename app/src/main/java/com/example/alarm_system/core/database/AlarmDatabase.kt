package com.example.alarm_system.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.alarm_system.core.database.dao.AlarmDao
import com.example.alarm_system.core.database.entity.AlarmEntity

@Database(
    entities = [AlarmEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AlarmDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao

    companion object {
        const val DATABASE_NAME = "alarm_db"
    }
}
