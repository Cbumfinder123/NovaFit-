package com.cibertec.novafit.model

import android.content.Context

data class Ejercicio(
    val id: Int = 0,
    val nombre: String,
    val grupoMuscular: String,
    val dificultad: String,
    val equipamiento: String,
    val descripcion: String? = null,
    val imagenResource: String? = null,  // ✅ URL o drawable local
    val activo: Boolean = true,
    val fechaCreacion: Long = System.currentTimeMillis()
) {
    companion object {
        val GRUPOS_MUSCULARES = listOf(
            "Pecho", "Espalda", "Piernas", "Hombros",
            "Bíceps", "Tríceps", "Core"
        )

        const val DIFICULTAD_PRINCIPIANTE = "Principiante"
        const val DIFICULTAD_INTERMEDIO = "Intermedio"
        const val DIFICULTAD_AVANZADO = "Avanzado"

        const val EQUIPAMIENTO_NINGUNO = "ninguno"
        const val EQUIPAMIENTO_BASICO = "basico"
        const val EQUIPAMIENTO_GYM = "gym"
    }


    fun esUrl(): Boolean {
        return imagenResource?.startsWith("http", ignoreCase = true) == true
    }


    fun obtenerImagenResourceId(context: Context): Int {

        if (esUrl()) {
            return android.R.drawable.ic_menu_help
        }


        if (imagenResource.isNullOrEmpty()) {
            return android.R.drawable.ic_menu_help
        }


        val resId = context.resources.getIdentifier(
            imagenResource,
            "drawable",
            context.packageName
        )

        return if (resId != 0) resId else android.R.drawable.ic_menu_help
    }


    fun obtenerImagenUrl(): String? {
        return if (esUrl()) imagenResource else null
    }

    fun esValido(): Boolean {
        return nombre.isNotBlank() &&
                grupoMuscular in GRUPOS_MUSCULARES &&
                dificultad in listOf(DIFICULTAD_PRINCIPIANTE, DIFICULTAD_INTERMEDIO, DIFICULTAD_AVANZADO) &&
                equipamiento in listOf(EQUIPAMIENTO_NINGUNO, EQUIPAMIENTO_BASICO, EQUIPAMIENTO_GYM)
    }

    fun esApropiado(nivelExperiencia: String?): Boolean {
        return when (nivelExperiencia) {
            "Principiante" -> dificultad != DIFICULTAD_AVANZADO
            "Intermedio" -> true
            "Avanzado" -> true
            else -> dificultad == DIFICULTAD_PRINCIPIANTE
        }
    }
}