package gmikhail.notes.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Note(
    @PrimaryKey(autoGenerate = true)
    val uid: Int = 0,
    val title: String,
    val text: String,
    val lastModified: Long
) {
    fun isNotBlank(): Boolean {
        return title.isNotBlank() || text.isNotBlank()
    }
}