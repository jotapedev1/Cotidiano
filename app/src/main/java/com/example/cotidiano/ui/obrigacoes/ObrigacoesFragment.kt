package com.example.cotidiano.ui.obrigacoes

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
import com.example.cotidiano.dados.local.Obrigacao
import com.example.cotidiano.databinding.FragmentObrigacoesBinding
import com.example.cotidiano.ui.comum.formatarData
import com.example.cotidiano.ui.comum.mostrarXp
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch

class ObrigacoesFragment : Fragment(R.layout.fragment_obrigacoes) {

    private lateinit var binding: FragmentObrigacoesBinding
    private val vm: ObrigacoesViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentObrigacoesBinding.bind(view)

        val adapter = ObrigacoesAdapter(vm::alternar, vm::excluir)
        binding.rvObrigacoes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvObrigacoes.adapter = adapter
        binding.fabNovaObrigacao.setOnClickListener { mostrarDialogNova() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vm.obrigacoes.collect {
                        adapter.submitList(it)
                        binding.tvVazio.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
                launch { vm.eventosXp.collect { binding.root.mostrarXp(it) } }
            }
        }
    }

    private fun mostrarDialogNova() {
        val ctx = requireContext()
        var prazo: Long? = null

        val campo = EditText(ctx).apply { hint = getString(R.string.obrigacoes_dica_titulo); setSingleLine() }
        val btnPrazo = MaterialButton(
            ctx, null, com.google.android.material.R.attr.materialButtonOutlinedStyle
        ).apply { text = getString(R.string.obrigacoes_definir_prazo) }

        btnPrazo.setOnClickListener {
            val seletor = MaterialDatePicker.Builder.datePicker().setTitleText(R.string.prazo_titulo).build()
            seletor.addOnPositiveButtonClickListener {
                prazo = it
                btnPrazo.text = getString(R.string.prazo_com_data, formatarData(it))
            }
            seletor.show(childFragmentManager, "prazo")
        }

        val caixa = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 0)
            addView(campo)
            addView(btnPrazo)
        }

        AlertDialog.Builder(ctx)
            .setTitle(R.string.obrigacoes_nova)
            .setView(caixa)
            .setPositiveButton(R.string.acao_adicionar) { _, _ ->
                val txt = campo.text.toString().trim()
                if (txt.isNotEmpty()) vm.adicionar(txt, prazo)
            }
            .setNegativeButton(R.string.acao_cancelar, null)
            .show()
    }

    class ObrigacoesAdapter(
        private val aoAlternar: (Obrigacao) -> Unit,
        private val aoExcluir: (Obrigacao) -> Unit
    ) : ListAdapter<Obrigacao, ObrigacoesAdapter.VH>(Comparador) {

        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val cb: CheckBox = v.findViewById(R.id.cbConcluida)
            val tvTitulo: TextView = v.findViewById(R.id.tvTitulo)
            val tvPrazo: TextView = v.findViewById(R.id.tvPrazo)
            val btnExcluir: ImageButton = v.findViewById(R.id.btnExcluir)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
            LayoutInflater.from(parent.context).inflate(R.layout.item_obrigacao, parent, false)
        )

        override fun onBindViewHolder(holder: VH, position: Int) {
            val o = getItem(position)
            holder.tvTitulo.text = o.titulo
            holder.tvTitulo.paintFlags =
                if (o.concluida) holder.tvTitulo.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                else holder.tvTitulo.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()

            holder.cb.setOnCheckedChangeListener(null)
            holder.cb.isChecked = o.concluida
            holder.cb.setOnCheckedChangeListener { _, _ -> aoAlternar(o) }

            val prazo = o.prazo
            if (prazo == null) {
                holder.tvPrazo.visibility = View.GONE
            } else {
                holder.tvPrazo.visibility = View.VISIBLE
                holder.tvPrazo.text =
                    holder.itemView.context.getString(R.string.prazo_com_data, formatarData(prazo))
                val atrasada = !o.concluida && prazo + 86_400_000L < System.currentTimeMillis()
                holder.tvPrazo.setTextColor(if (atrasada) Color.parseColor("#B3261E") else Color.GRAY)
            }
            holder.btnExcluir.setOnClickListener { aoExcluir(o) }
        }

        private object Comparador : DiffUtil.ItemCallback<Obrigacao>() {
            override fun areItemsTheSame(a: Obrigacao, b: Obrigacao) = a.id == b.id
            override fun areContentsTheSame(a: Obrigacao, b: Obrigacao) = a == b
        }
    }
}