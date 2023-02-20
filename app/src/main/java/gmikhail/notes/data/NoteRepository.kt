package gmikhail.notes.data

class NoteRepository {

    fun getNotes(): List<Note>{
        // TODO load notes from db or fs
        return listOf(
            Note(
                title = "Hello world!",
                text = "Here note description!",
                lastModified = System.currentTimeMillis()
            ),
            Note(
                title = "Hello world 2!",
                text = "Here note description!",
                lastModified = 1676547991000
            ),
            Note(
                title = "Hello world 3!",
                text = "Here note description!",
                lastModified = 1676292391000
            )
        )
    }
}