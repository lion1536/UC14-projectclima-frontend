package com.example.uc14front

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class Hour {
    fun getCurrentHour(): String {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
        return current.format(formatter)
    }
}
