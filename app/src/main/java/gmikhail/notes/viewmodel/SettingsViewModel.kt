package gmikhail.notes.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import gmikhail.notes.data.PreferencesRepository

internal const val AUTH_KEY = "auth"
internal const val AUTH_DEFAULT_VALUE = false

class SettingsViewModel(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {
    private var _isAuthEnabled = MutableLiveData<Boolean>()
    val isAuthEnabled: LiveData<Boolean>
        get() = _isAuthEnabled

    init {
        loadAuth()
    }

    private fun loadAuth(){
        _isAuthEnabled.value = preferencesRepository.loadBool(AUTH_KEY, AUTH_DEFAULT_VALUE)
    }

    fun setAuth(isEnabled: Boolean){
        if(_isAuthEnabled.value == isEnabled) return
        _isAuthEnabled.value = isEnabled
        preferencesRepository.saveBool(AUTH_KEY, isEnabled)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                return SettingsViewModel(
                    PreferencesRepository(application)
                ) as T
            }
        }
    }
}