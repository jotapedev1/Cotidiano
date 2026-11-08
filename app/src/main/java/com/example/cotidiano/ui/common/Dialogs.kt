package com.example.cotidiano.ui.common

import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/** MaterialDatePicker devolve meia-noite UTC, então formatamos em UTC. */
fun formatarData(millis: Long): String =
    SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
        .apply { timeZone = TimeZone.getTimeZone("UTC") }
        .format(millis)

fun Fragment.pedirTexto(titulo: String, dica: String, aoConfirmar: (String) -> Unit) {
    val input = EditText(requireContext()).apply { hint = dica; setSingleLine() }
    AlertDialog.Builder(requireContext())
        .setTitle(titulo)
        .setView(input)
        .setPositiveButton("Adicionar") { _, _ ->
            val txt = input.text.toString().trim()
            if (txt.isNotEmpty()) aoConfirmar(txt)
        }
        .setNegativeButton("Cancelar", null)
        .show()
}

/** Feedback provisório de XP; a recompensa com animação entra no Sprint 3. */
fun View.mostrarXp(delta: Int) {
    if (delta > 0) Snackbar.make(this, "+$delta XP", Snackbar.LENGTH_SHORT).show()
}