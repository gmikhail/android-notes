package gmikhail.notes.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import gmikhail.notes.R
import gmikhail.notes.databinding.FragmentEditBinding
import gmikhail.notes.viewmodel.MainFragmentViewModel

private const val NOTE_ID = "noteId"

class EditFragment : Fragment(R.layout.fragment_edit) {

    private var noteId: Int = -1
    private var binding: FragmentEditBinding? = null
    private val viewModel: MainFragmentViewModel by activityViewModels{ MainFragmentViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            noteId = it.getInt(NOTE_ID)
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
        if(noteId != -1){
            viewModel.notes.value?.get(noteId)?.let {
                binding?.editTextTitle?.setText(it.title)
                binding?.editTextBody?.setText(it.text)
            }
        } else {
            // TODO create new note
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(noteId: Int) =
            EditFragment().apply {
                arguments = Bundle().apply {
                    putInt(NOTE_ID, noteId)
                }
            }
    }
}