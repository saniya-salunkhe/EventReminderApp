package com.example.eventreminderapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.eventreminderapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), EventAdapter.EventClickListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var db: DBHelper
    private lateinit var adapter: EventAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DBHelper(this)

        adapter = EventAdapter(mutableListOf(), this)
        binding.rvEvents.layoutManager = LinearLayoutManager(this)
        binding.rvEvents.adapter = adapter

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                loadEventsSafely()
            }
        } else {
            loadEventsSafely()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                "event_channel_id",
                "Event Reminders",
                android.app.NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(android.app.NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddEditEventActivity::class.java))
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            loadEventsSafely()
        }

    override fun onResume() {
        super.onResume()
        loadEventsSafely()
    }

    private fun loadEventsSafely() {
        try {
            val events = db.getAllEvents()
            adapter.submitList(events.toMutableList())
        } catch (e: Exception) {
            Log.e("MainActivity", "Error loading events", e)
        }
    }

    override fun onEventClick(event: Event) {
        val i = Intent(this, AddEditEventActivity::class.java)
        i.putExtra("eventId", event.id)
        startActivity(i)
    }

    override fun onEventLongClick(event: Event) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delete Event")
            .setMessage("Delete event \"${event.title}\"?")
            .setPositiveButton("Delete") { _, _ ->
                db.deleteEvent(event.id)
                AlarmScheduler.cancelAlarm(this, event.id)
                loadEventsSafely()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
