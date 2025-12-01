package com.cibertec.novafit.logic

import android.content.Context
import android.util.Log
import com.cibertec.novafit.data.AlimentoRepository
import com.cibertec.novafit.model.Alimento
import com.cibertec.novafit.model.Dieta
import com.cibertec.novafit.model.Perfil
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.roundToInt

object GeneradorDieta {

    private const val TAG = "GeneradorDieta"


    fun calcularMacrosExactos(perfil: Perfil): Dieta.Macros {

        val porcentajeGrasa = if (perfil.circunferenciaCuello != null && perfil.circunferenciaCintura != null) {
            if (perfil.genero == "Masculino") {
                val bf = 86.010 * log10(perfil.circunferenciaCintura - perfil.circunferenciaCuello) -
                        70.041 * log10(perfil.altura) + 36.76
                bf.coerceIn(5.0, 50.0)
            } else if (perfil.circunferenciaCadera != null) {
                val bf = 163.205 * log10(perfil.circunferenciaCintura + perfil.circunferenciaCadera - perfil.circunferenciaCuello) -
                        97.684 * log10(perfil.altura) - 78.387
                bf.coerceIn(10.0, 55.0)
            } else null
        } else null


        val masaMagra = if (porcentajeGrasa != null) {
            perfil.peso * (1 - porcentajeGrasa / 100.0)
        } else {
            if (perfil.genero == "Masculino") perfil.peso * 0.85 else perfil.peso * 0.75
        }


        val tmb = if (porcentajeGrasa != null) {
            370 + (21.6 * masaMagra)
        } else {
            if (perfil.genero == "Masculino") {
                (10 * perfil.peso) + (6.25 * perfil.altura) - (5 * perfil.edad) + 5
            } else {
                (10 * perfil.peso) + (6.25 * perfil.altura) - (5 * perfil.edad) - 161
            }
        }


        val factorBase = when (perfil.nivelActividad) {
            "Sedentario" -> 1.2
            "Ligero" -> 1.375
            "Moderado" -> 1.55
            "Activo" -> 1.725
            "Muy activo" -> 1.9
            else -> 1.2
        }

        val ajusteExperiencia = when (perfil.nivelExperiencia) {
            "Principiante" -> -0.05
            "Avanzado" -> 0.05
            else -> 0.0
        }

        val factorActividad = factorBase + ajusteExperiencia
        val tdee = (tmb * factorActividad).roundToInt()


        var caloriasObjetivo = when (perfil.objetivo) {
            "Perder grasa" -> tdee - 500
            "Ganar músculo" -> tdee + 300
            "Mantener" -> tdee
            else -> tdee
        }


        when (perfil.tipoCuerpo) {
            "Ectomorfo" -> if (perfil.objetivo == "Ganar músculo") caloriasObjetivo += 200
            "Endomorfo" -> if (perfil.objetivo == "Perder grasa") caloriasObjetivo -= 100
        }


        if (porcentajeGrasa != null) {
            when (perfil.objetivo) {
                "Perder grasa" -> {
                    if (porcentajeGrasa > 30) caloriasObjetivo -= 200
                    else if (porcentajeGrasa > 25) caloriasObjetivo -= 100
                }
                "Ganar músculo" -> {
                    if (porcentajeGrasa < 12) caloriasObjetivo -= 100
                }
            }
        }

        caloriasObjetivo = caloriasObjetivo.coerceAtLeast(1200)


        val proteinasPorKg = when {
            perfil.objetivo == "Ganar músculo" && perfil.nivelExperiencia == "Avanzado" -> 2.4
            perfil.objetivo == "Ganar músculo" -> 2.2
            perfil.objetivo == "Perder grasa" -> 2.5
            else -> 2.0
        }
        val proteinas = (masaMagra * proteinasPorKg).roundToInt()

        val grasasPorKg = when (perfil.tipoCuerpo) {
            "Ectomorfo" -> 0.9
            "Endomorfo" -> 0.7
            else -> 0.8
        }
        val grasas = (perfil.peso * grasasPorKg).roundToInt()

        val caloriasRestantes = caloriasObjetivo - (proteinas * 4 + grasas * 9)
        val carbohidratos = when (perfil.tipoCuerpo) {
            "Ectomorfo" -> ((caloriasRestantes / 4.0) * 1.1).roundToInt()
            "Endomorfo" -> ((caloriasRestantes / 4.0) * 0.85).roundToInt()
            else -> (caloriasRestantes / 4.0).roundToInt()
        }.coerceAtLeast(50)

        val caloriasReales = (proteinas * 4) + (grasas * 9) + (carbohidratos * 4)

        Log.d(TAG, "✅ Macros calculados: ${caloriasReales}kcal | P:${proteinas}g | G:${grasas}g | C:${carbohidratos}g")

        return Dieta.Macros(caloriasReales, proteinas, grasas, carbohidratos)
    }


