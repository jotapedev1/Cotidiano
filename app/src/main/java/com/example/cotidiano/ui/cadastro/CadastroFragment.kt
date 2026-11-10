package com.example.cotidiano.ui.cadastro

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.cotidiano.R
import com.example.cotidiano.databinding.FragmentCadastroBinding

class CadastroFragment : Fragment(R.layout.fragment_cadastro) {

    private lateinit var binding: FragmentCadastroBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCadastroBinding.bind(view)

        binding.btnCadastrar.setOnClickListener {
            val nome = binding.etNome.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val senha = binding.etSenha.text.toString()
            val confirmar = binding.etConfirmarSenha.text.toString()

            when {
                nome.isBlank() || email.isBlank() || senha.isBlank() ->
                    Toast.makeText(requireContext(), R.string.msg_preencha_campos, Toast.LENGTH_SHORT).show()
                senha != confirmar ->
                    Toast.makeText(requireContext(), R.string.msg_senhas_diferentes, Toast.LENGTH_SHORT).show()
                else -> {
                    Toast.makeText(requireContext(), R.string.msg_conta_criada, Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.acao_cadastro_para_login)
                }
            }
        }
    }
}