package com.example.cotidiano.ui.login

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.cotidiano.R
import com.example.cotidiano.dados.Repositorio
import com.example.cotidiano.databinding.FragmentLoginBinding
import kotlinx.coroutines.launch

class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var binding: FragmentLoginBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginBinding.bind(view)
        val repo = Repositorio.obter(requireContext())

        if (repo.logado) {
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

            binding.btnEntrar.isEnabled = false
            viewLifecycleOwner.lifecycleScope.launch {
                if (repo.entrar(email, senha)) {
                    findNavController().navigate(R.id.acao_login_para_inicio)
                } else {
                    binding.btnEntrar.isEnabled = true
                    Toast.makeText(requireContext(), R.string.msg_credenciais_invalidas, Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnIrCadastro.setOnClickListener {
            findNavController().navigate(R.id.acao_login_para_cadastro)
        }
    }
}