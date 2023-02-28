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

    private val _selection = MutableLiveData<MutableList<Int>>(mutableListOf())
    val selection: LiveData<List<Int>> = _selection.map { it.toList() }

    val notesState: MediatorLiveData<List<NoteState>> = MediatorLiveData<List<NoteState>>().apply {
        addSource(notes) { notes ->
            value = mutableListOf<NoteState>().apply {
                notes.forEachIndexed { index, note ->
                    val isSelected = selection.value?.contains(index) == true
                    add(NoteState(note, isSelected))
                }
            }
        }
        addSource(selection) { selection ->
            value = mutableListOf<NoteState>().apply {
                notes.value?.forEachIndexed { index, note ->
                    val isSelected = index in selection
                    add(NoteState(note, isSelected))
                }
            }
        }
    }

    private val _darkMode = MutableLiveData<Boolean>()
    val darkMode: LiveData<Boolean> = _darkMode

    private val _displayModeList = MutableLiveData<Boolean>()
    val displayModeList: LiveData<Boolean> = _displayModeList

    private val viewModelScope = CoroutineScope(Dispatchers.IO)

    init {
        loadNotes()
        loadDarkMode()
        loadDisplayMode()
    }

    fun select(index: Int){
        if(_notes.value?.indices?.contains(index) == false) return
        _selection.value?.let {
            if(it.contains(index))
                it.remove(index)
            else it.add(index)
            notifySelectionChanged()
        }
    }

    fun clearSelection(){
        _selection.value?.let {
            it.clear()
            notifySelectionChanged()
        }
    }

    fun deleteSelectedNotes(){
        _selection.value?.let { selection ->
            _notes.value?.let { notes ->
                val notesToRemove = mutableListOf<Note>()
                for (index in selection) {
                    getNote(index)?.let { note ->
                        notesToRemove.add(note)
                    }
                }
                notes.removeAll(notesToRemove)
                notifyNotesChanged()
                viewModelScope.launch {
                    for (n in notesToRemove)
                        noteRepository.deleteNote(n)
                }
            }
            clearSelection()
        }
    }

    private fun notifySelectionChanged(){
        _selection.value = _selection.value
    }

    private fun loadDarkMode() {
        _darkMode.value  = preferencesRepository.loadBool(Constants.PREF_KEY_DARK_MODE, false)
    }

    private fun loadDisplayMode(){
        _displayModeList.value = preferencesRepository.loadBool(
            Constants.PREF_KEY_DISPLAY_MODE_LIST, true
        )
    }

    fun switchDarkMode(){
        _darkMode.value?.let {
            val newValue = !it
            _darkMode.value = newValue
            preferencesRepository.saveBool(Constants.PREF_KEY_DARK_MODE, newValue)
        }
    }

    fun switchDisplayMode(){
        _displayModeList.value?.let {
            val newValue = !it
            _displayModeList.value = newValue
            preferencesRepository.saveBool(Constants.PREF_KEY_DISPLAY_MODE_LIST, newValue)
        }
    }

    fun getNote(index: Int) = _notes.value?.get(index)

    private fun loadNotes(){
        viewModelScope.launch {
            val list = noteRepository.getAll().toMutableList()
            list.sortByDescending { it.lastModified }
            _notes.postValue(list)
        }
    }

    fun editNote(index: Int, newNote: Note){
        _notes.value?.let {
            if(index in it.indices) {
                it.removeAt(index)
                it.add(0, newNote)
                notifyNotesChanged()
                viewModelScope.launch {
                    noteRepository.updateNote(newNote)
                }
            }
        }
    }

    fun addNote(newNote: Note){
        _notes.value?.add(0, newNote)
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

data class NoteState(val note: Note, val isSelected: Boolean)