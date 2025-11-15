package com.example.eventreminderapp

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        const val DB_NAME = "events.db"
        const val DB_VERSION = 1

        const val TABLE_EVENTS = "events"
        const val COL_ID = "id"
        const val COL_TITLE = "title"
        const val COL_DESC = "description"
        const val COL_DATE = "dateMillis"
        const val COL_NOTIFY_BEFORE = "notifyBefore"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val create = """
            CREATE TABLE $TABLE_EVENTS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TITLE TEXT NOT NULL,
                $COL_DESC TEXT,
                $COL_DATE INTEGER NOT NULL,
                $COL_NOTIFY_BEFORE INTEGER DEFAULT 0
            );
        """.trimIndent()
        db.execSQL(create)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldV: Int, newV: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EVENTS")
        onCreate(db)
    }

    fun insertEvent(event: Event): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_TITLE, event.title)
            put(COL_DESC, event.description)
            put(COL_DATE, event.dateMillis)
            put(COL_NOTIFY_BEFORE, event.notifyBeforeMinutes)
        }
        return db.insert(TABLE_EVENTS, null, values)
    }

    fun updateEvent(event: Event): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_TITLE, event.title)
            put(COL_DESC, event.description)
            put(COL_DATE, event.dateMillis)
            put(COL_NOTIFY_BEFORE, event.notifyBeforeMinutes)
        }
        return db.update(TABLE_EVENTS, values, "$COL_ID=?", arrayOf(event.id.toString()))
    }

    fun deleteEvent(eventId: Long) {
        writableDatabase.delete(TABLE_EVENTS, "$COL_ID=?", arrayOf(eventId.toString()))
    }

    fun getAllEvents(): List<Event> {
        val list = mutableListOf<Event>()
        val db = readableDatabase
        val cursor: Cursor = db.query(TABLE_EVENTS, null, null, null, null, null, "$COL_DATE ASC")
        cursor.use {
            while (it.moveToNext()) {
                val ev = Event(
                    id = it.getLong(it.getColumnIndexOrThrow(COL_ID)),
                    title = it.getString(it.getColumnIndexOrThrow(COL_TITLE)),
                    description = it.getString(it.getColumnIndexOrThrow(COL_DESC)),
                    dateMillis = it.getLong(it.getColumnIndexOrThrow(COL_DATE)),
                    notifyBeforeMinutes = it.getInt(it.getColumnIndexOrThrow(COL_NOTIFY_BEFORE))
                )
                list.add(ev)
            }
        }
        return list
    }

    fun getEventById(id: Long): Event? {
        val db = readableDatabase
        val c = db.query(TABLE_EVENTS, null, "$COL_ID=?", arrayOf(id.toString()), null, null, null)
        c.use {
            if (it.moveToFirst()) {
                return Event(
                    id = it.getLong(it.getColumnIndexOrThrow(COL_ID)),
                    title = it.getString(it.getColumnIndexOrThrow(COL_TITLE)),
                    description = it.getString(it.getColumnIndexOrThrow(COL_DESC)),
                    dateMillis = it.getLong(it.getColumnIndexOrThrow(COL_DATE)),
                    notifyBeforeMinutes = it.getInt(it.getColumnIndexOrThrow(COL_NOTIFY_BEFORE))
                )
            }
        }
        return null
    }
}
