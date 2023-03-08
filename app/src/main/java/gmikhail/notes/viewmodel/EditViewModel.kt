package gmikhail.notes.viewmodel

import androidx.lifecycle.*

private const val MAX_UNDO = 1024
internal const val NEW_NOTE_ID = -1

class EditViewModel(
    private var _noteId: Int
) : ViewModel() {
    val noteId: Int
        get() = _noteId
    private val history = mutableListOf<HistoryRecord>()
    private var index = 0
    private var inProgress = false

    private var _textState = MutableLiveData<HistoryRecord>()
    val textState: LiveData<HistoryRecord> = _textState

    private var _canUndo = MutableLiveData(false)
    val canUndo: LiveData<Boolean> = _canUndo

    private var _canRedo = MutableLiveData(false)
    val canRedo: LiveData<Boolean> = _canRedo

    fun updateNewNoteId(id: Int){
        if(_noteId == NEW_NOTE_ID)
            _noteId = id
    }

    fun addToHistory(record: HistoryRecord){
        if(inProgress) return
        if(history.isNotEmpty() && history.last().text == record.text) return
        history.add(record)
        if (history.lastIndex > MAX_UNDO) {
            history.removeAt(0)
        }
        index = history.lastIndex
        _textState.value = record
        updateCanUndoOrRedo()
    }

    fun undo(){
        if(_canUndo.value == true){
            inProgress = true
            index--
            _textState.value = history[index]
            inProgress = false
            updateCanUndoOrRedo()
        }
    }

    fun redo(){
        if(_canRedo.value == true){
            inProgress = true
            index++
            _textState.value = history[index]
            inProgress = false
            updateCanUndoOrRedo()
        }
    }

    private fun updateCanUndoOrRedo(){
        _canUndo.value = history.isNotEmpty() && index - 1 in history.indices
        _canRedo.value = history.isNotEmpty() && index + 1 in history.indices
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun provideFactory(noteId: Int?): ViewModelProvider.Factory {
            return object : ViewModelProvider.NewInstanceFactory() {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return EditViewModel(noteId ?: NEW_NOTE_ID) as T
                }
            }
        }
    }
}

data class HistoryRecord(val text: String, val cursorPosition: Int)