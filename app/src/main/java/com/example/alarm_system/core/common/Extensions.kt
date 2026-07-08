package com.example.alarm_system.core.common

import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

fun Set<DayOfWeek>.toAbbreviatedDays(): String {
    if (isEmpty()) return "Once"
    if (size == 7) return "Daily"
    
    val sortedDays = this.sorted()
    return sortedDays.joinToString(", ") { 
        it.getDisplayName(TextStyle.SHORT, Locale.getDefault()) 
    }
}
