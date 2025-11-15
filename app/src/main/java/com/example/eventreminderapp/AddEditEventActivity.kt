package com.example.eventreminderapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.eventreminderapp.databinding.ActivityAddEditEventBinding
import java.util.*

class AddEditEventActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddEditEventBinding
    private lateinit var db: DBHelper
    private var pickedMillis: Long = 0
    private var eventId: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DBHelper(this)

        eventId = intent.getLongExtra("eventId", 0L)
        if (eventId != 0L) loadEvent(eventId)

        binding.btnPickDateTime.setOnClickListener { pickDateTime() }
        binding.btnSave.setOnClickListener { saveEvent() }
    }

    private fun loadEvent(id: Long) {
        val ev = db.getEventById(id)
        ev?.let {
            binding.etTitle.setText(it.title)
            binding.etDesc.setText(it.description)
            binding.etNotifyBefore.setText(it.notifyBeforeMinutes.toString())
            pickedMillis = it.dateMillis
            binding.tvPicked.text = java.text.DateFormat.getDateTimeInstance().format(Date(pickedMillis))
        }
    }

    private fun pickDateTime() {
        val c = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
            TimePickerDialog(this, { _, hour, minute ->
                c.set(Calendar.YEAR, y)
                c.set(Calendar.MONTH, m)
                c.set(Calendar.DAY_OF_MONTH, d)
                c.set(Calendar.HOUR_OF_DAY, hour)
                c.set(Calendar.MINUTE, minute)
                c.set(Calendar.SECOND, 0)
                pickedMillis = c.timeInMillis
                binding.tvPicked.text = java.text.DateFormat.getDateTimeInstance().format(Date(pickedMillis))
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false).show()
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun saveEvent() {
        val title = binding.etTitle.text?.toString()?.trim() ?: ""
        if (title.isEmpty()) {
            Toast.makeText(this, "Title required", Toast.LENGTH_SHORT).show()
            return
        }
        if (pickedMillis == 0L) {
            Toast.makeText(this, "Pick date/time", Toast.LENGTH_SHORT).show()
            return
        }

        val desc = binding.etDesc.text?.toString()
        val notifyBefore = binding.etNotifyBefore.text?.toString()?.toIntOrNull() ?: 0

        if (eventId == 0L) {
            val ev = Event(title = title, description = desc, dateMillis = pickedMillis, notifyBeforeMinutes = notifyBefore)
            val id = db.insertEvent(ev)
            if (id > 0) {
                ev.id = id
                AlarmScheduler.scheduleAlarm(this, ev)
                finish()
            } else {
                Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show()
            }
        } else {
            val ev = Event(id = eventId, title = title, description = desc, dateMillis = pickedMillis, notifyBeforeMinutes = notifyBefore)
            db.updateEvent(ev)
            AlarmScheduler.cancelAlarm(this, ev.id)
            AlarmScheduler.scheduleAlarm(this, ev)
            finish()
        }
    }
}
