package gmikhail.notes.ui

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
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
                val id = viewModel.notes.value?.get(position)?.uid ?: return@AdapterItemClickListener
                val action = MainFragmentDirections.actionMainFragmentToEditFragment(id)
                val navController = findNavController()
                if(navController.currentDestination?.id == R.id.mainFragment)
                    navController.navigate(action)
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
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            actionMode?.finish()
            val action = MainFragmentDirections.actionMainFragmentToEditFragment()
            it.findNavController().navigate(action)
        }
        viewModel.displayModeList.observe(viewLifecycleOwner) { linearLayout ->
            binding?.recyclerView?.let { recyclerView ->
                recyclerView.layoutManager =
                    if(linearLayout)
                        LinearLayoutManager(context)
                    else {
                        val columns = resources.getInteger(R.integer.notes_list_columns)
                        StaggeredGridLayoutManager(columns, RecyclerView.VERTICAL)
                    }
            }
        }
        binding?.recyclerView?.adapter = adapter
        binding?.recyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                binding?.fab?.let { fab ->
                    if (dy > 0 && fab.isExtended)
                        fab.shrink()
                    else if (dy < 0 && !fab.isExtended)
                        fab.extend()
                }
            }
        })
        viewModel.notesState.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
        viewModel.notes.observe(viewLifecycleOwner) {
            binding?.textFrontMessage?.visibility = if(it.isEmpty()) View.VISIBLE else View.GONE
            if(it.isNotEmpty() && System.currentTimeMillis() - it.first().lastModified <= 1000)
                binding?.recyclerView?.let { recyclerView ->
                    recyclerView.post {
                        recyclerView.smoothScrollToPosition(0)
                    }
                }
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
        if(viewModel.selection.value?.isNotEmpty() == true)
            startActionMode()
        viewModel.selection.observe(viewLifecycleOwner){
            actionMode?.title = it.size.toString()
            actionMode?.menu?.findItem(R.id.action_delete)?.isEnabled = it.isNotEmpty()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}