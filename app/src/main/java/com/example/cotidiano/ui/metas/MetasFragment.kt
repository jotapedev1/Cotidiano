package com.example.cotidiano.ui.metas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cotidiano.R
import com.example.cotidiano.dados.local.Meta
import com.example.cotidiano.dados.local.MetaComSubMetas
import com.example.cotidiano.dados.local.SubMeta
import com.example.cotidiano.databinding.FragmentMetasBinding
import com.example.cotidiano.databinding.ItemMetaBinding
import com.example.cotidiano.databinding.ItemSubmetaBinding
import com.example.cotidiano.ui.comum.enquantoVisivel
import com.example.cotidiano.ui.comum.mostrarXp
import com.example.cotidiano.ui.comum.pedirTexto
import com.example.cotidiano.ui.comum.riscar
import kotlinx.coroutines.launch

class MetasFragment : Fragment(R.layout.fragment_metas) {

    private val vm: MetasViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentMetasBinding.bind(view)

        val adapter = MetasAdapter(
            aoAlternarMeta = vm::alternarMeta,
            aoAlternarSub = vm::alternarSubMeta,
            aoAdicionarSub = { meta ->
                pedirTexto(R.string.submeta_nova, R.string.submeta_dica) { vm.adicionarSubMeta(meta, it) }
            },
            aoExcluir = vm::excluirMeta
        )
        binding.rvMetas.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMetas.adapter = adapter

        binding.fabNovaMeta.setOnClickListener {
            pedirTexto(R.string.metas_nova, R.string.metas_dica_titulo) { vm.adicionarMeta(it) }
        }

        enquantoVisivel {
            launch {
                vm.metas.collect {
                    adapter.atualizar(it)
                    binding.tvVazio.isVisible = it.isEmpty()
                }
            }
            launch { vm.eventosXp.collect { binding.root.mostrarXp(it) } }
        }
    }

    /** Cada item da lista é um cartão com a meta, a barra de progresso e as submetas. */
    class MetasAdapter(
        private val aoAlternarMeta: (Meta) -> Unit,
        private val aoAlternarSub: (MetaComSubMetas, SubMeta) -> Unit,
        private val aoAdicionarSub: (Meta) -> Unit,
        private val aoExcluir: (Meta) -> Unit
    ) : RecyclerView.Adapter<MetasAdapter.VH>() {

        private var lista = emptyList<MetaComSubMetas>()

        fun atualizar(nova: List<MetaComSubMetas>) {
            lista = nova
            notifyDataSetChanged()
        }

        class VH(val b: ItemMetaBinding) : RecyclerView.ViewHolder(b.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            VH(ItemMetaBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun getItemCount() = lista.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = lista[position]
            val meta = item.meta
            val subs = item.subMetas
            val b = holder.b
            val ctx = b.root.context

            b.tvTitulo.text = meta.titulo
            b.tvTitulo.riscar(meta.concluida)

            // Com submetas, a meta conclui sozinha (checkbox bloqueado); sem submetas, o checkbox vale
            b.cbMeta.setOnCheckedChangeListener(null)
            b.cbMeta.isChecked = meta.concluida
            b.cbMeta.isEnabled = subs.isEmpty()
            b.cbMeta.setOnCheckedChangeListener { _, _ -> aoAlternarMeta(meta) }

            // Porcentagem de progresso da meta
            val feitas = subs.count { it.concluida }
            val pct = when {
                subs.isEmpty() -> if (meta.concluida) 100 else 0
                else -> feitas * 100 / subs.size
            }
            b.progressMeta.setProgressCompat(pct, true)
            b.tvProgresso.text =
                if (subs.isEmpty()) ctx.getString(R.string.metas_progresso_simples, pct)
                else ctx.getString(R.string.metas_progresso_subs, feitas, subs.size, pct)

            // Uma linha (checkbox) para cada submeta
            b.containerSubs.removeAllViews()
            subs.forEach { sub ->
                val linha = ItemSubmetaBinding.inflate(LayoutInflater.from(ctx), b.containerSubs, false).root
                linha.text = sub.titulo
                linha.isChecked = sub.concluida
                linha.setOnCheckedChangeListener { _, _ -> aoAlternarSub(item, sub) }
                b.containerSubs.addView(linha)
            }

            b.btnAddSub.setOnClickListener { aoAdicionarSub(meta) }
            b.btnExcluirMeta.setOnClickListener { aoExcluir(meta) }
        }
    }
}
