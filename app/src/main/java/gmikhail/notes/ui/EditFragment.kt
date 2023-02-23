package gmikhail.notes.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import gmikhail.notes.R
import gmikhail.notes.data.db.Note
import gmikhail.notes.databinding.FragmentEditBinding
import gmikhail.notes.viewmodel.MainFragmentViewModel

private const val KEY_NOTE_INDEX = "noteIndex"

class EditFragment : Fragment(R.layout.fragment_edit) {

    private var noteIndex: Int = -1
    private var binding: FragmentEditBinding? = null
    private val viewModel: MainFragmentViewModel by activityViewModels{ MainFragmentViewModel.Factory }
    private var undoManager: UndoManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            noteIndex = it.getInt(KEY_NOTE_INDEX)
        }
    }

    override fun onPause() {
        super.onPause()
        if(noteIndex == -1) {
            val newNote = Note(
                title = binding?.editTextTitle?.text.toString(),
                text = binding?.editTextBody?.text.toString(),
                lastModified = System.currentTimeMillis()
            )
            if(newNote.isNotBlank())
                viewModel.addNote(newNote)
        } else {
            val newTitle = binding?.editTextTitle?.text.toString()
            val newBody = binding?.editTextBody?.text.toString()
            val oldNote = viewModel.notes.value?.get(noteIndex)
            val isNoteChanged = oldNote?.title != newTitle || oldNote.text != newBody
            if(isNoteChanged){
                val editedNote = oldNote?.apply {
                    title = newTitle
                    text = newBody
                    lastModified = System.currentTimeMillis()
                }
                editedNote?.let {
                    if(it.isNotBlank())
                        viewModel.editNote(noteIndex, it)
                    else
                        viewModel.deleteNote(noteIndex)
                }
            }
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
            toolbar.inflateMenu(R.menu.menu_edit)
            toolbar.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.action_undo -> {
                        undoManager?.undo()
                        true
                    }
                    R.id.action_redo -> {
                        undoManager?.redo()
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
        updateUndoMenu(canUndo = false, canRedo = false)
        if(undoManager == null){
            undoManager = binding?.editTextBody?.let {
                UndoManager(it, TextChangedListener { canUndo, canRedo ->
                    updateUndoMenu(canUndo, canRedo)
                })
            }
        }
        binding?.editTextTitle?.setOnFocusChangeListener { _, hasFocus ->
            showUndoMenu(!hasFocus)
        }
        binding?.editTextBody?.setOnFocusChangeListener { _, hasFocus ->
            showUndoMenu(hasFocus)
        }
        if(noteIndex == -1){
            binding?.editTextBody?.requestFocus()
            binding?.editTextBody?.setText("")
            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding?.editTextBody, InputMethodManager.SHOW_IMPLICIT)
        } else {
            viewModel.notes.value?.get(noteIndex)?.let {
                binding?.editTextTitle?.setText(it.title)
                binding?.editTextBody?.setText(it.text)
            }
        }
    }

    private fun showUndoMenu(visible: Boolean){
        binding?.topAppBar?.menu?.findItem(R.id.action_undo)?.isVisible = visible
        binding?.topAppBar?.menu?.findItem(R.id.action_redo)?.isVisible = visible
    }

    private fun updateUndoMenu(canUndo: Boolean, canRedo: Boolean){
        binding?.topAppBar?.menu?.findItem(R.id.action_undo)?.isEnabled = canUndo
        binding?.topAppBar?.menu?.findItem(R.id.action_redo)?.isEnabled = canRedo
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