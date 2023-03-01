package gmikhail.notes.data

import gmikhail.notes.data.db.DatabaseSource
import gmikhail.notes.data.db.Note

class NoteRepository(
    private val databaseSource: DatabaseSource
) {

    suspend fun getAll(): List<Note>{
        return databaseSource.database.noteDao().getAll()
    }

    suspend fun addNote(note: Note): Long{
        return databaseSource.database.noteDao().insert(note)
    }

    suspend fun updateNote(note: Note){
        databaseSource.database.noteDao().update(note)
    }

    suspend fun deleteNote(note: Note){
        databaseSource.database.noteDao().delete(note)
    }
}