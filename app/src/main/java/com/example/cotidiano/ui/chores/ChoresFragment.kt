package com.example.cotidiano.ui.chores

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
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
import com.example.cotidiano.data.local.Obrigacao
import com.example.cotidiano.databinding.FragmentChoresBinding
import com.example.cotidiano.ui.common.formatarData
import com.example.cotidiano.ui.common.mostrarXp
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch
import kotlin.getValue

class ChoresFragment : Fragment(R.layout.fragment_chores) {

    private lateinit var binding: FragmentChoresBinding
    private val vm: ChoresViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentChoresBinding.bind(view)

        val adapter = ObrigacoesAdapter(vm::alternar, vm::excluir)
        binding.rvChores.layoutManager = LinearLayoutManager(requireContext())
        binding.rvChores.adapter = adapter
        binding.fabNovaObrigacao.setOnClickListener { mostrarDialogNova() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vm.obrigacoes.collect {
                        adapter.submitList(it)
                        binding.tvVazio.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
                launch { vm.xpEvents.collect { binding.root.mostrarXp(it) } }
            }
        }
    }

    private fun mostrarDialogNova() {
        val ctx = requireContext()
        var prazo: Long? = null

        val input = EditText(ctx).apply { hint = "Título da obrigação"; setSingleLine() }
        val btnPrazo = MaterialButton(
            ctx, null, com.google.android.material.R.attr.materialButtonOutlinedStyle
        ).apply { text = "Definir prazo (opcional)" }

        btnPrazo.setOnClickListener {
            val picker = MaterialDatePicker.Builder.datePicker().setTitleText("Prazo").build()
            picker.addOnPositiveButtonClickListener {
                prazo = it
                btnPrazo.text = "Prazo: ${formatarData(it)}"
            }
            picker.show(childFragmentManager, "prazo")
        }

        val box = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 0)
            addView(input)
            addView(btnPrazo)
        }

        AlertDialog.Builder(ctx)
            .setTitle("Nova obrigação")
            .setView(box)
            .setPositiveButton("Adicionar") { _, _ ->
                val txt = input.text.toString().trim()
                if (txt.isNotEmpty()) vm.adicionar(txt, prazo)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    class ObrigacoesAdapter(
        private val onToggle: (Obrigacao) -> Unit,
        private val onDelete: (Obrigacao) -> Unit
    ) : ListAdapter<Obrigacao, ObrigacoesAdapter.VH>(Diff) {

        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val cb: CheckBox = v.findViewById(R.id.cbConcluida)
            val tvTitulo: TextView = v.findViewById(R.id.tvTitulo)
            val tvPrazo: TextView = v.findViewById(R.id.tvPrazo)
            val btnDel: ImageButton = v.findViewById(R.id.btnExcluir)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
            LayoutInflater.from(parent.context).inflate(R.layout.item_chore, parent, false)
        )

        override fun onBindViewHolder(holder: VH, position: Int) {
            val o = getItem(position)
            holder.tvTitulo.text = o.titulo
            holder.tvTitulo.paintFlags =
                if (o.concluida) holder.tvTitulo.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                else holder.tvTitulo.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()

            holder.cb.setOnCheckedChangeListener(null)
            holder.cb.isChecked = o.concluida
            holder.cb.setOnCheckedChangeListener { _, _ -> onToggle(o) }

            val prazo = o.prazo
            if (prazo == null) {
                holder.tvPrazo.visibility = View.GONE
            } else {
                holder.tvPrazo.visibility = View.VISIBLE
                holder.tvPrazo.text = "Prazo: ${formatarData(prazo)}"
                val atrasada = !o.concluida && prazo + 86_400_000L < System.currentTimeMillis()
                holder.tvPrazo.setTextColor(if (atrasada) Color.parseColor("#B3261E") else Color.GRAY)
            }
            holder.btnDel.setOnClickListener { onDelete(o) }
        }

        private object Diff : DiffUtil.ItemCallback<Obrigacao>() {
            override fun areItemsTheSame(a: Obrigacao, b: Obrigacao) = a.id == b.id
            override fun areContentsTheSame(a: Obrigacao, b: Obrigacao) = a == b
        }
    }
}