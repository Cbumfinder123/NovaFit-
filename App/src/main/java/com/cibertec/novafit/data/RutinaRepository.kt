package com.cibertec.novafit.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.cibertec.novafit.model.Rutina

class RutinaRepository(context: Context) {
    private val dbHelper = DbHelper(context)

    fun guardarRutina(rutina: Rutina, hashPerfil: String) {
        dbHelper.writableDatabase.use { db ->
            val cv = ContentValues().apply {
                put("email", rutina.email)
                put("dias_semana", rutina.diasSemana)
                put("lugar", rutina.lugar)
                put("objetivo", rutina.objetivo)
                put("nivel_experiencia", rutina.nivelExperiencia)
                put("hash_perfil", hashPerfil)
                put("plan_semanal", rutina.planSemanal.joinToString("|") { dia ->
                    "${dia.nombre}~${dia.grupoMuscular}~" + dia.ejercicios.joinToString(";") { ej ->
                        "${ej.nombre},${ej.series},${ej.repeticiones},${ej.descanso},${ej.notas ?: ""}"
                    }
                })
                put("fecha_creacion", rutina.fechaCreacion)
            }
            db.insertWithOnConflict("rutinas", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
        }
    }

    fun obtenerRutina(email: String): Rutina? {
        dbHelper.readableDatabase.use { db ->
            val cursor = db.query("rutinas", null, "email=?", arrayOf(email), null, null, null)

            return if (cursor.moveToFirst()) {

                val planText = cursor.getString(cursor.getColumnIndexOrThrow("plan_semanal"))
                val diasSemana = cursor.getInt(cursor.getColumnIndexOrThrow("dias_semana"))
                val lugar = cursor.getString(cursor.getColumnIndexOrThrow("lugar"))
                val objetivo = cursor.getString(cursor.getColumnIndexOrThrow("objetivo"))
                val nivelExperiencia = cursor.getString(cursor.getColumnIndexOrThrow("nivel_experiencia"))
                val fechaCreacion = cursor.getLong(cursor.getColumnIndexOrThrow("fecha_creacion"))

                cursor.close()


                val sesiones = mutableListOf<Rutina.DiaSesion>()
                planText.split("|").forEach { diaStr ->
                    if (diaStr.isNotBlank()) {
                        val partes = diaStr.split("~")
                        if (partes.size >= 3) {
                            val nombre = partes[0]
                            val grupo = partes[1]
                            val ejerciciosStr = partes[2]

                            val ejercicios = ejerciciosStr.split(";").mapNotNull { ejStr ->
                                if (ejStr.isBlank()) return@mapNotNull null
                                val ejPartes = ejStr.split(",")
                                if (ejPartes.size < 4) return@mapNotNull null

                                Rutina.Ejercicio(
                                    nombre = ejPartes[0],
                                    series = ejPartes[1].toIntOrNull() ?: 3,
                                    repeticiones = ejPartes[2],
                                    descanso = ejPartes[3],
                                    notas = if (ejPartes.size > 4 && ejPartes[4].isNotEmpty()) ejPartes[4] else null
                                )
                            }

                            sesiones.add(Rutina.DiaSesion(nombre, grupo, ejercicios))
                        }
                    }
                }

                Rutina(
                    email = email,
                    diasSemana = diasSemana,
                    lugar = lugar,
                    objetivo = objetivo,
                    nivelExperiencia = nivelExperiencia,
                    planSemanal = sesiones,
                    fechaCreacion = fechaCreacion
                )
            } else {
                cursor.close()
                null
            }
        }
    }

    fun obtenerHashPerfil(email: String): String? {
        dbHelper.readableDatabase.use { db ->
            val cursor = db.query("rutinas", arrayOf("hash_perfil"), "email=?", arrayOf(email), null, null, null)

            return if (cursor.moveToFirst()) {
                val hash = cursor.getString(cursor.getColumnIndexOrThrow("hash_perfil"))
                cursor.close()
                hash
            } else {
                cursor.close()
                null
            }
        }
    }

    fun eliminarRutina(email: String) {
        dbHelper.writableDatabase.use { db ->
            db.delete("rutinas", "email=?", arrayOf(email))
        }
    }
}