package com.cibertec.novafit.model

data class HistorialEntrenamiento(
    val id: Int = 0,
    val email: String,
    val fecha: Long,
    val diaRutina: String,
    val ejercicioNombre: String,
    val seriesPlanificadas: Int,
    val seriesCompletadas: Int,
    val pesoUsado: Double,
    val repeticiones: List<Int>,
    val duracionSegundos: Int
)

data class RegistroSerie(
    val numeroSerie: Int,
    val peso: Double,
    val repeticiones: Int
)