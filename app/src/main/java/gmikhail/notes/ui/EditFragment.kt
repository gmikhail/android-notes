package gmikhail.notes.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import gmikhail.notes.R
import gmikhail.notes.data.Note
import gmikhail.notes.databinding.FragmentEditBinding
import gmikhail.notes.viewmodel.MainFragmentViewModel

private const val KEY_NOTE_INDEX = "noteIndex"

class EditFragment : Fragment(R.layout.fragment_edit) {

    private var noteIndex: Int = -1
    private var binding: FragmentEditBinding? = null
    private val viewModel: MainFragmentViewModel by activityViewModels{ MainFragmentViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            noteIndex = it.getInt(KEY_NOTE_INDEX)
        }
    }

    override fun onPause() {
        super.onPause()
        if(noteIndex != -1) {
            val editedNote = viewModel.notes.value?.get(noteIndex)?.apply {
                title = binding?.editTextTitle?.text.toString()
                text = binding?.editTextBody?.text.toString()
                lastModified = System.currentTimeMillis()
            }
            editedNote?.let {
                if(it.isNotBlank())
                    viewModel.editNote(noteIndex, it)
                else
                    viewModel.deleteNote(noteIndex)
            }
        } else {
            val newNote = Note(
                title = binding?.editTextTitle?.text.toString(),
                text = binding?.editTextBody?.text.toString(),
                lastModified = System.currentTimeMillis()
            )
            if(newNote.isNotBlank())
                viewModel.addNote(newNote)
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
                        // TODO undo
                        true
                    }
                    R.id.action_redo -> {
                        // TODO redo
                        true
                    }
                    R.id.action_done -> {
                        // TODO done
                        true
                    }
                    else -> false
                }

            }
        }
        if(noteIndex != -1){
            viewModel.notes.value?.get(noteIndex)?.let {
                binding?.editTextTitle?.setText(it.title)
                binding?.editTextBody?.setText(it.text)
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