package com.example.cotidiano

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.cotidiano.databinding.ActivityPrincipalBinding

class PrincipalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrincipalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.host_navegacao) as NavHostFragment
        val navController = navHostFragment.navController

        binding.navegacaoInferior.setupWithNavController(navController)

        // Esconde a barra de navegação inferior nas telas de login e cadastro
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.navegacaoInferior.visibility = when (destination.id) {
                R.id.loginFragment, R.id.cadastroFragment -> View.GONE
                else -> View.VISIBLE
            }
        }
    }
}