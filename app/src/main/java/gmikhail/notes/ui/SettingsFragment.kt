package gmikhail.notes.ui

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import gmikhail.notes.R
import gmikhail.notes.util.BiometricUtil

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        findPreference<SwitchPreferenceCompat>(getString(R.string.auth_key))?.let {
            val isCanBio = BiometricUtil.isSupported(requireContext())
            it.isEnabled = isCanBio
            it.summary = if(isCanBio) null else getString(R.string.feature_not_supported)
        }
    }
}