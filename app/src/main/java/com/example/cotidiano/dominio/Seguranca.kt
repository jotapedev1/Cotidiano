package com.example.cotidiano.dominio

import java.security.MessageDigest

/** Guardamos só o hash da senha, nunca a senha em texto puro. */
object Seguranca {
    // SHA-256 transforma a senha em um texto fixo que não tem volta.
    // Em produção usaríamos também "salt" (bcrypt/PBKDF2); aqui mantemos simples.
    fun hash(senha: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(senha.toByteArray())
            .joinToString("") { "%02x".format(it) }
}
