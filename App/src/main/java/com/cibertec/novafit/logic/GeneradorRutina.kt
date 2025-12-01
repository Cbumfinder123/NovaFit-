package com.cibertec.novafit.logic

import android.content.Context
import com.cibertec.novafit.data.EjercicioRepository
import com.cibertec.novafit.model.Ejercicio
import com.cibertec.novafit.model.Perfil
import com.cibertec.novafit.model.Rutina

object GeneradorRutina {


    fun generar(context: Context, perfil: Perfil, diasSemana: Int, lugar: String): Rutina {
        val dias = diasSemana.coerceIn(3, 7)

        val objetivo = perfil.objetivo
        val nivelExperiencia = perfil.nivelExperiencia ?: "Intermedio"


        val ejercicioRepo = EjercicioRepository(context)
        val ejerciciosDisponibles = ejercicioRepo.obtenerPorNivelYLugar(nivelExperiencia, lugar)

        if (ejerciciosDisponibles.isEmpty()) {
            throw IllegalStateException("No hay ejercicios disponibles para lugar: $lugar y nivel: $nivelExperiencia")
        }

        val split = determinarSplit(dias)

        val sesiones = split.map { dia ->
            generarSesion(dia, ejerciciosDisponibles, perfil)
        }

        return Rutina(
            email = perfil.email,
            diasSemana = dias,
            lugar = lugar,
            objetivo = objetivo,
            nivelExperiencia = nivelExperiencia,
            planSemanal = sesiones,
            fechaCreacion = System.currentTimeMillis()
        )
    }


    private fun determinarSplit(dias: Int): List<DiaConfig> {
        return when (dias) {
            3 -> split3Dias()
            4 -> split4Dias()
            5 -> split5Dias()
            6 -> split6Dias()
            7 -> split7Dias()
            else -> split3Dias()
        }
    }

    private data class DiaConfig(
        val nombre: String,
        val gruposMusculares: List<String>
    )

    private fun split3Dias() = listOf(
        DiaConfig("Día 1 - Full Body A", listOf("Pecho", "Espalda", "Hombros", "Core")),
        DiaConfig("Día 2 - Piernas", listOf("Piernas", "Core")),
        DiaConfig("Día 3 - Full Body B", listOf("Pecho", "Espalda", "Bíceps", "Tríceps", "Core"))
    )

    private fun split4Dias() = listOf(
        DiaConfig("Día 1 - Pecho/Tríceps", listOf("Pecho", "Tríceps", "Core")),
        DiaConfig("Día 2 - Piernas", listOf("Piernas", "Core")),
        DiaConfig("Día 3 - Espalda/Bíceps", listOf("Espalda", "Bíceps", "Core")),
        DiaConfig("Día 4 - Hombros", listOf("Hombros", "Core"))
    )

    private fun split5Dias() = listOf(
        DiaConfig("Día 1 - Pecho", listOf("Pecho", "Core")),
        DiaConfig("Día 2 - Espalda", listOf("Espalda", "Core")),
        DiaConfig("Día 3 - Piernas", listOf("Piernas", "Core")),
        DiaConfig("Día 4 - Hombros", listOf("Hombros", "Core")),
        DiaConfig("Día 5 - Brazos", listOf("Bíceps", "Tríceps", "Core"))
    )

    private fun split6Dias() = listOf(
        DiaConfig("Día 1 - Pecho/Tríceps", listOf("Pecho", "Tríceps")),
        DiaConfig("Día 2 - Espalda/Bíceps", listOf("Espalda", "Bíceps")),
        DiaConfig("Día 3 - Piernas", listOf("Piernas")),
        DiaConfig("Día 4 - Pecho/Hombros", listOf("Pecho", "Hombros")),
        DiaConfig("Día 5 - Espalda/Core", listOf("Espalda", "Core")),
        DiaConfig("Día 6 - Piernas/Brazos", listOf("Piernas", "Bíceps", "Tríceps"))
    )

