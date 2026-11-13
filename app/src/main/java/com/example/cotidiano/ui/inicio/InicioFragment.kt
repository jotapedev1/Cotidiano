package com.example.cotidiano.ui.inicio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cotidiano.R
import com.example.cotidiano.dados.Repositorio
import com.example.cotidiano.databinding.FragmentInicioBinding
import com.example.cotidiano.databinding.ItemTarefaBinding
import com.example.cotidiano.ui.comum.enquantoVisivel
import com.example.cotidiano.ui.comum.mostrarXp
import com.example.cotidiano.ui.comum.pedirTexto
import com.example.cotidiano.ui.comum.riscar
import com.example.cotidiano.ui.pomodoro.PomodoroViewModel
import kotlinx.coroutines.launch

class InicioFragment : Fragment(R.layout.fragment_inicio) {

    private val vm: InicioViewModel by viewModels()
    // Compartilhado com a aba Pomodoro: precisamos zerar o timer ao sair da conta
    private val pomodoroVm: PomodoroViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentInicioBinding.bind(view)

        val adapter = TarefasAdapter(vm::alternar, vm::excluir)
        binding.rvTarefas.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTarefas.adapter = adapter

        binding.fabAdicionar.setOnClickListener {
            pedirTexto(R.string.tarefas_nova, R.string.tarefas_dica) { vm.adicionar(it) }
        }

        // Sair: encerra a sessão e volta ao login limpando o histórico de telas
        binding.btnSair.setOnClickListener {
            Repositorio.obter(requireContext()).sair()
            pomodoroVm.resetar()
            findNavController().navigate(
                R.id.loginFragment, null,
                NavOptions.Builder().setPopUpTo(R.id.grafo_navegacao, true).build()
            )
        }

        enquantoVisivel {
            launch {
                vm.tarefas.collect { lista ->
                    adapter.atualizar(lista)
                    binding.tvVazio.isVisible = lista.isEmpty()
                    binding.tvContador.text = getString(
                        R.string.tarefas_contador, lista.count { it.concluidaHoje }, lista.size
                    )
                }
            }
            launch { vm.eventosXp.collect { binding.root.mostrarXp(it) } }
        }
    }

    override fun onResume() {
        super.onResume()
        vm.atualizarDia()
    }

    /** Liga a lista de tarefas ao RecyclerView (uma linha por tarefa). */
    class TarefasAdapter(
        private val aoAlternar: (TarefaItem) -> Unit,
        private val aoExcluir: (TarefaItem) -> Unit
    ) : RecyclerView.Adapter<TarefasAdapter.VH>() {

        private var lista = emptyList<TarefaItem>()

        fun atualizar(nova: List<TarefaItem>) {
            lista = nova
            notifyDataSetChanged()
        }

        class VH(val b: ItemTarefaBinding) : RecyclerView.ViewHolder(b.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            VH(ItemTarefaBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun getItemCount() = lista.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = lista[position]
            val b = holder.b
            b.tvTitulo.text = item.tarefa.titulo
            b.tvTitulo.riscar(item.concluidaHoje)

            // Remove o listener antes de mudar o checkbox, senão ele dispararia sozinho
            b.cbConcluida.setOnCheckedChangeListener(null)
            b.cbConcluida.isChecked = item.concluidaHoje
            b.cbConcluida.setOnCheckedChangeListener { _, _ -> aoAlternar(item) }
            b.btnExcluir.setOnClickListener { aoExcluir(item) }
        }
    }
}
