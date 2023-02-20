package gmikhail.notes.data

data class Note(
    var title: String,
    var text: String,
    var lastModified: Long
) {
    fun isNotBlank(): Boolean {
        return title.isNotBlank() || text.isNotBlank()
    }
}