package gmikhail.notes.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import gmikhail.notes.R
import gmikhail.notes.data.db.Note
import gmikhail.notes.databinding.FragmentEditBinding
import gmikhail.notes.viewmodel.EditViewModel
import gmikhail.notes.viewmodel.HistoryRecord
import gmikhail.notes.viewmodel.MainFragmentViewModel

private const val KEY_NOTE_ID = "noteId"

class EditFragment : Fragment(R.layout.fragment_edit) {

    private var binding: FragmentEditBinding? = null
    private val viewModelMain: MainFragmentViewModel by activityViewModels{ MainFragmentViewModel.Factory }
    private val viewModelEdit: EditViewModel by viewModels()
    private var shouldRemove = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            viewModelEdit.setNoteId(it.getInt(KEY_NOTE_ID))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.materialToolbar?.let { toolbar ->
            val navController = findNavController()
            val appBarConfiguration = AppBarConfiguration(navController.graph)
            toolbar.setupWithNavController(navController, appBarConfiguration)
            toolbar.inflateMenu(R.menu.edit_menu)
            toolbar.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.action_undo -> {
                        viewModelEdit.undo()
                        true
                    }
                    R.id.action_redo -> {
                        viewModelEdit.redo()
                        true
                    }
                    R.id.action_share -> {
                        share()
                        true
                    }
                    R.id.action_delete -> {
                        shouldRemove = true
                        hideKeyboard()
                        findNavController().popBackStack()
                        true
                    }
                    else -> false
                }
            }
        }
        updateShareMenu()
        viewModelEdit.canUndo.observe(viewLifecycleOwner) {
            binding?.materialToolbar?.menu?.findItem(R.id.action_undo)?.isEnabled = it
        }
        viewModelEdit.canRedo.observe(viewLifecycleOwner) {
            binding?.materialToolbar?.menu?.findItem(R.id.action_redo)?.isEnabled = it
        }
        binding?.editTextTitle?.let { editTextTitle ->
            editTextTitle.setOnFocusChangeListener { _, hasFocus ->
                showUndoMenu(!hasFocus)
            }
            editTextTitle.doOnTextChanged { _, _, _, _ ->
                updateShareMenu()
            }
        }
        binding?.editTextBody?.let { editTextBody ->
            editTextBody.setOnFocusChangeListener { _, hasFocus ->
                showUndoMenu(hasFocus)
            }
            editTextBody.doOnTextChanged { text, _, _, _ ->
                val record = HistoryRecord(text.toString(), editTextBody.selectionEnd)
                viewModelEdit.addToHistory(record)
                updateShareMenu()
            }
            viewModelEdit.textState.observe(viewLifecycleOwner) {
                it?.let {
                    if(it.text != editTextBody.text.toString()) {
                        editTextBody.setText(it.text)
                        editTextBody.setSelection(it.cursorPosition)
                    }
                }
            }
        }
        val noteId = viewModelEdit.noteId
        if(savedInstanceState == null) {
            if (noteId == -1) {
                binding?.editTextBody?.requestFocus()
                showKeyboard(binding?.editTextBody)
                viewModelEdit.addToHistory(HistoryRecord("", 0))
            } else {
                viewModelMain.getNote(noteId)?.let {
                    binding?.editTextTitle?.setText(it.title)
                    viewModelEdit.addToHistory(HistoryRecord(it.text, it.text.length))
                }
            }
        }
    }

    private fun updateShareMenu(){
        val hasContent = binding?.editTextTitle?.text?.isNotBlank() == true ||
                binding?.editTextBody?.text?.isNotBlank() == true
        binding?.materialToolbar?.menu?.findItem(R.id.action_share)?.isEnabled = hasContent
    }

    private fun share(){
        val title = binding?.editTextTitle?.text.toString()
        val text = binding?.editTextBody?.text.toString()
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(shareIntent, null))
    }

    private fun showKeyboard(view: View?){
        val inputMethodManager =
            context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard(){
        val inputMethodManager =
            context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun showUndoMenu(visible: Boolean){
        binding?.materialToolbar?.menu?.findItem(R.id.action_undo)?.isVisible = visible
        binding?.materialToolbar?.menu?.findItem(R.id.action_redo)?.isVisible = visible
    }

    private fun addNewNote(){
        val newNote = Note(
            title = binding?.editTextTitle?.text.toString(),
            text = binding?.editTextBody?.text.toString(),
            lastModified = System.currentTimeMillis()
        )
        if(newNote.isNotBlank())
            viewModelMain.addNote(newNote) {
                viewModelEdit.setNoteId(it)
            }
    }

    private fun editNote(){
        val noteId = viewModelEdit.noteId
        val newTitle = binding?.editTextTitle?.text.toString()
        val newBody = binding?.editTextBody?.text.toString()
        val oldNote = viewModelMain.getNote(noteId)
        val isNoteChanged = oldNote?.title != newTitle || oldNote.text != newBody
        if(isNoteChanged){
            val editedNote = oldNote?.copy(
                title = newTitle,
                text = newBody,
                lastModified = System.currentTimeMillis()
            )
            editedNote?.let {
                if(it.isNotBlank())
                    viewModelMain.editNote(it)
                else
                    viewModelMain.deleteNote(noteId)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if(activity?.isChangingConfigurations == true) return
        val noteId = viewModelEdit.noteId
        if(noteId == -1) {
            if (!shouldRemove)
                addNewNote()
        }
        else {
            if (shouldRemove) viewModelMain.deleteNote(noteId)
            else editNote()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}