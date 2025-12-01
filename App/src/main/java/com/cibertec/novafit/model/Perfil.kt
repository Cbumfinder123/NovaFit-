package com.cibertec.novafit.model

data class Perfil(
    val id: Int = 0,
    val email: String,
    val password: String,
    val nombre: String,
    val fechaNacimiento: Long,
    val edad: Int,
    val genero: String,
    val peso: Double,
    val altura: Double,
    val circunferenciaCuello: Double? = null,
    val circunferenciaCintura: Double? = null,
    val circunferenciaCadera: Double? = null,
    val nivelActividad: String? = null,
    val objetivo: String,
    val tipoCuerpo: String? = null,
    val nivelExperiencia: String? = null
)