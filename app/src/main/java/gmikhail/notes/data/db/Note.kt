package gmikhail.notes.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Note(
    val title: String,
    val text: String,
    val lastModified: Long
) {
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0

    fun isNotBlank(): Boolean {
        return title.isNotBlank() || text.isNotBlank()
    }
}