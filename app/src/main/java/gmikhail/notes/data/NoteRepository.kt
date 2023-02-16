package gmikhail.notes.data

class NoteRepository {

    fun getNotes(): List<Note>{
        // TODO load notes from db or fs
        return listOf(
            Note(
                uid = 0,
                title = "Hello world!",
                text = "Here note description!",
                lastModified = System.currentTimeMillis()
            ),
            Note(
                uid = 1,
                title = "Hello world 2!",
                text = "Here note description!",
                lastModified = 1676547991000
            ),
            Note(
                uid = 2,
                title = "Hello world 3!",
                text = "Here note description!",
                lastModified = 1676292391000
            )
        )
    }
}