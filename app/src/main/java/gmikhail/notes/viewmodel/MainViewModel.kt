package gmikhail.notes.viewmodel

import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import gmikhail.notes.data.NoteRepository
import gmikhail.notes.data.Note

class MainFragmentViewModel(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _notes = MutableLiveData<List<Note>>()
    val notes: LiveData<List<Note>> = _notes

    fun fetchNotes(){
        _notes.value = noteRepository.getNotes()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MainFragmentViewModel(NoteRepository())
            }
        }
    }
}