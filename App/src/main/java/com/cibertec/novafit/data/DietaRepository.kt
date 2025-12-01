package com.cibertec.novafit.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.cibertec.novafit.model.Dieta

class DietaRepository(context: Context) {
    private val dbHelper = DbHelper(context)

    fun guardarDieta(dieta: Dieta) {
        dbHelper.writableDatabase.use { db ->
            val cv = ContentValues().apply {
                put("email", dieta.email)
                put("numero_comidas", dieta.numeroComidas)
                put("proteinas_ids", dieta.proteinasIds.joinToString(","))
                put("grasas_ids", dieta.grasasIds.joinToString(","))
                put("carbohidratos_ids", dieta.carbohidratosIds.joinToString(","))
                put("plan_semanal", dieta.planSemanal.joinToString("|") { dia ->
                    dia.comidas.joinToString(";") { comida ->
                        "${comida.nombre},${comida.hora},${comida.macros.calorias},${comida.macros.proteinas},${comida.macros.grasas},${comida.macros.carbohidratos}," +
                                comida.alimentos.joinToString("#") { "${it.nombre}:${it.porcion}" }
                    }
                })
                put("calorias", dieta.macros.calorias)
                put("proteinas", dieta.macros.proteinas)
                put("grasas", dieta.macros.grasas)
                put("carbohidratos", dieta.macros.carbohidratos)
                put("fecha_creacion", dieta.fechaCreacion)
            }
            db.insertWithOnConflict("dietas", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
        }
    }

    fun obtenerDieta(email: String): Dieta? {
        dbHelper.readableDatabase.use { db ->
            val cursor = db.query("dietas", null, "email=?", arrayOf(email), null, null, null)
            if (cursor.moveToFirst()) {
                val planText = cursor.getString(cursor.getColumnIndexOrThrow("plan_semanal"))
                val dias = mutableListOf<Dieta.Dia>()
                var diaIndex = 1

                planText.split("|").forEach { diaStr ->
                    if (diaStr.isNotBlank()) {
                        val comidas = diaStr.split(";").mapNotNull { comidaStr ->
                            if (comidaStr.isBlank()) return@mapNotNull null
                            val parts = comidaStr.split(",", limit = 7)
                            if (parts.size < 7) return@mapNotNull null
                            val alimentosStr = parts[6]
                            val alimentos = alimentosStr.split("#").mapNotNull {
                                if (it.contains(":")) {
                                    val (nombre, porcion) = it.split(":", limit = 2)
                                    Dieta.AlimentoPorcion(nombre, porcion)
                                } else null
                            }
                            Dieta.Comida(
                                nombre = parts[0],
                                hora = parts[1],
                                alimentos = alimentos,
                                macros = Dieta.Macros(
                                    calorias = parts[2].toIntOrNull() ?: 0,
                                    proteinas = parts[3].toIntOrNull() ?: 0,
                                    grasas = parts[4].toIntOrNull() ?: 0,
                                    carbohidratos = parts[5].toIntOrNull() ?: 0
                                )
                            )
                        }
                        dias.add(Dieta.Dia("Día $diaIndex", comidas.toMutableList()))
                        diaIndex++
                    }
                }

                return Dieta(
                    email = email,
                    numeroComidas = cursor.getInt(cursor.getColumnIndexOrThrow("numero_comidas")),
                    proteinasIds = cursor.getString(cursor.getColumnIndexOrThrow("proteinas_ids")).split(",").mapNotNull { it.toIntOrNull() },
                    grasasIds = cursor.getString(cursor.getColumnIndexOrThrow("grasas_ids")).split(",").mapNotNull { it.toIntOrNull() },
                    carbohidratosIds = cursor.getString(cursor.getColumnIndexOrThrow("carbohidratos_ids")).split(",").mapNotNull { it.toIntOrNull() },
                    planSemanal = dias,
                    macros = Dieta.Macros(
                        cursor.getInt(cursor.getColumnIndexOrThrow("calorias")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("proteinas")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("grasas")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("carbohidratos"))
                    ),
                    fechaCreacion = cursor.getLong(cursor.getColumnIndexOrThrow("fecha_creacion"))
                )
            }
            cursor.close()
        }
        return null
    }

    fun eliminarDieta(email: String) {
        dbHelper.writableDatabase.use { db ->
            db.delete("dietas", "email=?", arrayOf(email))
        }
    }
}