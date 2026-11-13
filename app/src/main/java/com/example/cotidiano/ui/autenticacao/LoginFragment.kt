package com.example.cotidiano.ui.autenticacao

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.cotidiano.R
import com.example.cotidiano.dados.Repositorio
import com.example.cotidiano.databinding.FragmentLoginBinding
import com.example.cotidiano.ui.comum.aviso
import kotlinx.coroutines.launch

class LoginFragment : Fragment(R.layout.fragment_login) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentLoginBinding.bind(view)
        val repo = Repositorio.obter(requireContext())

        // Já existe sessão aberta: pula direto para a tela inicial
        if (repo.logado) {
            findNavController().navigate(R.id.acao_login_para_inicio)
            return
        }

        binding.btnEntrar.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val senha = binding.etSenha.text.toString()

            if (email.isBlank() || senha.isBlank()) {
                aviso(R.string.msg_preencha_campos)
                return@setOnClickListener
            }

            binding.btnEntrar.isEnabled = false // evita clique duplo
            viewLifecycleOwner.lifecycleScope.launch {
                if (repo.entrar(email, senha)) {
                    findNavController().navigate(R.id.acao_login_para_inicio)
                } else {
                    binding.btnEntrar.isEnabled = true
                    aviso(R.string.msg_credenciais_invalidas)
                }
            }
        }

        binding.btnIrCadastro.setOnClickListener {
            findNavController().navigate(R.id.acao_login_para_cadastro)
        }
    }
}
