package com.example.cotidiano.ui.home

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
import com.example.cotidiano.databinding.FragmentHomeBinding

data class Tarefa(val id: Int, val titulo: String, var concluida: Boolean = false)

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding
    private val tarefas = mutableListOf(
        Tarefa(1, "Estudar Kotlin"),
        Tarefa(2, "Fazer exercícios", true),
        Tarefa(3, "Ler 20 páginas")
    )
    private val adapter = TasksAdapter(tarefas) { atualizarContador() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)

        binding.rvTasks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTasks.adapter = adapter
        atualizarContador()

        binding.fabAdicionar.setOnClickListener { mostrarDialogNovaTarefa() }
    }

    private fun atualizarContador() {
        binding.tvContador.text =
            "${tarefas.count { it.concluida }}/${tarefas.size} concluídas"
    }

    private fun mostrarDialogNovaTarefa() {
        val input = EditText(requireContext()).apply { hint = "Descrição da tarefa" }
        AlertDialog.Builder(requireContext())
            .setTitle("Nova tarefa")
            .setView(input)
            .setPositiveButton("Adicionar") { _, _ ->
                val txt = input.text.toString().trim()
                if (txt.isNotEmpty()) {
                    tarefas.add(Tarefa((tarefas.maxOfOrNull { it.id } ?: 0) + 1, txt))
                    adapter.notifyItemInserted(tarefas.size - 1)
                    atualizarContador()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    class TasksAdapter(
        private val lista: MutableList<Tarefa>,
        private val onChange: () -> Unit
    ) : RecyclerView.Adapter<TasksAdapter.VH>() {

        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val cb: CheckBox = v.findViewById(R.id.cbConcluida)
            val tv: TextView = v.findViewById(R.id.tvTitulo)
            val btnDel: ImageButton = v.findViewById(R.id.btnExcluir)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val t = lista[position]
            holder.tv.text = t.titulo
            holder.cb.setOnCheckedChangeListener(null)
            holder.cb.isChecked = t.concluida
            holder.cb.setOnCheckedChangeListener { _, checked ->
                t.concluida = checked
                onChange()
            }
            holder.btnDel.setOnClickListener {
                lista.removeAt(position)
                notifyItemRemoved(position)
                onChange()
            }
        }

        override fun getItemCount() = lista.size
    }
}