package com.example.eventreminderapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

object AlarmScheduler {
    private const val TAG = "AlarmScheduler"

    fun scheduleAlarm(context: Context, event: Event) {
        val alarmTime = event.dateMillis - event.notifyBeforeMinutes * 60_000L
        if (alarmTime <= System.currentTimeMillis()) {
            Log.w(TAG, "Alarm time is in the past — skipping schedule for eventId=${event.id}")
            return
        }

        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("eventId", event.id)
            putExtra("title", event.title)
            putExtra("description", event.description)
        }

        val requestCode = event.id.toInt()
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pi = PendingIntent.getBroadcast(context, requestCode, intent, flags)

        val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                am.canScheduleExactAlarms()
            } catch (e: Exception) {
                Log.w(TAG, "canScheduleExactAlarms() failed — assuming false", e)
                false
            }
        } else {
            true
        }

        if (!canExact) {
            Log.w(TAG, "Exact alarms not permitted. Scheduling inexact alarm for eventId=${event.id}")
            try {
                am.set(AlarmManager.RTC_WAKEUP, alarmTime, pi)
            } catch (se: SecurityException) {
                Log.e(TAG, "SecurityException scheduling inexact alarm", se)
            } catch (e: Exception) {
                Log.e(TAG, "Failed scheduling inexact alarm", e)
            }
            return
        }

        try {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, pi)
            Log.d(TAG, "Scheduled exact alarm for eventId=${event.id} at $alarmTime")
        } catch (se: SecurityException) {
            Log.e(TAG, "SecurityException scheduling exact alarm — falling back to inexact", se)
            try {
                am.set(AlarmManager.RTC_WAKEUP, alarmTime, pi)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to schedule fallback inexact alarm", e)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error scheduling alarm", e)
        }
    }

    fun cancelAlarm(context: Context, eventId: Long) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val requestCode = eventId.toInt()
        val flags = PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        val pi = PendingIntent.getBroadcast(context, requestCode, intent, flags)
        if (pi != null) {
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am.cancel(pi)
            pi.cancel()
            Log.d(TAG, "Cancelled alarm for eventId=$eventId")
        } else {
            Log.d(TAG, "No existing PendingIntent found for eventId=$eventId")
        }
    }
}
