package gmikhail.notes.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import gmikhail.notes.R
import gmikhail.notes.databinding.FragmentAuthBinding
import gmikhail.notes.viewmodel.AUTH_KEY
import gmikhail.notes.viewmodel.AUTH_DEFAULT_VALUE
import java.util.concurrent.Executor

class AuthFragment : Fragment(R.layout.fragment_auth) {

    private lateinit var binding: FragmentAuthBinding
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(isNeedToAuthenticate()) {
            binding.imageButtonAuth.setOnClickListener {
                authenticate()
            }
            authenticate()
        } else {
            val action = AuthFragmentDirections.actionAuthFragmentToMainFragment()
            findNavController().navigate(action)
        }
    }

    private fun isNeedToAuthenticate(): Boolean =
        PreferenceManager.getDefaultSharedPreferences(requireActivity())
            .getBoolean(AUTH_KEY, AUTH_DEFAULT_VALUE)

    private fun authenticate(){
        executor = ContextCompat.getMainExecutor(requireContext())
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int,
                                                   errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(context,
                        getString(R.string.auth_error, errString), Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    val action = AuthFragmentDirections.actionAuthFragmentToMainFragment()
                    findNavController().navigate(action)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(context, R.string.auth_fail, Toast.LENGTH_SHORT).show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.auth_prompt_title))
            .setNegativeButtonText(getString(R.string.auth_prompt_negative))
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}