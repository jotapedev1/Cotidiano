package com.example.cotidiano.ui.cadastro

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.cotidiano.R
import com.example.cotidiano.dados.Repositorio
import com.example.cotidiano.databinding.FragmentCadastroBinding
import kotlinx.coroutines.launch

class CadastroFragment : Fragment(R.layout.fragment_cadastro) {

    private lateinit var binding: FragmentCadastroBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCadastroBinding.bind(view)
        val repo = Repositorio.obter(requireContext())

        binding.btnCadastrar.setOnClickListener {
            val nome = binding.etNome.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val senha = binding.etSenha.text.toString()
            val confirmar = binding.etConfirmarSenha.text.toString()

            val erro = when {
                nome.isBlank() || email.isBlank() || senha.isBlank() -> R.string.msg_preencha_campos
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> R.string.msg_email_invalido
                senha.length < 6 -> R.string.msg_senha_curta
                senha != confirmar -> R.string.msg_senhas_diferentes
                else -> null
            }
            if (erro != null) {
                Toast.makeText(requireContext(), erro, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.btnCadastrar.isEnabled = false
            viewLifecycleOwner.lifecycleScope.launch {
                if (repo.cadastrar(nome, email, senha)) {
                    Toast.makeText(requireContext(), R.string.msg_conta_criada, Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.acao_cadastro_para_login)
                } else {
                    binding.btnCadastrar.isEnabled = true
                    Toast.makeText(requireContext(), R.string.msg_email_em_uso, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}