package gmikhail.notes.ui

import android.content.Intent
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import gmikhail.notes.BuildConfig
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
        findPreference<Preference>(getString(R.string.license_key))?.setOnPreferenceClickListener {
            startActivity(Intent(context, OssLicensesMenuActivity::class.java))
            true
        }
        findPreference<Preference>(getString(R.string.version_key))?.summary =
            BuildConfig.VERSION_NAME
    }
}