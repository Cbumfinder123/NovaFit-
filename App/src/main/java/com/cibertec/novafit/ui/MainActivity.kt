package com.cibertec.novafit.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.cibertec.novafit.R
import com.cibertec.novafit.data.PerfilRepository
import com.cibertec.novafit.databinding.ActivityMainBinding
import com.cibertec.novafit.firebase.FirebaseSyncService
import com.cibertec.novafit.firebase.MigracionFirebase
import com.cibertec.novafit.firebase.FirebaseConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var repo: PerfilRepository
    var emailActual: String = ""

    private lateinit var firebaseSync: FirebaseSyncService
    private lateinit var migracion: MigracionFirebase

    private lateinit var prefs: SharedPreferences

    companion object {
        private const val TAG = "MainActivity"
        private const val PREF_NAME = "novafit_prefs"
        private const val KEY_MIGRATION_DONE = "migration_completed"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repo = PerfilRepository(this)
        prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        firebaseSync = FirebaseSyncService(this)
        migracion = MigracionFirebase(this)


        sincronizarSilenciosamente()

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_perfil -> {
                    openFragment(MenuFragment())
                    true
                }
                R.id.nav_config -> {
                    openFragment(PerfilFragment())
                    true
                }
                else -> false
            }
        }

        ocultarBottomNav()
        openFragment(BienvenidaFragment())
    }


    private fun sincronizarSilenciosamente() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val migrationDone = prefs.getBoolean(KEY_MIGRATION_DONE, false)

                if (migrationDone) {

                    Log.d(TAG, "✅ Sincronización automática iniciada")
                    firebaseSync.iniciarListeners()
                    firebaseSync.sincronizarTodo()
                    return@launch
                }


                val alimentosCount = FirebaseConfig.firestore
                    .collection(FirebaseConfig.Collections.ALIMENTOS)
                    .limit(1)
                    .get()
                    .await()
                    .size()

                if (alimentosCount > 0) {

                    prefs.edit().putBoolean(KEY_MIGRATION_DONE, true).apply()
                    Log.d(TAG, "✅ Datos detectados en Firebase, sincronizando...")
                    firebaseSync.iniciarListeners()
                    firebaseSync.sincronizarTodo()
                } else {

                    Log.d(TAG, "🚀 Primera sincronización, migrando datos...")
                    migrarDatosSilenciosamente()
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error en sincronización: ${e.message}")

            }
        }
    }


    private suspend fun migrarDatosSilenciosamente() {
        try {
            firebaseSync.desactivarSync()
            kotlinx.coroutines.delay(1000)

            val resultado = migracion.migrarTodo()

            if (resultado.exitoso) {
                Log.d(TAG, "✅ Migración completada: ${resultado.alimentosMigrados} alimentos, ${resultado.ejerciciosMigrados} ejercicios")
                prefs.edit().putBoolean(KEY_MIGRATION_DONE, true).apply()

                firebaseSync.reactivarSync()
                firebaseSync.iniciarListeners()
                firebaseSync.sincronizarTodo()
            } else {
                Log.e(TAG, "❌ Error en migración: ${resultado.mensaje}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error crítico: ${e.message}")
        }
    }


    fun openFragment(f: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, f)
            .addToBackStack(null)
            .commit()
    }

    fun mostrarMenuPrincipal() {
        mostrarBottomNav()
        openFragment(MenuFragment())
    }

    fun volverAOnboarding() {
        ocultarBottomNav()
        emailActual = ""
        supportFragmentManager.popBackStack(
            null,
            androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        openFragment(BienvenidaFragment())
    }

    private fun ocultarBottomNav() {
        binding.bottomNav.visibility = android.view.View.GONE
    }

    private fun mostrarBottomNav() {
        binding.bottomNav.visibility = android.view.View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        firebaseSync.cleanup()
        Log.d(TAG, "🛑 MainActivity destruida")
    }
}