package com.example.cotidiano.ui.comum

import android.view.View
import android.widget.EditText
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.cotidiano.R
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/** MaterialDatePicker devolve meia-noite UTC, então formatamos em UTC. */
fun formatarData(millis: Long): String =
    SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
        .apply { timeZone = TimeZone.getTimeZone("UTC") }
        .format(millis)

fun Fragment.pedirTexto(
    @StringRes titulo: Int,
    @StringRes dica: Int,
    aoConfirmar: (String) -> Unit
) {
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

/** Feedback provisório de XP; a recompensa com animação entra no Sprint 3. */
fun View.mostrarXp(delta: Int) {
    if (delta > 0) Snackbar.make(this, context.getString(R.string.xp_ganho, delta), Snackbar.LENGTH_SHORT).show()
}