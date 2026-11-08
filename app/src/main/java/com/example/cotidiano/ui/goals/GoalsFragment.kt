package com.example.cotidiano.ui.goals

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
import com.example.cotidiano.data.local.Meta
import com.example.cotidiano.data.local.MetaComSubMetas
import com.example.cotidiano.data.local.SubMeta
import com.example.cotidiano.databinding.FragmentGoalsBinding
import com.example.cotidiano.ui.common.mostrarXp
import com.example.cotidiano.ui.common.pedirTexto
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.launch

class GoalsFragment : Fragment(R.layout.fragment_goals) {

    private lateinit var binding: FragmentGoalsBinding
    private val vm: GoalsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentGoalsBinding.bind(view)

        val adapter = GoalsAdapter(
            onToggleMeta = vm::alternarMeta,
            onToggleSub = vm::alternarSubMeta,
            onAddSub = { meta ->
                pedirTexto("Nova submeta", "Descrição da submeta") { vm.adicionarSubMeta(meta, it) }
            },
            onDelete = vm::excluirMeta
        )
        binding.rvGoals.layoutManager = LinearLayoutManager(requireContext())
        binding.rvGoals.adapter = adapter
        binding.fabNovaMeta.setOnClickListener {
            pedirTexto("Nova meta", "Título da meta") { vm.adicionarMeta(it) }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vm.metas.collect {
                        adapter.submitList(it)
                        binding.tvVazio.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
                launch { vm.xpEvents.collect { binding.root.mostrarXp(it) } }
            }
        }
    }

    class GoalsAdapter(
        private val onToggleMeta: (Meta) -> Unit,
        private val onToggleSub: (MetaComSubMetas, SubMeta) -> Unit,
        private val onAddSub: (Meta) -> Unit,
        private val onDelete: (Meta) -> Unit
    ) : ListAdapter<MetaComSubMetas, GoalsAdapter.VH>(Diff) {

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
            LayoutInflater.from(parent.context).inflate(R.layout.item_goal, parent, false)
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
            holder.cbMeta.setOnCheckedChangeListener { _, _ -> onToggleMeta(meta) }

            val feitas = subs.count { it.concluida }
            val pct = when {
                subs.isEmpty() -> if (meta.concluida) 100 else 0
                else -> feitas * 100 / subs.size
            }
            holder.progress.setProgressCompat(pct, true)
            holder.tvProgresso.text =
                if (subs.isEmpty()) "$pct%" else "$feitas/${subs.size} submetas · $pct%"

            holder.containerSubs.removeAllViews()
            val inflater = LayoutInflater.from(holder.itemView.context)
            subs.forEach { sub ->
                val row = inflater.inflate(R.layout.item_subgoal, holder.containerSubs, false) as CheckBox
                row.text = sub.titulo
                row.isChecked = sub.concluida
                row.setOnCheckedChangeListener { _, _ -> onToggleSub(item, sub) }
                holder.containerSubs.addView(row)
            }

            holder.btnAddSub.setOnClickListener { onAddSub(meta) }
            holder.btnExcluir.setOnClickListener { onDelete(meta) }
        }

        private object Diff : DiffUtil.ItemCallback<MetaComSubMetas>() {
            override fun areItemsTheSame(a: MetaComSubMetas, b: MetaComSubMetas) = a.meta.id == b.meta.id
            override fun areContentsTheSame(a: MetaComSubMetas, b: MetaComSubMetas) = a == b
        }
    }
}