package com.example.cotidiano.ui.cronograma

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cotidiano.R
import com.example.cotidiano.databinding.FragmentCronogramaBinding

data class BlocoHorario(val horario: String, val atividade: String, val cor: Int)

class CronogramaFragment : Fragment(R.layout.fragment_cronograma) {

    private lateinit var binding: FragmentCronogramaBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCronogramaBinding.bind(view)

        val blocos = listOf(
            BlocoHorario("06:00 - 07:00", "Academia", Color.parseColor("#6750A4")),
            BlocoHorario("08:00 - 12:00", "Trabalho", Color.parseColor("#4CAF50")),
            BlocoHorario("12:00 - 13:00", "Almoço", Color.parseColor("#FF9800")),
            BlocoHorario("13:00 - 17:00", "Trabalho", Color.parseColor("#4CAF50")),
            BlocoHorario("18:00 - 19:00", "Estudos", Color.parseColor("#2196F3")),
            BlocoHorario("20:00 - 21:00", "Leitura", Color.parseColor("#E91E63"))
        )

        binding.rvCronograma.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCronograma.adapter = CronogramaAdapter(blocos)
    }

    class CronogramaAdapter(private val lista: List<BlocoHorario>) :
        RecyclerView.Adapter<CronogramaAdapter.VH>() {

        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val tvHorario: TextView = v.findViewById(R.id.tvHorario)
            val tvAtividade: TextView = v.findViewById(R.id.tvAtividade)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_cronograma, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val b = lista[position]
            holder.tvHorario.text = b.horario
            holder.tvAtividade.text = b.atividade
            val bg = GradientDrawable().apply {
                setColor(b.cor)
                cornerRadius = 24f
            }
            holder.tvAtividade.background = bg
        }

        override fun getItemCount() = lista.size
    }
}