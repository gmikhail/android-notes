package gmikhail.notes.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import gmikhail.notes.R
import gmikhail.notes.databinding.FragmentMainBinding
import gmikhail.notes.viewmodel.MainFragmentViewModel

class MainFragment : Fragment(R.layout.fragment_main) {

    private var viewBinding: FragmentMainBinding? = null
    private val viewModel: MainFragmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentMainBinding.inflate(inflater, container, false)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding?.topAppBar?.let { toolbar ->
            toolbar.inflateMenu(R.menu.main_menu)
            toolbar.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.action_dark_mode -> {
                        // TODO switch dark mode
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
        viewBinding?.fab?.setOnClickListener {
            // TODO create new note
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }
}