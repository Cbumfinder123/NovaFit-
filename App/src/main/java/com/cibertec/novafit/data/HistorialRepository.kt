package com.cibertec.novafit.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.cibertec.novafit.model.HistorialEntrenamiento
import java.text.SimpleDateFormat
import java.util.*

class HistorialRepository(context: Context) {
    private val dbHelper = DbHelper(context)

    fun guardarEntrenamiento(historial: HistorialEntrenamiento) {
        dbHelper.writableDatabase.use { db ->
            val cv = ContentValues().apply {
                put("email", historial.email)
                put("fecha", historial.fecha)
                put("dia_rutina", historial.diaRutina)
                put("ejercicio_nombre", historial.ejercicioNombre)
                put("series_planificadas", historial.seriesPlanificadas)
                put("series_completadas", historial.seriesCompletadas)
                put("peso_usado", historial.pesoUsado)
                put("repeticiones", historial.repeticiones.joinToString(","))
                put("duracion_segundos", historial.duracionSegundos)
            }
            db.insertWithOnConflict("historial_entrenamientos", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
        }
    }

    fun obtenerHistorialPorEmail(email: String, limite: Int = 10): List<HistorialEntrenamiento> {
        val historial = mutableListOf<HistorialEntrenamiento>()

        dbHelper.readableDatabase.use { db ->
            val cursor = db.query(
                "historial_entrenamientos",
                null,
                "email=?",
                arrayOf(email),
                null,
                null,
                "fecha DESC",
                limite.toString()
            )

            while (cursor.moveToNext()) {
                historial.add(
                    HistorialEntrenamiento(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        email = cursor.getString(cursor.getColumnIndexOrThrow("email")),
                        fecha = cursor.getLong(cursor.getColumnIndexOrThrow("fecha")),
                        diaRutina = cursor.getString(cursor.getColumnIndexOrThrow("dia_rutina")),
                        ejercicioNombre = cursor.getString(cursor.getColumnIndexOrThrow("ejercicio_nombre")),
                        seriesPlanificadas = cursor.getInt(cursor.getColumnIndexOrThrow("series_planificadas")),
                        seriesCompletadas = cursor.getInt(cursor.getColumnIndexOrThrow("series_completadas")),
                        pesoUsado = cursor.getDouble(cursor.getColumnIndexOrThrow("peso_usado")),
                        repeticiones = cursor.getString(cursor.getColumnIndexOrThrow("repeticiones"))
                            .split(",").mapNotNull { it.toIntOrNull() },
                        duracionSegundos = cursor.getInt(cursor.getColumnIndexOrThrow("duracion_segundos"))
                    )
                )
            }
            cursor.close()
        }

        return historial
    }


    fun obtenerRachaActual(email: String): Int {
        var racha = 0
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        dbHelper.readableDatabase.use { db ->
            val cursor = db.rawQuery(
                """
                SELECT DISTINCT DATE(fecha/1000, 'unixepoch', 'localtime') as dia
                FROM historial_entrenamientos 
                WHERE email=? 
                ORDER BY dia DESC
                """.trimIndent(),
                arrayOf(email)
            )

            val hoy = dateFormat.format(Date(System.currentTimeMillis()))
            var fechaEsperada = hoy

            while (cursor.moveToNext()) {
                val dia = cursor.getString(0)

                if (dia == fechaEsperada) {
                    racha++
                    // Calcular día anterior
                    val cal = Calendar.getInstance()
                    cal.time = dateFormat.parse(fechaEsperada)!!
                    cal.add(Calendar.DAY_OF_YEAR, -1)
                    fechaEsperada = dateFormat.format(cal.time)
                } else {
                    break
                }
            }
            cursor.close()
        }

        return racha
    }

    fun obtenerDiasEntrenadosEsteMes(email: String): Int {
        val inicioMes = obtenerInicioMesActual()

        dbHelper.readableDatabase.use { db ->
            val cursor = db.rawQuery(
                """
                SELECT COUNT(DISTINCT DATE(fecha/1000, 'unixepoch')) as dias
                FROM historial_entrenamientos 
                WHERE email=? AND fecha >= ?
                """.trimIndent(),
                arrayOf(email, inicioMes.toString())
            )

            return if (cursor.moveToFirst()) {
                val dias = cursor.getInt(0)
                cursor.close()
                dias
            } else {
                cursor.close()
                0
            }
        }
    }

    fun obtenerRecordsPorEjercicio(email: String): Map<String, Pair<Double, Int>> {
        val records = mutableMapOf<String, Pair<Double, Int>>()

        dbHelper.readableDatabase.use { db ->
            val cursor = db.rawQuery(
                """
                SELECT ejercicio_nombre, MAX(peso_usado) as peso_max, repeticiones
                FROM historial_entrenamientos
                WHERE email=?
                GROUP BY ejercicio_nombre
                """.trimIndent(),
                arrayOf(email)
            )

            while (cursor.moveToNext()) {
                val ejercicio = cursor.getString(0)
                val peso = cursor.getDouble(1)
                val repsStr = cursor.getString(2)
                val reps = repsStr.split(",").firstOrNull()?.toIntOrNull() ?: 0

                records[ejercicio] = Pair(peso, reps)
            }
            cursor.close()
        }

        return records
    }

    private fun obtenerInicioMesActual(): Long {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}