package com.smkn8bone.jejakpklskadel.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
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
            viewModel.login(email, password)
        }

        viewModel.loginState.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                is LoginViewModel.LoginState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is LoginViewModel.LoginState.Success -> {
                    binding.progressBar.visibility = View.GONE
//                    findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                }
                is LoginViewModel.LoginState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}