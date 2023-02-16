package gmikhail.notes.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import gmikhail.notes.R
import gmikhail.notes.databinding.FragmentMainBinding
import gmikhail.notes.viewmodel.MainFragmentViewModel

class MainFragment : Fragment(R.layout.fragment_main) {

    private var binding: FragmentMainBinding? = null
    private val viewModel: MainFragmentViewModel by viewModels{ MainFragmentViewModel.Factory }

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
                        // TODO recyclerView change display mode
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
            // TODO create new note
        }
        viewModel.notes.observe(viewLifecycleOwner) {
            // TODO where to store layout manager and adapter? Recreate after each data update is wrong
            binding?.recyclerView?.let { rw ->
                rw.layoutManager = LinearLayoutManager(context)
                rw.adapter = NoteAdapter(it.toTypedArray())
            }
        }
        viewModel.fetchNotes()
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}