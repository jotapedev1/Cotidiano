package com.example.cotidiano.ui.login

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.cotidiano.R
import com.example.cotidiano.databinding.FragmentLoginBinding

class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var binding: FragmentLoginBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginBinding.bind(view)

        val prefs = requireContext().getSharedPreferences("cotidiano_prefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("is_logged_in", false)) {
            findNavController().navigate(R.id.acao_login_para_inicio)
            return
        }

        binding.btnEntrar.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val senha = binding.etSenha.text.toString()

            if (email.isBlank() || senha.isBlank()) {
                Toast.makeText(requireContext(), R.string.msg_preencha_campos, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            prefs.edit().putBoolean("is_logged_in", true).apply()
            findNavController().navigate(R.id.acao_login_para_inicio)
        }

        binding.btnIrCadastro.setOnClickListener {
            findNavController().navigate(R.id.acao_login_para_cadastro)
        }
    }
}
