package com.example.cotidiano.ui.inicio

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.cotidiano.R
import com.example.cotidiano.dados.Repositorio
import com.example.cotidiano.databinding.FragmentInicioBinding
import com.example.cotidiano.ui.comum.mostrarXp
import com.example.cotidiano.ui.comum.pedirTexto
import com.example.cotidiano.ui.pomodoro.PomodoroViewModel
import kotlinx.coroutines.launch

class InicioFragment : Fragment(R.layout.fragment_inicio) {

    private lateinit var binding: FragmentInicioBinding
    private val vm: InicioViewModel by viewModels()
    private val pomodoroViewModel: PomodoroViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentInicioBinding.bind(view)

        val adapter = TarefasAdapter(vm::alternar, vm::excluir)
        binding.rvTarefas.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTarefas.adapter = adapter

        binding.fabAdicionar.setOnClickListener {
            pedirTexto(R.string.tarefas_nova, R.string.tarefas_dica) { vm.adicionar(it) }
        }

        binding.btnSair.setOnClickListener {
            Repositorio.obter(requireContext()).sair()
            pomodoroViewModel.resetar()
            findNavController().navigate(
                R.id.loginFragment,
                null,
                NavOptions.Builder().setPopUpTo(R.id.grafo_navegacao, true).build()
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vm.tarefas.collect { lista ->
                        adapter.submitList(lista)
                        binding.tvVazio.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
                        binding.tvContador.text = getString(
                            R.string.tarefas_contador, lista.count { it.concluidaHoje }, lista.size
                        )
                    }
                }
                launch { vm.eventosXp.collect { binding.root.mostrarXp(it) } }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        vm.atualizarDia()
    }

    class TarefasAdapter(
        private val aoAlternar: (TarefaItem) -> Unit,
        private val aoExcluir: (TarefaItem) -> Unit
    ) : ListAdapter<TarefaItem, TarefasAdapter.VH>(Comparador) {

        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val cb: CheckBox = v.findViewById(R.id.cbConcluida)
            val tv: TextView = v.findViewById(R.id.tvTitulo)
            val btnExcluir: ImageButton = v.findViewById(R.id.btnExcluir)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
            LayoutInflater.from(parent.context).inflate(R.layout.item_tarefa, parent, false)
        )

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = getItem(position)
            holder.tv.text = item.tarefa.titulo
            holder.tv.paintFlags =
                if (item.concluidaHoje) holder.tv.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                else holder.tv.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()

            holder.cb.setOnCheckedChangeListener(null)
            holder.cb.isChecked = item.concluidaHoje
            holder.cb.setOnCheckedChangeListener { _, _ -> aoAlternar(item) }
            holder.btnExcluir.setOnClickListener { aoExcluir(item) }
        }

        private object Comparador : DiffUtil.ItemCallback<TarefaItem>() {
            override fun areItemsTheSame(a: TarefaItem, b: TarefaItem) = a.tarefa.id == b.tarefa.id
            override fun areContentsTheSame(a: TarefaItem, b: TarefaItem) = a == b
        }
    }
}