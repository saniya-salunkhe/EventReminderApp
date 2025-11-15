package com.example.eventreminderapp

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import android.app.NotificationChannel
import android.app.NotificationManager

object NotificationUtil {
    private const val TAG = "NotificationUtil"
    private const val CHANNEL_ID = "event_reminder_channel"
    private const val CHANNEL_NAME = "Event Reminders"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for scheduled events"
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(ch)
        }
    }

    fun showNotification(context: Context, id: Int, title: String, text: String): Boolean {
        createChannel(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                Log.w(TAG, "POST_NOTIFICATIONS permission not granted; skipping notification")
                return false
            }
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        return try {
            NotificationManagerCompat.from(context).notify(id, builder.build())
            true
        } catch (se: SecurityException) {
            Log.e(TAG, "SecurityException while posting notification", se)
            false
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error posting notification", e)
            false
        }
    }
}
