package com.example.cotidiano.ui.pomodoro

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.cotidiano.R
import com.example.cotidiano.databinding.FragmentPomodoroBinding
import com.example.cotidiano.ui.comum.enquantoVisivel
import com.example.cotidiano.ui.comum.mostrarXp
import kotlinx.coroutines.launch

class PomodoroFragment : Fragment(R.layout.fragment_pomodoro) {

    private val vm: PomodoroViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentPomodoroBinding.bind(view)

        // A tela só mostra o estado do ViewModel
        enquantoVisivel {
            launch {
                vm.restanteSeg.collect { binding.tvTimer.text = "%02d:%02d".format(it / 60, it % 60) }
            }
            launch {
                vm.rodando.collect {
                    binding.btnIniciar.setText(if (it) R.string.pomodoro_pausar else R.string.pomodoro_iniciar)
                }
            }
            launch { vm.eventosXp.collect { binding.root.mostrarXp(it) } }
        }

        binding.btn25m.setOnClickListener { vm.definirMinutos(25) }
        binding.btn15m.setOnClickListener { vm.definirMinutos(15) }
        binding.btn5m.setOnClickListener { vm.definirMinutos(5) }
        binding.btnIniciar.setOnClickListener { vm.iniciarOuPausar() }
        binding.btnResetar.setOnClickListener { vm.resetar() }
    }
}
