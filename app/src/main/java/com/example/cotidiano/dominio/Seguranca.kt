package com.example.cotidiano.dominio

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object Seguranca {
    private const val ITERACOES = 10_000
    private val random = SecureRandom()

    fun novoSalt(): String = ByteArray(16).also { random.nextBytes(it) }.paraHex()

    fun hash(senha: String, salt: String): String {
        val spec = PBEKeySpec(senha.toCharArray(), salt.hexParaBytes(), ITERACOES, 256)
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            .generateSecret(spec).encoded.paraHex()
    }

    fun verificar(senha: String, salt: String, hashEsperado: String): Boolean =
        MessageDigest.isEqual(hash(senha, salt).toByteArray(), hashEsperado.toByteArray())

    private fun ByteArray.paraHex() = joinToString("") { "%02x".format(it) }
    private fun String.hexParaBytes() =
        ByteArray(length / 2) { substring(it * 2, it * 2 + 2).toInt(16).toByte() }
}