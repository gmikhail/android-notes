package gmikhail.notes.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import gmikhail.notes.R
import gmikhail.notes.data.db.Note
import gmikhail.notes.databinding.FragmentEditBinding
import gmikhail.notes.viewmodel.EditViewModel
import gmikhail.notes.viewmodel.HistoryRecord
import gmikhail.notes.viewmodel.MainFragmentViewModel

private const val KEY_NOTE_INDEX = "noteIndex"

class EditFragment : Fragment(R.layout.fragment_edit) {

    private var noteIndex: Int = -1
    private var binding: FragmentEditBinding? = null
    private val viewModelMain: MainFragmentViewModel by activityViewModels{ MainFragmentViewModel.Factory }
    private val viewModelEdit: EditViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            noteIndex = it.getInt(KEY_NOTE_INDEX)
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
        binding?.topAppBar?.let { toolbar ->
            toolbar.navigationIcon =
                ContextCompat.getDrawable(view.context, R.drawable.ic_arrow_back)
            toolbar.setNavigationOnClickListener {
                parentFragmentManager.popBackStack()
            }
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
                    R.id.action_done -> {
                        parentFragmentManager.popBackStack()
                        true
                    }
                    else -> false
                }

            }
        }
        viewModelEdit.canUndo.observe(viewLifecycleOwner) {
            binding?.topAppBar?.menu?.findItem(R.id.action_undo)?.isEnabled = it
        }
        viewModelEdit.canRedo.observe(viewLifecycleOwner) {
            binding?.topAppBar?.menu?.findItem(R.id.action_redo)?.isEnabled = it
        }
        binding?.editTextTitle?.setOnFocusChangeListener { _, hasFocus ->
            showUndoMenu(!hasFocus)
        }
        binding?.editTextBody?.setOnFocusChangeListener { _, hasFocus ->
            showUndoMenu(hasFocus)
        }
        binding?.editTextBody?.let { editTextBody ->
            editTextBody.doOnTextChanged { text, _, _, _ ->
                val record = HistoryRecord(text.toString(), editTextBody.selectionEnd)
                viewModelEdit.addToHistory(record)
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
        if(savedInstanceState == null) {
            if (noteIndex == -1) {
                binding?.editTextBody?.requestFocus()
                viewModelEdit.addToHistory(HistoryRecord("", 0))
                val imm =
                    context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(binding?.editTextBody, InputMethodManager.SHOW_IMPLICIT)
            } else {
                viewModelMain.getNote(noteIndex)?.let {
                    binding?.editTextTitle?.setText(it.title)
                    viewModelEdit.addToHistory(HistoryRecord(it.text, it.text.length))
                }
            }
        }
    }

    private fun showUndoMenu(visible: Boolean){
        binding?.topAppBar?.menu?.findItem(R.id.action_undo)?.isVisible = visible
        binding?.topAppBar?.menu?.findItem(R.id.action_redo)?.isVisible = visible
    }

    override fun onPause() {
        super.onPause()
        if(activity?.isChangingConfigurations == true) return
        if(noteIndex == -1) {
            val newNote = Note(
                title = binding?.editTextTitle?.text.toString(),
                text = binding?.editTextBody?.text.toString(),
                lastModified = System.currentTimeMillis()
            )
            if(newNote.isNotBlank())
                viewModelMain.addNote(newNote)
        } else {
            val newTitle = binding?.editTextTitle?.text.toString()
            val newBody = binding?.editTextBody?.text.toString()
            val oldNote = viewModelMain.getNote(noteIndex)
            val isNoteChanged = oldNote?.title != newTitle || oldNote.text != newBody
            if(isNoteChanged){
                val editedNote = oldNote?.copy(
                    title = newTitle,
                    text = newBody,
                    lastModified = System.currentTimeMillis()
                )
                editedNote?.let {
                    if(it.isNotBlank())
                        viewModelMain.editNote(noteIndex, it)
                    else
                        viewModelMain.deleteNote(noteIndex)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(noteIndex: Int) =
            EditFragment().apply {
                arguments = Bundle().apply {
                    putInt(KEY_NOTE_INDEX, noteIndex)
                }
            }
    }
}