    fun crear(
        context: Context,
        macros: Dieta.Macros,
        comidas: Int,
        pIds: List<Int>,
        gIds: List<Int>,
        cIds: List<Int>
    ): List<Dieta.Dia> {
        val alimentoRepo = AlimentoRepository(context)

        val proteinas = alimentoRepo.obtenerPorIds(pIds).filter { it.proteinas > 0 }
        val grasas = alimentoRepo.obtenerPorIds(gIds).filter { it.grasas > 0 }
        val carbs = alimentoRepo.obtenerPorIds(cIds).filter { it.carbohidratos > 0 }

        require(proteinas.isNotEmpty() && grasas.isNotEmpty() && carbs.isNotEmpty()) {
            "Debe haber al menos un alimento válido por categoría"
        }

        val nombresComidas = when (comidas) {
            2 -> listOf("Comida 1", "Comida 2")
            3 -> listOf("Desayuno", "Almuerzo", "Cena")
            4 -> listOf("Desayuno", "Snack", "Almuerzo", "Cena")
            5 -> listOf("Desayuno", "Snack 1", "Almuerzo", "Snack 2", "Cena")
            6 -> listOf("Desayuno", "Snack 1", "Almuerzo", "Snack 2", "Post-entreno", "Cena")
            else -> listOf("Desayuno", "Almuerzo", "Cena")
        }

        val macrosPorComida = macros.dividir(comidas)

        return (1..7).map { diaIndex ->
            val comidasDelDia = mutableListOf<Dieta.Comida>()
            var totalP = 0
            var totalG = 0
            var totalC = 0

            (0 until comidas).forEach { i ->
                val objetivo = macrosPorComida[i]


                var mejorComida: Dieta.Comida? = null
                var mejorPrecision = Double.MAX_VALUE

                repeat(5) { intento ->
                    val p = proteinas.random()
                    val g = grasas.random()
                    val c = carbs.random()


                    val resultado = calcularPorcionesOptimas(
                        objetivo,
                        p, g, c
                    )


                    val errorP = abs(resultado.macros.proteinas - objetivo.proteinas)
                    val errorG = abs(resultado.macros.grasas - objetivo.grasas)
                    val errorC = abs(resultado.macros.carbohidratos - objetivo.carbohidratos)
                    val errorTotal = errorP + errorG + errorC

                    if (errorTotal < mejorPrecision) {
                        mejorPrecision = errorTotal.toDouble()
                        mejorComida = resultado
                    }


                    if (errorTotal <= 5) return@repeat
                }


                val comidaFinal = mejorComida ?: calcularPorcionesOptimas(
                    objetivo,
                    proteinas.random(),
                    grasas.random(),
                    carbs.random()
                )

                totalP += comidaFinal.macros.proteinas
                totalG += comidaFinal.macros.grasas
                totalC += comidaFinal.macros.carbohidratos

                comidasDelDia.add(
                    Dieta.Comida(
                        nombresComidas[i],
                        hora(i),
                        comidaFinal.alimentos,
                        comidaFinal.macros
                    )
                )

                Log.d(TAG, "Comida ${i + 1}: ${comidaFinal.macros.calorias}kcal | " +
                        "P:${comidaFinal.macros.proteinas}g | " +
                        "G:${comidaFinal.macros.grasas}g | " +
                        "C:${comidaFinal.macros.carbohidratos}g " +
                        "(precisión: ±${mejorPrecision.roundToInt()}g)")
            }

            Log.d(TAG, "Día $diaIndex TOTAL: P:${totalP}/${macros.proteinas}g | " +
                    "G:${totalG}/${macros.grasas}g | C:${totalC}/${macros.carbohidratos}g")

            Dieta.Dia("Día $diaIndex", comidasDelDia)
        }
    }


