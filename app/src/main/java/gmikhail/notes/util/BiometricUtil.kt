package gmikhail.notes.util

import android.content.Context
import androidx.biometric.BiometricManager

object BiometricUtil {
    fun isSupported(context: Context): Boolean =
        BiometricManager.from(context)
            .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
                BiometricManager.BIOMETRIC_SUCCESS
}