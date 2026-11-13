package com.example.cotidiano

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.cotidiano.dados.Repositorio
import com.example.cotidiano.dados.local.Progresso
import com.example.cotidiano.databinding.ActivityPrincipalBinding
import com.example.cotidiano.dominio.CalculadoraXp
import kotlinx.coroutines.launch

class PrincipalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrincipalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.host_navegacao) as NavHostFragment
        val navController = navHostFragment.navController

        binding.navegacaoInferior.setupWithNavController(navController)

        val repositorio = Repositorio.obter(this)

        lifecycleScope.launch {
            repositorio.progresso.collect { progresso: Progresso? ->
                val xpTotal = progresso?.xpTotal ?: 0
                val nivel = CalculadoraXp.nivel(xpTotal)
                val xpAtual = CalculadoraXp.xpAtualNoNivel(xpTotal)
                val xpNecessario = CalculadoraXp.xpNecessarioParaProximoNivel(xpTotal)
                val percentual = CalculadoraXp.progressoNoNivel(xpTotal)

                binding.textNivel.text = "Nível $nivel"
                binding.textXpDetalhe.text = "$xpAtual / $xpNecessario XP"
                binding.progressXp.progress = percentual
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val showNavigation = when (destination.id) {
                R.id.loginFragment, R.id.cadastroFragment -> false
                else -> true
            }
            binding.toolbar.visibility = if (showNavigation) View.VISIBLE else View.GONE
            binding.layoutXpBar.visibility = if (showNavigation) View.VISIBLE else View.GONE
            binding.navegacaoInferior.visibility = if (showNavigation) View.VISIBLE else View.GONE
            supportActionBar?.title = destination.label
        }
    }
}
