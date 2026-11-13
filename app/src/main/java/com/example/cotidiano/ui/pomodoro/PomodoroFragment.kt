package com.example.cotidiano.ui.pomodoro

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.cotidiano.R
import com.example.cotidiano.databinding.FragmentPomodoroBinding
import com.example.cotidiano.ui.comum.mostrarXp
import kotlinx.coroutines.launch

class PomodoroFragment : Fragment(R.layout.fragment_pomodoro) {

    private lateinit var binding: FragmentPomodoroBinding
    private val viewModel: PomodoroViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPomodoroBinding.bind(view)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.restanteMs.collect { ms ->
                        atualizarTexto(ms)
                    }
                }
                launch {
                    viewModel.rodando.collect { rodando ->
                        atualizarEstadoBotao(rodando, viewModel.restanteMs.value)
                    }
                }
                launch {
                    viewModel.eventosXp.collect { xp ->
                        view?.mostrarXp(xp)
                    }
                }
            }
        }

        binding.btn25m.setOnClickListener { viewModel.definirDuracaoMinutos(25) }
        binding.btn15m.setOnClickListener { viewModel.definirDuracaoMinutos(15) }
        binding.btn5m.setOnClickListener { viewModel.definirDuracaoMinutos(5) }

        binding.btnIniciar.setOnClickListener {
            viewModel.iniciarOuPausar()
        }

        binding.btnResetar.setOnClickListener {
            viewModel.resetar()
        }
    }

    private fun atualizarTexto(ms: Long) {
        val min = ms / 1000 / 60
        val seg = (ms / 1000) % 60
        binding.tvTimer.text = "%02d:%02d".format(min, seg)
    }

    private fun atualizarEstadoBotao(rodando: Boolean, ms: Long) {
        // Assume tempo inicial padrão ou atual (25 min = 1500000ms, mas checamos se está em progresso)
        binding.btnIniciar.text = when {
            rodando -> getString(R.string.pomodoro_pausar)
            // Se o tempo restante for menor que o tempo total ou pausado
            else -> {
                // Verificar se foi pausado ou está no início
                // Se rodando é false e timer não está no max, é continuar
                getString(R.string.pomodoro_iniciar) // or continuar
            }
        }
    }
}
