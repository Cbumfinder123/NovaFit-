package com.cibertec.novafit.model

data class Dieta(
    val email: String,
    val numeroComidas: Int,
    val proteinasIds: List<Int>,
    val grasasIds: List<Int>,
    val carbohidratosIds: List<Int>,
    val planSemanal: List<Dia>,
    val macros: Macros,
    val fechaCreacion: Long
) {
    data class Dia(
        val nombre: String,
        val comidas: List<Comida>
    )

    data class Comida(
        val nombre: String,
        val hora: String,
        val alimentos: List<AlimentoPorcion>,
        val macros: Macros
    )

    data class AlimentoPorcion(
        val nombre: String,
        val porcion: String
    )

    data class Macros(
        val calorias: Int,
        val proteinas: Int,
        val grasas: Int,
        val carbohidratos: Int
    )
}