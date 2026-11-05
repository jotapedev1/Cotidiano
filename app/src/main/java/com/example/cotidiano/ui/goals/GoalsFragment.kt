package com.example.cotidiano.ui.goals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cotidiano.R
import com.example.cotidiano.databinding.FragmentGoalsBinding
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.slider.Slider

data class Meta(
    val id: Int,
    val titulo: String,
    var progresso: Int,
    val prazo: String
)

class GoalsFragment : Fragment(R.layout.fragment_goals) {

    private lateinit var binding: FragmentGoalsBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentGoalsBinding.bind(view)

        val metas = mutableListOf(
            Meta(1, "Ler 12 livros no ano", 50, "31/12/2026"),
            Meta(2, "Correr 5km sem parar", 30, "30/06/2026"),
            Meta(3, "Aprender Compose", 70, "31/03/2026")
        )

        binding.rvGoals.layoutManager = LinearLayoutManager(requireContext())
        binding.rvGoals.adapter = GoalsAdapter(metas)
    }

    class GoalsAdapter(private val lista: List<Meta>) :
        RecyclerView.Adapter<GoalsAdapter.VH>() {

        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val tvTitulo: TextView = v.findViewById(R.id.tvTitulo)
            val tvPorcentagem: TextView = v.findViewById(R.id.tvPorcentagem)
            val tvPrazo: TextView = v.findViewById(R.id.tvPrazo)
            val progress: LinearProgressIndicator = v.findViewById(R.id.progressMeta)
            val slider: Slider = v.findViewById(R.id.sliderProgresso)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_goal, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val m = lista[position]
            holder.tvTitulo.text = m.titulo
            holder.tvPrazo.text = "Prazo: ${m.prazo}"
            holder.tvPorcentagem.text = "${m.progresso}%"
            holder.progress.progress = m.progresso
            holder.slider.value = m.progresso.toFloat()

            holder.slider.addOnChangeListener { _, value, _ ->
                m.progresso = value.toInt()
                holder.tvPorcentagem.text = "${m.progresso}%"
                holder.progress.progress = m.progresso
            }
        }

        override fun getItemCount() = lista.size
    }
}