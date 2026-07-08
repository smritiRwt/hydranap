# Hilt
-keep class com.example.alarm_system.** { *; }
-keep interface com.example.alarm_system.** { *; }

# Keep Dagger/Hilt classes
-keep class dagger.hilt.** { *; }
-keep class com.google.dagger.** { *; }

# Room
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep class * extends androidx.room.Entity
-keep class * extends androidx.room.Dao

# Keep Services and Receivers
-keep class * extends android.app.Service
-keep class * extends android.content.BroadcastReceiver

# Keep lifecycle classes
-keep class androidx.lifecycle.** { *; }

# Preserve Intent extras and action names
-keepclassmembernames class com.example.alarm_system.core.common.AlarmConstants {
    public static final java.lang.String *;
}
