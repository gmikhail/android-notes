package gmikhail.notes.data.db

import android.content.Context
import androidx.room.Room

private const val DB_NAME = "database"

class DatabaseSource(applicationContext: Context) {
    val database = Room.databaseBuilder(
        applicationContext,
        AppDatabase::class.java, DB_NAME
    ).build()
}