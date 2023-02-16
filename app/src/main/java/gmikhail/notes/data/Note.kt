package gmikhail.notes.data

data class Note(
    val uid: Int,
    val title: String,
    val data: String,
    var lastModified: Long
)