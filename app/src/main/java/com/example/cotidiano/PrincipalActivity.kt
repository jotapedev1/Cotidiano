package com.example.cotidiano

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.cotidiano.dados.Repositorio
import com.example.cotidiano.databinding.ActivityPrincipalBinding
import com.example.cotidiano.dominio.CalculadoraXp
import kotlinx.coroutines.launch

/** Única Activity do app: mostra a barra de XP, o menu inferior e hospeda as telas (fragments). */
class PrincipalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrincipalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        // Liga o menu inferior ao grafo de navegação
        val navHost = supportFragmentManager.findFragmentById(R.id.host_navegacao) as NavHostFragment
        val navController = navHost.navController
        binding.navegacaoInferior.setupWithNavController(navController)

        // Barra de XP: atualiza sempre que o XP do usuário logado muda
        lifecycleScope.launch {
            Repositorio.obter(this@PrincipalActivity).xp.collect { xp ->
                val total = xp ?: 0
                binding.textNivel.text = getString(R.string.nivel_texto, CalculadoraXp.nivel(total))
                binding.textXpDetalhe.text = getString(
                    R.string.xp_detalhe,
                    CalculadoraXp.xpAtualNoNivel(total),
                    CalculadoraXp.xpNecessarioParaProximoNivel(total)
                )
                binding.progressXp.progress = CalculadoraXp.progressoNoNivel(total)
            }
        }

        // Esconde barra superior, XP e menu nas telas de login e cadastro
        navController.addOnDestinationChangedListener { _, destino, _ ->
            val mostrar = destino.id != R.id.loginFragment && destino.id != R.id.cadastroFragment
            binding.toolbar.isVisible = mostrar
            binding.layoutXpBar.isVisible = mostrar
            binding.navegacaoInferior.isVisible = mostrar
            supportActionBar?.title = destino.label
        }
    }
}
