package com.example.cotidiano.ui.comum

import android.graphics.Paint
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.cotidiano.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

// Funções de apoio usadas por várias telas.

/** O seletor de data devolve meia-noite UTC, então formatamos em UTC. */
fun formatarData(millis: Long): String =
    SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
        .apply { timeZone = TimeZone.getTimeZone("UTC") }
        .format(millis)

/** Mostra uma mensagem curta na tela. */
fun Fragment.aviso(@StringRes mensagem: Int) =
    Toast.makeText(requireContext(), mensagem, Toast.LENGTH_SHORT).show()

/** Diálogo simples com um campo de texto (usado para nova meta, submeta e tarefa). */
fun Fragment.pedirTexto(@StringRes titulo: Int, @StringRes dica: Int, aoConfirmar: (String) -> Unit) {
    val campo = EditText(requireContext()).apply { hint = getString(dica); setSingleLine() }
    AlertDialog.Builder(requireContext())
        .setTitle(titulo)
        .setView(campo)
        .setPositiveButton(R.string.acao_adicionar) { _, _ ->
            val txt = campo.text.toString().trim()
            if (txt.isNotEmpty()) aoConfirmar(txt)
        }
        .setNegativeButton(R.string.acao_cancelar, null)
        .show()
}

/** Executa [bloco] só enquanto a tela está visível (usado para observar os Flows). */
fun Fragment.enquantoVisivel(bloco: suspend CoroutineScope.() -> Unit) {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED, bloco)
    }
}

/** Feedback provisório de XP; a recompensa com animação entra depois. */
fun View.mostrarXp(delta: Int) {
    if (delta > 0) Snackbar.make(this, context.getString(R.string.xp_ganho, delta), Snackbar.LENGTH_SHORT).show()
}

/** Risca (ou tira o risco de) um texto, usado em itens concluídos. */
fun TextView.riscar(sim: Boolean) {
    paintFlags = if (sim) paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
    else paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
}
