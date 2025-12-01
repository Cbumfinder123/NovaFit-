package com.cibertec.novafit.firebase

import android.content.Context
import android.util.Log
import com.cibertec.novafit.data.AlimentoRepository
import com.cibertec.novafit.data.EjercicioRepository
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await


class MigracionFirebase(private val context: Context) {

    private val firestore = FirebaseConfig.firestore
    private val alimentoRepo = AlimentoRepository(context)
    private val ejercicioRepo = EjercicioRepository(context)

    companion object {
        private const val TAG = "MigracionFirebase"
    }


    suspend fun migrarTodo(): ResultadoMigracion {
        Log.d(TAG, "🚀 Iniciando migración SQLite → Firestore...")

        return try {
            val alimentosMigrados = migrarAlimentos()
            val ejerciciosMigrados = migrarEjercicios()

            ResultadoMigracion(
                exitoso = true,
                alimentosMigrados = alimentosMigrados,
                ejerciciosMigrados = ejerciciosMigrados,
                mensaje = "✅ Migración completada"
            )
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error en migración: ${e.message}", e)
            ResultadoMigracion(
                exitoso = false,
                mensaje = "❌ Error: ${e.message}"
            )
        }
    }


    private suspend fun migrarAlimentos(): Int {
        Log.d(TAG, "📥 Migrando alimentos...")

        val alimentos = alimentoRepo.obtenerTodos()
        Log.d(TAG, "📦 Alimentos en SQLite: ${alimentos.size}")

        var contador = 0

        alimentos.forEach { alimento ->
            try {
                val alimentoData = hashMapOf(
                    "nombre" to alimento.nombre,
                    "categoria" to alimento.categoria,  // ✅ Singular
                    "proteinas" to alimento.proteinas,
                    "grasas" to alimento.grasas,
                    "carbohidratos" to alimento.carbohidratos,
                    "calorias" to alimento.calorias,
                    "activo" to true,
                    "fechaCreacion" to FieldValue.serverTimestamp()
                )

                firestore.collection(FirebaseConfig.Collections.ALIMENTOS)
                    .add(alimentoData)
                    .await()

                contador++
                Log.d(TAG, "✅ Migrado: ${alimento.nombre}")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error migrando ${alimento.nombre}: ${e.message}")
            }
        }

        Log.d(TAG, "✅ Total migrados: $contador de ${alimentos.size}")
        return contador
    }


    private suspend fun migrarEjercicios(): Int {
        Log.d(TAG, "📥 Migrando ejercicios...")

        val ejercicios = ejercicioRepo.obtenerTodos()
        Log.d(TAG, "📦 Ejercicios en SQLite: ${ejercicios.size}")

        var contador = 0

        ejercicios.forEach { ejercicio ->
            try {
                val ejercicioData = hashMapOf(
                    "nombre" to ejercicio.nombre,
                    "grupoMuscular" to ejercicio.grupoMuscular,
                    "dificultad" to ejercicio.dificultad,
                    "equipamiento" to ejercicio.equipamiento,
                    "descripcion" to (ejercicio.descripcion ?: ""),
                    "tieneImagen" to false,
                    "imagenUrl" to "",
                    "activo" to true,
                    "fechaCreacion" to FieldValue.serverTimestamp()
                )

                firestore.collection(FirebaseConfig.Collections.EJERCICIOS)
                    .add(ejercicioData)
                    .await()

                contador++
                Log.d(TAG, "✅ Migrado: ${ejercicio.nombre}")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error migrando ${ejercicio.nombre}: ${e.message}")
            }
        }

        Log.d(TAG, "✅ Total migrados: $contador de ${ejercicios.size}")
        return contador
    }

    data class ResultadoMigracion(
        val exitoso: Boolean,
        val alimentosMigrados: Int = 0,
        val ejerciciosMigrados: Int = 0,
        val mensaje: String
    )
}