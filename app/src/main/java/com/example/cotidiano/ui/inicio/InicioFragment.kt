package com.example.cotidiano.ui.inicio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cotidiano.R
import com.example.cotidiano.databinding.FragmentInicioBinding

data class Tarefa(val id: Int, val titulo: String, var concluida: Boolean = false)

class InicioFragment : Fragment(R.layout.fragment_inicio) {

    private lateinit var binding: FragmentInicioBinding
    private val tarefas = mutableListOf(
        Tarefa(1, "Estudar Kotlin"),
        Tarefa(2, "Fazer exercícios", true),
        Tarefa(3, "Ler 20 páginas")
    )
    private val adapter = TarefasAdapter(tarefas) { atualizarContador() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentInicioBinding.bind(view)

        binding.rvTarefas.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTarefas.adapter = adapter
        atualizarContador()

        binding.fabAdicionar.setOnClickListener { mostrarDialogNovaTarefa() }
    }

    private fun atualizarContador() {
        binding.tvContador.text = getString(
            R.string.tarefas_contador, tarefas.count { it.concluida }, tarefas.size
        )
    }

    private fun mostrarDialogNovaTarefa() {
        val campo = EditText(requireContext()).apply { hint = getString(R.string.tarefas_dica) }
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.tarefas_nova)
            .setView(campo)
            .setPositiveButton(R.string.acao_adicionar) { _, _ ->
                val txt = campo.text.toString().trim()
                if (txt.isNotEmpty()) {
                    tarefas.add(Tarefa((tarefas.maxOfOrNull { it.id } ?: 0) + 1, txt))
                    adapter.notifyItemInserted(tarefas.size - 1)
                    atualizarContador()
                }
            }
            .setNegativeButton(R.string.acao_cancelar, null)
            .show()
    }

    class TarefasAdapter(
        private val lista: MutableList<Tarefa>,
        private val aoMudar: () -> Unit
    ) : RecyclerView.Adapter<TarefasAdapter.VH>() {

        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val cb: CheckBox = v.findViewById(R.id.cbConcluida)
            val tv: TextView = v.findViewById(R.id.tvTitulo)
            val btnExcluir: ImageButton = v.findViewById(R.id.btnExcluir)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_tarefa, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val t = lista[position]
            holder.tv.text = t.titulo
            holder.cb.setOnCheckedChangeListener(null)
            holder.cb.isChecked = t.concluida
            holder.cb.setOnCheckedChangeListener { _, checked ->
                t.concluida = checked
                aoMudar()
            }
            holder.btnExcluir.setOnClickListener {
                val pos = holder.bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
                lista.removeAt(pos)
                notifyItemRemoved(pos)
                aoMudar()
            }
        }

        override fun getItemCount() = lista.size
    }
}