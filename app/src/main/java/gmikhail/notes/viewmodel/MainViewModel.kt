package gmikhail.notes.viewmodel

import androidx.lifecycle.*
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import gmikhail.notes.data.db.Note
import gmikhail.notes.data.NoteRepository
import gmikhail.notes.data.PreferencesRepository
import gmikhail.notes.data.db.DatabaseSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val DARK_MODE_KEY = "dark_mode"
private const val DARK_MODE_DEFAULT_VALUE = false
private const val DISPLAY_MODE_LIST_KEY = "display_mode_list"
private const val DISPLAY_MODE_DEFAULT_VALUE = true

class MainViewModel(
    private val noteRepository: NoteRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {
    private val _notes = MutableLiveData<MutableList<Note>>()
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
                    notes[index].let { note ->
                        notesToRemove.add(note)
                    }
                }
                notes.removeAll(notesToRemove)
                notifyNotesChanged()
                viewModelScope.launch {
                    for (note in notesToRemove)
                        noteRepository.deleteNote(note)
                }
            }
            clearSelection()
        }
    }

    private fun notifySelectionChanged(){
        _selection.value = _selection.value
    }

    private fun loadDarkMode() {
        _darkMode.value  = preferencesRepository.loadBool(DARK_MODE_KEY, DARK_MODE_DEFAULT_VALUE)
    }

    private fun loadDisplayMode(){
        _displayModeList.value = preferencesRepository.loadBool(
            DISPLAY_MODE_LIST_KEY, DISPLAY_MODE_DEFAULT_VALUE
        )
    }

    fun switchDarkMode(){
        _darkMode.value?.let {
            val newValue = !it
            _darkMode.value = newValue
            preferencesRepository.saveBool(DARK_MODE_KEY, newValue)
        }
    }

    fun switchDisplayMode(){
        _displayModeList.value?.let {
            val newValue = !it
            _displayModeList.value = newValue
            preferencesRepository.saveBool(DISPLAY_MODE_LIST_KEY, newValue)
        }
    }

    fun getNote(id: Int) = _notes.value?.find { it.uid == id }

    private fun loadNotes(){
        viewModelScope.launch {
            val list = noteRepository.getAll().toMutableList()
            list.sortByDescending { it.lastModified }
            _notes.postValue(list)
        }
    }

    fun editNote(newNote: Note){
        _notes.value?.let { notes ->
            notes.find { it.uid == newNote.uid }?.let { oldNote ->
                notes.remove(oldNote)
                notes.add(0, newNote)
                notifyNotesChanged()
                viewModelScope.launch {
                    noteRepository.updateNote(newNote)
                }
            }
        }
    }

    fun addNote(newNote: Note, callback: (id: Int) -> Unit){
        viewModelScope.launch {
            val id = noteRepository.addNote(newNote).toInt()
            val newNoteWithId = newNote.copy(uid = id)
            _notes.value?.let {
                _notes.postValue(it.apply { add(0, newNoteWithId) })
            }
            callback.invoke(id)
        }
    }

    fun deleteNote(id: Int){
        _notes.value?.find { it.uid == id }?.let {
            _notes.value?.remove(it)
            viewModelScope.launch {
                noteRepository.deleteNote(it)
            }
            notifyNotesChanged()
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
                return MainViewModel(
                    NoteRepository(DatabaseSource(application)),
                    PreferencesRepository(application)
                ) as T
            }
        }
    }
}

data class NoteState(val note: Note, val isSelected: Boolean)