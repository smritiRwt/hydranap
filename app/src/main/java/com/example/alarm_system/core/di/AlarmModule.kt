package com.example.alarm_system.core.di

import com.example.alarm_system.core.alarm.data.scheduler.AlarmSchedulerImpl
import com.example.alarm_system.core.alarm.domain.repository.AlarmScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AlarmModule {

    @Binds
    @Singleton
    abstract fun bindAlarmScheduler(
        alarmSchedulerImpl: AlarmSchedulerImpl
    ): AlarmScheduler
}
