package com.example.cotidiano.ui.cronograma

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cotidiano.R
import com.example.cotidiano.dados.local.BlocoCronograma
import com.example.cotidiano.databinding.DialogBlocoBinding
import com.example.cotidiano.databinding.FragmentCronogramaBinding
import com.example.cotidiano.databinding.ItemCronogramaBinding
import com.example.cotidiano.ui.comum.aviso
import com.example.cotidiano.ui.comum.enquantoVisivel
import kotlinx.coroutines.launch
import java.util.Calendar

// A posição na lista + 1 é o número do dia (padrão do Calendar: 1 = domingo)
private val DIAS = listOf(
    "Domingo", "Segunda-feira", "Terça-feira", "Quarta-feira",
    "Quinta-feira", "Sexta-feira", "Sábado"
)

// Cores disponíveis para os blocos: nome exibido + código hexadecimal
private val CORES = listOf(
    "Roxo" to "#6750A4", "Verde" to "#4CAF50", "Laranja" to "#FF9800", "Azul" to "#2196F3",
    "Rosa" to "#E91E63", "Verde-água" to "#009688", "Vermelho" to "#F44336"
)

class CronogramaFragment : Fragment(R.layout.fragment_cronograma) {

    private val vm: CronogramaViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentCronogramaBinding.bind(view)

        // A tela sempre abre no dia da semana de hoje
        val hoje = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        vm.selecionarDia(hoje)
        binding.spinnerDiaFiltro.adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, DIAS)
        binding.spinnerDiaFiltro.setSelection(hoje - 1)
        binding.spinnerDiaFiltro.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, posicao: Int, id: Long) =
                vm.selecionarDia(posicao + 1)
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }

        val adapter = CronogramaAdapter { bloco -> mostrarDialogoBloco(bloco) } // clicar = editar
        binding.rvCronograma.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCronograma.adapter = adapter
        binding.fabNovoBloco.setOnClickListener { mostrarDialogoBloco(null) } // null = novo bloco

        enquantoVisivel {
            launch { vm.blocos.collect { adapter.atualizar(it) } }
        }
    }

    /** Diálogo de criar (bloco = null) ou editar um bloco. Layout em dialog_bloco.xml. */
    private fun mostrarDialogoBloco(bloco: BlocoCronograma?) {
        val ctx = requireContext()
        val d = DialogBlocoBinding.inflate(layoutInflater)

        d.spinnerDia.adapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_dropdown_item, DIAS)
        d.spinnerCor.adapter =
            ArrayAdapter(ctx, android.R.layout.simple_spinner_dropdown_item, CORES.map { it.first })
        d.spinnerDia.setSelection((bloco?.diaSemana ?: vm.diaSelecionado.value) - 1)

        // Editando: preenche os campos com os valores atuais
        if (bloco != null) {
            d.etTitulo.setText(bloco.titulo)
            d.etInicio.setText(formatarMinutos(bloco.inicioMin))
            d.etFim.setText(formatarMinutos(bloco.fimMin))
            d.spinnerCor.setSelection(
                CORES.indexOfFirst { Color.parseColor(it.second) == bloco.cor }.coerceAtLeast(0)
            )
        }

        val dialogo = AlertDialog.Builder(ctx)
            .setTitle(if (bloco == null) R.string.cronograma_titulo_dialog else R.string.cronograma_editar)
            .setView(d.root)
            .setPositiveButton(if (bloco == null) R.string.acao_adicionar else R.string.acao_salvar) { _, _ ->
                val titulo = d.etTitulo.text.toString().trim()
                val dia = d.spinnerDia.selectedItemPosition + 1
                val cor = Color.parseColor(CORES[d.spinnerCor.selectedItemPosition].second)

                // Início aceita 00:00 (mas não 24:00). Fim aceita 24:00; "00:00" no fim vale meia-noite.
                val inicio = lerHorario(d.etInicio.text.toString())?.takeIf { it < 24 * 60 }
                val fim = lerHorario(d.etFim.text.toString())?.let { if (it == 0) 24 * 60 else it }

                if (titulo.isEmpty() || inicio == null || fim == null || inicio >= fim) {
                    aviso(R.string.cronograma_erro)
                } else if (bloco == null) {
                    vm.adicionar(titulo, dia, inicio, fim, cor)
                } else {
                    vm.atualizar(bloco.copy(titulo = titulo, diaSemana = dia, inicioMin = inicio, fimMin = fim, cor = cor))
                }
            }
            .setNegativeButton(R.string.acao_cancelar, null)

        // Só ao editar existe o botão de excluir
        if (bloco != null) dialogo.setNeutralButton(R.string.acao_excluir) { _, _ -> vm.excluir(bloco) }
        dialogo.show()
    }

    /** Converte "HH:mm" em minutos desde 00:00. Devolve null se for inválido (aceita até 24:00). */
    private fun lerHorario(texto: String): Int? {
        val partes = texto.trim().split(":")
        if (partes.size != 2) return null
        val h = partes[0].toIntOrNull() ?: return null
        val m = partes[1].toIntOrNull() ?: return null
        val total = h * 60 + m
        return if (h in 0..24 && m in 0..59 && total <= 24 * 60) total else null
    }

    private fun formatarMinutos(minutos: Int) = "%02d:%02d".format(minutos / 60, minutos % 60)

    class CronogramaAdapter(private val aoClicar: (BlocoCronograma) -> Unit) :
        RecyclerView.Adapter<CronogramaAdapter.VH>() {

        private var lista = emptyList<BlocoCronograma>()

        fun atualizar(nova: List<BlocoCronograma>) {
            lista = nova
            notifyDataSetChanged()
        }

        class VH(val b: ItemCronogramaBinding) : RecyclerView.ViewHolder(b.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            VH(ItemCronogramaBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun getItemCount() = lista.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            val bloco = lista[position]
            val b = holder.b
            b.tvHorario.text = "%02d:%02d - %02d:%02d".format(
                bloco.inicioMin / 60, bloco.inicioMin % 60, bloco.fimMin / 60, bloco.fimMin % 60
            )
            b.tvAtividade.text = bloco.titulo
            // Fundo arredondado na cor escolhida
            b.tvAtividade.background = GradientDrawable().apply {
                setColor(bloco.cor)
                cornerRadius = 24f
            }
            b.root.setOnClickListener { aoClicar(bloco) }
        }
    }
}
