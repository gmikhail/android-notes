package gmikhail.notes.data

data class Note(
    val uid: Int,
    val title: String,
    val text: String,
    var lastModified: Long
)