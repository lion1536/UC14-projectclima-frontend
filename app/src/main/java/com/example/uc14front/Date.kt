package com.example.uc14front

import java.text.SimpleDateFormat
import java.util.Locale

class Date {
    fun getCurrentDate(): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return formatter.format(Date())
    }
}