package com.smkn8bone.jejakpklskadel.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.smkn8bone.jejakpklskadel.AuthActivity
import com.smkn8bone.jejakpklskadel.MainActivity
import com.smkn8bone.jejakpklskadel.R
import com.smkn8bone.jejakpklskadel.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmailLogin.text.toString()
            val password = binding.etPasswordLogin.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Semua field harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.login(email, password) { success, message, isProfileCompleted ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

                if (success) {
                    val intent = if (isProfileCompleted) {
                        Intent(requireContext(), MainActivity::class.java)
                    } else {
                        Intent(requireContext(), AuthActivity::class.java)
                    }
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
            }
        }

//        viewModel.loginState.observe(viewLifecycleOwner) { state ->
//            when (state) {
//                is LoginViewModel.LoginState.Loading -> {
//                    binding.loadingOverlay.show()
//                }
//
//                is LoginViewModel.LoginState.Success -> {
//                    binding.loadingOverlay.hide()
//                    findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
//                }
//
//                is LoginViewModel.LoginState.Error -> {
//                    binding.loadingOverlay.hide()
//                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}