    private fun calcularPorcionesOptimas(
        objetivo: Dieta.Macros,
        p: Alimento,
        g: Alimento,
        c: Alimento
    ): Dieta.Comida {

        var porcionP = calcularPorcionBase(objetivo.proteinas, p.proteinas)
        var porcionG = calcularPorcionBase(objetivo.grasas, g.grasas)
        var porcionC = calcularPorcionBase(objetivo.carbohidratos, c.carbohidratos)


        var macrosActuales = calcularMacrosTotales(porcionP, porcionG, porcionC, p, g, c)


        val iteraciones = 3
        repeat(iteraciones) {

            if (macrosActuales.proteinas > objetivo.proteinas * 1.15) {
                val exceso = macrosActuales.proteinas - objetivo.proteinas

                if (g.proteinas > 5) porcionG = (porcionG * 0.85).roundToInt()
                if (c.proteinas > 3) porcionC = (porcionC * 0.90).roundToInt()
            }


            if (macrosActuales.grasas > objetivo.grasas * 1.15) {
                val exceso = macrosActuales.grasas - objetivo.grasas
                if (p.grasas > 5) porcionP = (porcionP * 0.85).roundToInt()
                if (c.grasas > 3) porcionC = (porcionC * 0.90).roundToInt()
            }


            if (macrosActuales.carbohidratos > objetivo.carbohidratos * 1.15) {
                val exceso = macrosActuales.carbohidratos - objetivo.carbohidratos
                if (p.carbohidratos > 3) porcionP = (porcionP * 0.85).roundToInt()
                if (g.carbohidratos > 5) porcionG = (porcionG * 0.85).roundToInt()
            }


            porcionP = porcionP.coerceIn(10, 300)
            porcionG = porcionG.coerceIn(5, 100)
            porcionC = porcionC.coerceIn(10, 300)


            macrosActuales = calcularMacrosTotales(porcionP, porcionG, porcionC, p, g, c)
        }

        val alimentos = listOf(
            Dieta.AlimentoPorcion(p.nombre, "${porcionP}g"),
            Dieta.AlimentoPorcion(g.nombre, "${porcionG}g"),
            Dieta.AlimentoPorcion(c.nombre, "${porcionC}g")
        )

        return Dieta.Comida("", "", alimentos, macrosActuales)
    }

    private fun calcularPorcionBase(necesidad: Int, contenido: Double): Int {
        if (contenido <= 0) return 10
        val porcion = ((necesidad / contenido) * 100).roundToInt()
        return porcion.coerceIn(10, 300)
    }

    private fun calcularMacrosTotales(
        porcionP: Int,
        porcionG: Int,
        porcionC: Int,
        p: Alimento,
        g: Alimento,
        c: Alimento
    ): Dieta.Macros {
        val factorP = porcionP / 100.0
        val factorG = porcionG / 100.0
        val factorC = porcionC / 100.0

        return Dieta.Macros(
            calorias = (
                    (factorP * p.calorias) +
                            (factorG * g.calorias) +
                            (factorC * c.calorias)
                    ).roundToInt(),

            proteinas = (
                    (factorP * p.proteinas) +
                            (factorG * g.proteinas) +
                            (factorC * c.proteinas)
                    ).roundToInt(),

            grasas = (
                    (factorP * p.grasas) +
                            (factorG * g.grasas) +
                            (factorC * c.grasas)
                    ).roundToInt(),

            carbohidratos = (
                    (factorP * p.carbohidratos) +
                            (factorG * g.carbohidratos) +
                            (factorC * c.carbohidratos)
                    ).roundToInt()
        )
    }

    fun recalcularMacrosTotales(dieta: Dieta): Dieta.Macros {
        var totalCal = 0
        var totalP = 0
        var totalG = 0
        var totalC = 0

        dieta.planSemanal.forEach { dia ->
            dia.comidas.forEach { comida ->
                totalCal += comida.macros.calorias
                totalP += comida.macros.proteinas
                totalG += comida.macros.grasas
                totalC += comida.macros.carbohidratos
            }
        }

        return Dieta.Macros(totalCal / 7, totalP / 7, totalG / 7, totalC / 7)
    }

    private fun hora(i: Int) = listOf("08:00", "11:00", "14:00", "17:00", "19:00", "21:00").getOrElse(i) { "14:00" }

    private fun Dieta.Macros.dividir(n: Int): List<Dieta.Macros> {
        val baseCal = calorias / n
        val baseP = proteinas / n
        val baseG = grasas / n
        val baseC = carbohidratos / n

        return List(n) { index ->
            if (index == n - 1) {
                Dieta.Macros(
                    calorias - (baseCal * (n - 1)),
                    proteinas - (baseP * (n - 1)),
                    grasas - (baseG * (n - 1)),
                    carbohidratos - (baseC * (n - 1))
                )
            } else {
                Dieta.Macros(baseCal, baseP, baseG, baseC)
            }
        }
    }
}