    private fun split7Dias() = listOf(
        DiaConfig("Día 1 - Pecho", listOf("Pecho")),
        DiaConfig("Día 2 - Espalda", listOf("Espalda")),
        DiaConfig("Día 3 - Piernas", listOf("Piernas")),
        DiaConfig("Día 4 - Hombros", listOf("Hombros")),
        DiaConfig("Día 5 - Brazos", listOf("Bíceps", "Tríceps")),
        DiaConfig("Día 6 - Piernas/Glúteos", listOf("Piernas")),
        DiaConfig("Día 7 - Core/Cardio", listOf("Core"))
    )


    private fun generarSesion(
        diaConfig: DiaConfig,
        ejerciciosDisponibles: List<Ejercicio>,
        perfil: Perfil
    ): Rutina.DiaSesion {
        val ejercicios = mutableListOf<Rutina.Ejercicio>()

        diaConfig.gruposMusculares.forEach { grupo ->
            val ejerciciosGrupo = ejerciciosDisponibles
                .filter { it.grupoMuscular == grupo }
                .shuffled()
                .take(obtenerCantidadEjercicios(grupo, perfil.nivelExperiencia))

            ejerciciosGrupo.forEach { ej ->
                ejercicios.add(convertirAEjercicio(ej, perfil))
            }
        }

        return Rutina.DiaSesion(
            nombre = diaConfig.nombre,
            grupoMuscular = diaConfig.gruposMusculares.joinToString(", "),
            ejercicios = ejercicios
        )
    }


    private fun obtenerCantidadEjercicios(grupo: String, experiencia: String?): Int {
        val nivel = experiencia ?: "Intermedio"

        return when (grupo) {
            "Piernas" -> when (nivel) {
                "Principiante" -> 4
                "Intermedio" -> 5
                "Avanzado" -> 6
                else -> 4
            }
            "Pecho", "Espalda" -> when (nivel) {
                "Principiante" -> 3
                "Intermedio" -> 4
                "Avanzado" -> 5
                else -> 3
            }
            "Core" -> 2
            else -> when (nivel) {
                "Principiante" -> 2
                "Intermedio" -> 3
                "Avanzado" -> 4
                else -> 2
            }
        }
    }


    private fun convertirAEjercicio(
        ejercicio: Ejercicio,
        perfil: Perfil
    ): Rutina.Ejercicio {
        val nivelExperiencia = perfil.nivelExperiencia ?: "Intermedio"
        val objetivo = perfil.objetivo

        val series = when (nivelExperiencia) {
            "Principiante" -> 3
            "Intermedio" -> 4
            "Avanzado" -> 4
            else -> 3
        }

        val repeticiones = when (objetivo) {
            "Perder grasa" -> "12-15"
            "Ganar músculo" -> "8-12"
            "Mantener" -> "10-12"
            else -> "10-12"
        }

        val descanso = when (objetivo) {
            "Perder grasa" -> "45 seg"
            "Ganar músculo" -> "90 seg"
            "Mantener" -> "60 seg"
            else -> "60 seg"
        }

        return Rutina.Ejercicio(
            nombre = ejercicio.nombre,
            series = series,
            repeticiones = repeticiones,
            descanso = descanso,
            notas = obtenerNota(ejercicio, perfil)
        )
    }


    private fun obtenerNota(
        ejercicio: Ejercicio,
        perfil: Perfil
    ): String? {
        val nivelExperiencia = perfil.nivelExperiencia ?: "Intermedio"

        return when {
            nivelExperiencia == "Principiante" && ejercicio.dificultad == "Avanzado" ->
                "Reduce el peso y enfócate en la técnica"
            ejercicio.nombre.contains("Peso muerto", ignoreCase = true) ->
                "Mantén la espalda recta en todo momento"
            ejercicio.nombre.contains("Sentadilla", ignoreCase = true) ->
                "Rodillas alineadas con los pies"
            ejercicio.nombre.contains("Press", ignoreCase = true) ->
                "Control en la bajada, explosivo en la subida"
            ejercicio.nombre.contains("Dominadas", ignoreCase = true) ->
                "Escápulas retraídas antes de subir"
            ejercicio.grupoMuscular == "Core" && ejercicio.nombre.contains("Plancha", ignoreCase = true) ->
                "Mantén el abdomen contraído y la espalda neutral"
            else -> ejercicio.descripcion
        }
    }
}