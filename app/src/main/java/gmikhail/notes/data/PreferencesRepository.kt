package gmikhail.notes.data

import android.content.Context
import androidx.preference.PreferenceManager

class PreferencesRepository(private val context: Context) {

    fun loadBool(key: String, defValue: Boolean): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key, defValue)
    }

    fun saveBool(key: String, value: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(key, value).apply()
    }
}