package com.example.cotidiano.ui.obrigacoes

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cotidiano.R
import com.example.cotidiano.dados.local.Obrigacao
import com.example.cotidiano.databinding.DialogObrigacaoBinding
import com.example.cotidiano.databinding.FragmentObrigacoesBinding
import com.example.cotidiano.databinding.ItemObrigacaoBinding
import com.example.cotidiano.ui.comum.enquantoVisivel
import com.example.cotidiano.ui.comum.formatarData
import com.example.cotidiano.ui.comum.mostrarXp
import com.example.cotidiano.ui.comum.riscar
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch

class ObrigacoesFragment : Fragment(R.layout.fragment_obrigacoes) {

    private val vm: ObrigacoesViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentObrigacoesBinding.bind(view)

        val adapter = ObrigacoesAdapter(vm::alternar, vm::excluir)
        binding.rvObrigacoes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvObrigacoes.adapter = adapter
        binding.fabNovaObrigacao.setOnClickListener { mostrarDialogNova() }

        enquantoVisivel {
            launch {
                vm.obrigacoes.collect {
                    adapter.atualizar(it)
                    binding.tvVazio.isVisible = it.isEmpty()
                }
            }
            launch { vm.eventosXp.collect { binding.root.mostrarXp(it) } }
        }
    }

    /** Diálogo de nova obrigação: título + prazo opcional (layout em dialog_obrigacao.xml). */
    private fun mostrarDialogNova() {
        val d = DialogObrigacaoBinding.inflate(layoutInflater)
        var prazo: Long? = null

        d.btnPrazo.setOnClickListener {
            val seletor = MaterialDatePicker.Builder.datePicker().setTitleText(R.string.prazo_titulo).build()
            seletor.addOnPositiveButtonClickListener {
                prazo = it
                d.btnPrazo.text = getString(R.string.prazo_com_data, formatarData(it))
            }
            seletor.show(childFragmentManager, "prazo")
        }

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.obrigacoes_nova)
            .setView(d.root)
            .setPositiveButton(R.string.acao_adicionar) { _, _ ->
                val titulo = d.etTitulo.text.toString().trim()
                if (titulo.isNotEmpty()) vm.adicionar(titulo, prazo)
            }
            .setNegativeButton(R.string.acao_cancelar, null)
            .show()
    }

    class ObrigacoesAdapter(
        private val aoAlternar: (Obrigacao) -> Unit,
        private val aoExcluir: (Obrigacao) -> Unit
    ) : RecyclerView.Adapter<ObrigacoesAdapter.VH>() {

        private var lista = emptyList<Obrigacao>()

        fun atualizar(nova: List<Obrigacao>) {
            lista = nova
            notifyDataSetChanged()
        }

        class VH(val b: ItemObrigacaoBinding) : RecyclerView.ViewHolder(b.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            VH(ItemObrigacaoBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun getItemCount() = lista.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            val o = lista[position]
            val b = holder.b

            b.tvTitulo.text = o.titulo
            b.tvTitulo.riscar(o.concluida)

            b.cbConcluida.setOnCheckedChangeListener(null)
            b.cbConcluida.isChecked = o.concluida
            b.cbConcluida.setOnCheckedChangeListener { _, _ -> aoAlternar(o) }

            // Prazo: vermelho se já passou (damos o dia inteiro de tolerância) e ainda não foi feita
            val prazo = o.prazo
            b.tvPrazo.isVisible = prazo != null
            if (prazo != null) {
                b.tvPrazo.text = b.root.context.getString(R.string.prazo_com_data, formatarData(prazo))
                val atrasada = !o.concluida && prazo + 86_400_000L < System.currentTimeMillis()
                b.tvPrazo.setTextColor(if (atrasada) Color.parseColor("#B3261E") else Color.GRAY)
            }

            b.btnExcluir.setOnClickListener { aoExcluir(o) }
        }
    }
}
