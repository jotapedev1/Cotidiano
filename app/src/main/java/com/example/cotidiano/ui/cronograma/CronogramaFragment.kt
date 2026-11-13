package com.example.cotidiano.ui.cronograma

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cotidiano.R
import com.example.cotidiano.dados.local.BlocoCronograma
import com.example.cotidiano.databinding.FragmentCronogramaBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class CronogramaFragment : Fragment(R.layout.fragment_cronograma) {

    private lateinit var binding: FragmentCronogramaBinding
    private val viewModel: CronogramaViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCronogramaBinding.bind(view)

        viewModel.selecionarDia(java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK))

        val diasNomes = arrayOf("Domingo", "Segunda-feira", "Terça-feira", "Quarta-feira", "Quinta-feira", "Sexta-feira", "Sábado")
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, diasNomes)
        binding.spinnerDiaFiltro.adapter = spinnerAdapter
        binding.spinnerDiaFiltro.setSelection((viewModel.diaSelecionado.value - 1).coerceIn(0, 6))

        binding.spinnerDiaFiltro.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.selecionarDia(position + 1)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val adapter = CronogramaAdapter(
            onItemClick = { bloco ->
                mostrarDialogoBloco(bloco)
            }
        )

        binding.rvCronograma.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCronograma.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.blocos.collect { lista ->
                    adapter.atualizarLista(lista)
                }
            }
        }

        binding.fabNovoBloco.setOnClickListener {
            mostrarDialogoBloco(null)
        }
    }

    private fun mostrarDialogoBloco(blocoExistente: BlocoCronograma?) {
        val context = requireContext()
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 32)
        }

        val etTitulo = EditText(context).apply {
            hint = getString(R.string.cronograma_dica_titulo)
            setSingleLine()
            blocoExistente?.let { setText(it.titulo) }
        }

        val diasNomes = arrayOf("Domingo", "Segunda-feira", "Terça-feira", "Quarta-feira", "Quinta-feira", "Sexta-feira", "Sábado")
        val spinnerDia = Spinner(context).apply {
            adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, diasNomes)
            val diaAtualIndex = blocoExistente?.let { it.diaSemana - 1 } ?: (viewModel.diaSelecionado.value - 1)
            setSelection(diaAtualIndex.coerceIn(0, 6))
        }

        val etInicio = EditText(context).apply {
            hint = "Início (HH:mm, de 00:00 a 23:59)"   // etInicio
            setSingleLine()
            blocoExistente?.let { setText(formatarMinutos(it.inicioMin)) }
        }

        val etFim = EditText(context).apply {
            hint = "Fim (HH:mm, até 24:00)"             // etFim
            setSingleLine()
            blocoExistente?.let { setText(formatarMinutos(it.fimMin)) }
        }

        val cores = intArrayOf(
            Color.parseColor("#6750A4"),
            Color.parseColor("#4CAF50"),
            Color.parseColor("#FF9800"),
            Color.parseColor("#2196F3"),
            Color.parseColor("#E91E63"),
            Color.parseColor("#009688"),
            Color.parseColor("#F44336")
        )
        var corSelecionada = blocoExistente?.cor ?: cores[0]

        val layoutCores = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 16, 0, 16)
            gravity = android.view.Gravity.CENTER
        }

        val colorViews = mutableListOf<View>()

        cores.forEach { cor ->
            val colorView = View(context).apply {
                val size = 80
                layoutParams = LinearLayout.LayoutParams(size, size).apply { setMargins(8, 0, 8, 0) }
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(cor)
                    if (cor == corSelecionada) {
                        setStroke(8, Color.BLACK)
                    }
                }
                setOnClickListener {
                    corSelecionada = cor
                    colorViews.forEach { v ->
                        val c = (v.tag as Int)
                        v.background = GradientDrawable().apply {
                            shape = GradientDrawable.OVAL
                            setColor(c)
                            if (c == corSelecionada) {
                                setStroke(8, Color.BLACK)
                            }
                        }
                    }
                }
                tag = cor
            }
            colorViews.add(colorView)
            layoutCores.addView(colorView)
        }

        layout.addView(TextView(context).apply { text = "Título:"; setTextColor(Color.GRAY) })
        layout.addView(etTitulo)
        layout.addView(TextView(context).apply { text = "Dia da semana:"; setTextColor(Color.GRAY); setPadding(0, 16, 0, 0) })
        layout.addView(spinnerDia)
        layout.addView(TextView(context).apply { text = "Horário de início:"; setTextColor(Color.GRAY); setPadding(0, 16, 0, 0) })
        layout.addView(etInicio)
        layout.addView(TextView(context).apply { text = "Horário de término:"; setTextColor(Color.GRAY); setPadding(0, 16, 0, 0) })
        layout.addView(etFim)
        layout.addView(TextView(context).apply { text = "Cor:"; setTextColor(Color.GRAY); setPadding(0, 16, 0, 0) })
        layout.addView(layoutCores)

        val titleText = if (blocoExistente == null) getString(R.string.cronograma_titulo_dialog) else "Editar Bloco"
        val positiveText = if (blocoExistente == null) getString(R.string.acao_adicionar) else "Salvar"

        val builder = AlertDialog.Builder(context)
            .setTitle(titleText)
            .setView(layout)
            .setPositiveButton(positiveText) { _, _ ->
                val titulo = etTitulo.text.toString().trim()
                val diaSemana = spinnerDia.selectedItemPosition + 1
                val inicioMin = parseHorario(etInicio.text.toString().trim(), permitir24 = false)
                val fimMin = parseHorario(etFim.text.toString().trim(), permitir24 = true)
                    ?.let { if (it == 0) 24 * 60 else it }

                if (titulo.isNotEmpty() && inicioMin != null && fimMin != null && inicioMin < fimMin) {
                    if (blocoExistente == null) {
                        viewModel.adicionarBloco(titulo, diaSemana, inicioMin, fimMin, corSelecionada)
                    } else {
                        viewModel.atualizarBloco(blocoExistente.copy(
                            titulo = titulo,
                            diaSemana = diaSemana,
                            inicioMin = inicioMin,
                            fimMin = fimMin,
                            cor = corSelecionada
                        ))
                    }
                } else {
                    Snackbar.make(binding.root, "Preencha corretamente (Início < Fim, formato HH:mm)", Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.acao_cancelar, null)

        if (blocoExistente != null) {
            builder.setNeutralButton(R.string.acao_excluir) { _, _ ->
                viewModel.excluirBloco(blocoExistente)
            }
        }

        builder.show()
    }

    // 24:00 só vale como horário de término. 00:00 como término vira meia-noite (24:00).
    private fun parseHorario(s: String, permitir24: Boolean): Int? {
        val partes = s.split(":")
        if (partes.size != 2) return null
        val h = partes[0].trim().toIntOrNull() ?: return null
        val m = partes[1].trim().toIntOrNull() ?: return null
        if (m !in 0..59) return null
        if (h == 24 && m == 0 && permitir24) return 24 * 60
        if (h !in 0..23) return null
        return h * 60 + m
    }

    private fun formatarMinutos(minutos: Int): String {
        val h = minutos / 60
        val m = minutos % 60
        return "%02d:%02d".format(h, m)
    }

    class CronogramaAdapter(private val onItemClick: (BlocoCronograma) -> Unit) :
        RecyclerView.Adapter<CronogramaAdapter.VH>() {

        private var lista: List<BlocoCronograma> = emptyList()

        fun atualizarLista(novaLista: List<BlocoCronograma>) {
            lista = novaLista
            notifyDataSetChanged()
        }

        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val tvHorario: TextView = v.findViewById(R.id.tvHorario)
            val tvAtividade: TextView = v.findViewById(R.id.tvAtividade)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_cronograma, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val b = lista[position]
            val horarioStr = "%02d:%02d - %02d:%02d".format(b.inicioMin / 60, b.inicioMin % 60, b.fimMin / 60, b.fimMin % 60)
            holder.tvHorario.text = horarioStr
            holder.tvAtividade.text = b.titulo

            val bg = GradientDrawable().apply {
                setColor(b.cor)
                cornerRadius = 24f
            }
            holder.tvAtividade.background = bg

            holder.itemView.setOnClickListener {
                onItemClick(b)
            }
        }

        override fun getItemCount() = lista.size
    }
}
