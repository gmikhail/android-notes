package gmikhail.notes.viewmodel

import androidx.lifecycle.*
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import gmikhail.notes.Constants
import gmikhail.notes.data.db.Note
import gmikhail.notes.data.NoteRepository
import gmikhail.notes.data.PreferencesRepository
import gmikhail.notes.data.db.DatabaseSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainFragmentViewModel(
    private val noteRepository: NoteRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _notes = MutableLiveData<MutableList<Note>>(mutableListOf())
    val notes: LiveData<List<Note>> = _notes.map { it.toList() }

    private val _darkMode = MutableLiveData<Boolean>()
    val darkMode: LiveData<Boolean> = _darkMode

    private val viewModelScope = CoroutineScope(Dispatchers.IO)

    init {
        fetchDarkMode()
    }

    private fun fetchDarkMode() {
        _darkMode.value  = preferencesRepository.loadBool(Constants.PREF_KEY_DARK_MODE, false)
    }

    fun switchDarkMode(){
        _darkMode.value?.let {
            val newValue = !it
            _darkMode.value = newValue
            preferencesRepository.saveBool(Constants.PREF_KEY_DARK_MODE, newValue)
        }
    }

    fun getAllNotes(){
        viewModelScope.launch {
            _notes.postValue(noteRepository.getAll().toMutableList())
        }
    }

    fun editNote(index: Int, newNote: Note){
        _notes.value?.let {
            if(index in it.indices) {
                it[index] = newNote
                notifyNotesChanged()
                viewModelScope.launch {
                    noteRepository.updateNote(newNote)
                }
            }
        }
    }

    fun addNote(newNote: Note){
        _notes.value?.add(newNote)
        notifyNotesChanged()
        viewModelScope.launch {
            noteRepository.addNote(newNote)
        }
    }

    fun deleteNote(index: Int){
        _notes.value?.let {
            if(index in it.indices) {
                val note = it[index]
                viewModelScope.launch {
                    noteRepository.deleteNote(note)
                }
                _notes.value?.removeAt(index)
                notifyNotesChanged()
            }
        }
    }

    private fun notifyNotesChanged(){
        _notes.value = _notes.value
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val application = checkNotNull(extras[APPLICATION_KEY])
                return MainFragmentViewModel(
                    NoteRepository(DatabaseSource(application)),
                    PreferencesRepository(application)
                ) as T
            }
        }
    }
}