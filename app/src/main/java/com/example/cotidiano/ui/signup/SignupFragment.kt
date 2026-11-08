package com.example.cotidiano.ui.signup

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.cotidiano.R
import com.example.cotidiano.databinding.FragmentSignupBinding

class SignupFragment : Fragment(R.layout.fragment_signup) {

    private lateinit var binding: FragmentSignupBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSignupBinding.bind(view)

        binding.btnCadastrar.setOnClickListener {
            val nome = binding.etNome.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val senha = binding.etSenha.text.toString()
            val confirmar = binding.etConfirmarSenha.text.toString()

            when {
                nome.isBlank() || email.isBlank() || senha.isBlank() ->
                    Toast.makeText(requireContext(), "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                senha != confirmar ->
                    Toast.makeText(requireContext(), "Senhas não coincidem", Toast.LENGTH_SHORT).show()
                else -> {
                    Toast.makeText(requireContext(), "Conta criada! Faça login.", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_signup_to_login)
                }
            }
        }
    }
}