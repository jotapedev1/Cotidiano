package com.example.cotidiano.ui.pomodoro

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.fragment.app.Fragment
import com.example.cotidiano.R
import com.example.cotidiano.databinding.FragmentPomodoroBinding

class PomodoroFragment : Fragment(R.layout.fragment_pomodoro) {

    private lateinit var binding: FragmentPomodoroBinding
    private var timer: CountDownTimer? = null
    private var restanteMs: Long = 25 * 60 * 1000
    private var rodando = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPomodoroBinding.bind(view)
        atualizarTexto()

        binding.btnIniciar.setOnClickListener {
            if (rodando) pausar() else iniciar()
        }
        binding.btnResetar.setOnClickListener {
            timer?.cancel()
            rodando = false
            restanteMs = 25 * 60 * 1000
            binding.btnIniciar.text = getString(R.string.pomodoro_iniciar)
            atualizarTexto()
        }
    }

    private fun iniciar() {
        rodando = true
        binding.btnIniciar.text = getString(R.string.pomodoro_pausar)
        timer = object : CountDownTimer(restanteMs, 1000) {
            override fun onTick(ms: Long) {
                restanteMs = ms
                atualizarTexto()
            }
            override fun onFinish() {
                rodando = false
                binding.btnIniciar.text = getString(R.string.pomodoro_iniciar)
                restanteMs = 25 * 60 * 1000
                atualizarTexto()
            }
        }.start()
    }

    private fun pausar() {
        timer?.cancel()
        rodando = false
        binding.btnIniciar.text = getString(R.string.pomodoro_continuar)
    }

    private fun atualizarTexto() {
        val min = restanteMs / 1000 / 60
        val seg = (restanteMs / 1000) % 60
        binding.tvTimer.text = "%02d:%02d".format(min, seg)
    }

    override fun onDestroyView() {
        timer?.cancel()
        super.onDestroyView()
    }
}