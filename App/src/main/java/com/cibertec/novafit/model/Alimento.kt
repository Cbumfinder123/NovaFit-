package com.cibertec.novafit.model

data class Alimento(
    val id: Int = 0,
    val nombre: String,
    val categoria: String,
    val proteinas: Double,
    val grasas: Double,
    val carbohidratos: Double,
    val calorias: Double,
    val activo: Boolean = true,
    val fechaCreacion: Long = System.currentTimeMillis()
) {
    companion object {
        const val CATEGORIA_PROTEINA = "proteina"
        const val CATEGORIA_GRASA = "grasa"
        const val CATEGORIA_CARBOHIDRATO = "carbohidrato"
    }


    fun esValido(): Boolean {
        return nombre.isNotBlank() &&
                categoria in listOf(CATEGORIA_PROTEINA, CATEGORIA_GRASA, CATEGORIA_CARBOHIDRATO) &&
                proteinas >= 0 &&
                grasas >= 0 &&
                carbohidratos >= 0 &&
                calorias >= 0
    }


    fun tieneMacrosPrincipales(): Boolean {
        return when (categoria) {
            CATEGORIA_PROTEINA -> proteinas >= 10.0
            CATEGORIA_GRASA -> grasas >= 5.0
            CATEGORIA_CARBOHIDRATO -> carbohidratos >= 15.0
            else -> false
        }
    }
}