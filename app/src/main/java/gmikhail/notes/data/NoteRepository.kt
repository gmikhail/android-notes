package gmikhail.notes.data

class NoteRepository {

    fun getNotes(): List<Note>{
        // TODO load notes from db or fs
        return listOf(
            Note(
                uid = 0,
                title = "Hello world!",
                data = "Here note description!",
                lastModified = System.currentTimeMillis()
            ),
            Note(
                uid = 1,
                title = "Hello world 2!",
                data = "Here note description!",
                lastModified = System.currentTimeMillis()
            ),
            Note(
                uid = 2,
                title = "Hello world 3!",
                data = "Here note description!",
                lastModified = System.currentTimeMillis()
            )
        )
    }
}