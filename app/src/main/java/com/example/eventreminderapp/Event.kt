package com.example.eventreminderapp

data class Event(
    var id: Long = 0L,
    var title: String,
    var description: String? = null,
    var dateMillis: Long,
    var notifyBeforeMinutes: Int = 0
)
