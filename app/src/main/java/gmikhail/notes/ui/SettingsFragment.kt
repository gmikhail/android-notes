package gmikhail.notes.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import gmikhail.notes.BuildConfig
import gmikhail.notes.R
import gmikhail.notes.databinding.FragmentSettingsBinding
import gmikhail.notes.util.BiometricUtil
import gmikhail.notes.viewmodel.SettingsViewModel

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private var binding: FragmentSettingsBinding? = null
    private val viewModel: SettingsViewModel by viewModels{ SettingsViewModel.Factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.materialToolbar?.let { toolbar ->
            val navController = findNavController()
            val appBarConfiguration = AppBarConfiguration(navController.graph)
            toolbar.setupWithNavController(navController, appBarConfiguration)
        }
        binding?.switchAuth?.let { switch ->
            viewModel.isAuthEnabled.observe(viewLifecycleOwner) {
                switch.isChecked = it
            }
            switch.isEnabled = BiometricUtil.isSupported(requireContext())
            switch.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setAuth(isChecked)
            }
        }
        binding?.textViewLicense?.setOnClickListener {
            startActivity(Intent(context, OssLicensesMenuActivity::class.java))
        }
        binding?.textViewVersion?.text = BuildConfig.VERSION_NAME
    }
}