package com.cibertec.novafit.model

data class Rutina(
    val email: String,
    val diasSemana: Int,
    val lugar: String,
    val objetivo: String,
    val nivelExperiencia: String,
    val planSemanal: List<DiaSesion>,
    val fechaCreacion: Long
) {
    data class DiaSesion(
        val nombre: String,
        val grupoMuscular: String,
        val ejercicios: List<Ejercicio>
    )

    data class Ejercicio(
        val nombre: String,
        val series: Int,
        val repeticiones: String,
        val descanso: String,
        val notas: String?
    )
}