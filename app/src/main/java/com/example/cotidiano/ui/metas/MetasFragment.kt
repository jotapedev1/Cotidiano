package com.example.cotidiano.ui.metas

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.cotidiano.R
import com.example.cotidiano.dados.local.Meta
import com.example.cotidiano.dados.local.MetaComSubMetas
import com.example.cotidiano.dados.local.SubMeta
import com.example.cotidiano.databinding.FragmentMetasBinding
import com.example.cotidiano.ui.comum.mostrarXp
import com.example.cotidiano.ui.comum.pedirTexto
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.launch

class MetasFragment : Fragment(R.layout.fragment_metas) {

    private lateinit var binding: FragmentMetasBinding
    private val vm: MetasViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMetasBinding.bind(view)

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

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vm.metas.collect {
                        adapter.submitList(it)
                        binding.tvVazio.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
                launch { vm.eventosXp.collect { binding.root.mostrarXp(it) } }
            }
        }
    }

    class MetasAdapter(
        private val aoAlternarMeta: (Meta) -> Unit,
        private val aoAlternarSub: (MetaComSubMetas, SubMeta) -> Unit,
        private val aoAdicionarSub: (Meta) -> Unit,
        private val aoExcluir: (Meta) -> Unit
    ) : ListAdapter<MetaComSubMetas, MetasAdapter.VH>(Comparador) {

        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val cbMeta: CheckBox = v.findViewById(R.id.cbMeta)
            val tvTitulo: TextView = v.findViewById(R.id.tvTitulo)
            val btnExcluir: ImageButton = v.findViewById(R.id.btnExcluirMeta)
            val progress: LinearProgressIndicator = v.findViewById(R.id.progressMeta)
            val tvProgresso: TextView = v.findViewById(R.id.tvProgresso)
            val containerSubs: LinearLayout = v.findViewById(R.id.containerSubs)
            val btnAddSub: MaterialButton = v.findViewById(R.id.btnAddSub)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
            LayoutInflater.from(parent.context).inflate(R.layout.item_meta, parent, false)
        )

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = getItem(position)
            val meta = item.meta
            val subs = item.subMetas

            holder.tvTitulo.text = meta.titulo
            holder.tvTitulo.paintFlags =
                if (meta.concluida) holder.tvTitulo.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                else holder.tvTitulo.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()

            // Com submetas, a meta é concluída automaticamente; sem submetas, o checkbox vale.
            holder.cbMeta.setOnCheckedChangeListener(null)
            holder.cbMeta.isChecked = meta.concluida
            holder.cbMeta.isEnabled = subs.isEmpty()
            holder.cbMeta.setOnCheckedChangeListener { _, _ -> aoAlternarMeta(meta) }

            val feitas = subs.count { it.concluida }
            val pct = when {
                subs.isEmpty() -> if (meta.concluida) 100 else 0
                else -> feitas * 100 / subs.size
            }
            holder.progress.setProgressCompat(pct, true)
            val ctx = holder.itemView.context
            holder.tvProgresso.text =
                if (subs.isEmpty()) ctx.getString(R.string.metas_progresso_simples, pct)
                else ctx.getString(R.string.metas_progresso_subs, feitas, subs.size, pct)

            holder.containerSubs.removeAllViews()
            val inflater = LayoutInflater.from(holder.itemView.context)
            subs.forEach { sub ->
                val linha = inflater.inflate(R.layout.item_submeta, holder.containerSubs, false) as CheckBox
                linha.text = sub.titulo
                linha.isChecked = sub.concluida
                linha.setOnCheckedChangeListener { _, _ -> aoAlternarSub(item, sub) }
                holder.containerSubs.addView(linha)
            }

            holder.btnAddSub.setOnClickListener { aoAdicionarSub(meta) }
            holder.btnExcluir.setOnClickListener { aoExcluir(meta) }
        }

        private object Comparador : DiffUtil.ItemCallback<MetaComSubMetas>() {
            override fun areItemsTheSame(a: MetaComSubMetas, b: MetaComSubMetas) = a.meta.id == b.meta.id
            override fun areContentsTheSame(a: MetaComSubMetas, b: MetaComSubMetas) = a == b
        }
    }
}