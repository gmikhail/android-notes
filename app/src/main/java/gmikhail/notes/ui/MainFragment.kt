package gmikhail.notes.ui

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import gmikhail.notes.R
import gmikhail.notes.databinding.FragmentMainBinding
import gmikhail.notes.viewmodel.MainFragmentViewModel

class MainFragment : Fragment(R.layout.fragment_main) {

    private var binding: FragmentMainBinding? = null
    private val viewModel: MainFragmentViewModel by activityViewModels{ MainFragmentViewModel.Factory }

    private var actionMode: ActionMode? = null
    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            mode?.menuInflater?.inflate(R.menu.context_menu, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            return when (item?.itemId) {
                R.id.action_delete -> {
                    viewModel.deleteSelectedNotes()
                    mode?.finish()
                    true
                }
                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            viewModel.clearSelection()
            actionMode = null
        }
    }

    private fun startActionMode() {
        if (actionMode == null) {
            actionMode = requireActivity().startActionMode(actionModeCallback)
        }
    }

    private var adapter = NoteAdapter(
        NoteAdapter.AdapterItemClickListener { position ->
            if(actionMode != null){
                viewModel.select(position)
            } else {
                parentFragmentManager.commit {
                    setReorderingAllowed(true)
                    add(R.id.fragment_container_view, EditFragment.newInstance(position))
                    addToBackStack(null)
                }
            }
        },
        NoteAdapter.AdapterItemLongClickListener { position ->
            startActionMode()
            viewModel.select(position)
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.topAppBar?.let { toolbar ->
            toolbar.inflateMenu(R.menu.main_menu)
            toolbar.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.action_dark_mode -> {
                        viewModel.switchDarkMode()
                        true
                    }
                    R.id.action_change_display_mode -> {
                        viewModel.switchDisplayMode()
                        true
                    }
                    R.id.action_settings -> {
                        // TODO navigate to settings
                        true
                    }
                    else -> false
                }
            }
        }
        binding?.fab?.setOnClickListener {
            parentFragmentManager.commit {
                setReorderingAllowed(true)
                add(R.id.fragment_container_view, EditFragment())
                addToBackStack(null)
            }
        }
        viewModel.displayModeList.observe(viewLifecycleOwner) { linearLayout ->
            binding?.recyclerView?.let { recyclerView ->
                recyclerView.layoutManager =
                    if(linearLayout)
                        LinearLayoutManager(context)
                    else {
                        val columns = resources.getInteger(R.integer.notes_list_columns)
                        GridLayoutManager(context, columns)
                    }
            }
        }
        binding?.recyclerView?.adapter = adapter
        viewModel.notesState.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
        viewModel.notes.observe(viewLifecycleOwner) {
            binding?.textFrontMessage?.visibility = if(it.isEmpty()) View.VISIBLE else View.GONE
        }
        viewModel.darkMode.observe(viewLifecycleOwner) {
            // MIUI bug https://stackoverflow.com/q/63209993/
            AppCompatDelegate.setDefaultNightMode(
                if(it) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
            binding?.topAppBar?.menu?.findItem(R.id.action_dark_mode)?.setIcon(
                if(it) R.drawable.ic_light_mode
                else R.drawable.ic_dark_mode
            )
        }
        viewModel.selection.observe(viewLifecycleOwner){
            actionMode?.title = it.size.toString()
            actionMode?.menu?.findItem(R.id.action_delete)?.isEnabled = it.isNotEmpty()
            if(it.isNotEmpty())
                binding?.recyclerView?.adapter?.notifyItemChanged(it.last())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}