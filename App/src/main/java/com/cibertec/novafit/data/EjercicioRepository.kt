package com.cibertec.novafit.data
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.util.Log
import com.cibertec.novafit.model.Alimento
import com.cibertec.novafit.model.Ejercicio
class EjercicioRepository(context: Context) {
    private val dbHelper = DbHelper(context)


    fun obtenerTodos(): List<Ejercicio> {
        val ejercicios = mutableListOf<Ejercicio>()
        dbHelper.readableDatabase.use { db ->
            val cursor = db.query(
                "ejercicios",
                null,
                "activo = ?",
                arrayOf("1"),
                null,
                null,
                "nombre ASC"
            )

            while (cursor.moveToNext()) {
                ejercicios.add(cursorToEjercicio(cursor))
            }
            cursor.close()
        }
        Log.d("EjercicioRepo", "Obtenidos ${ejercicios.size} ejercicios")
        return ejercicios
    }

    fun obtenerPorLugar(lugar: String): List<Ejercicio> {
        val equipamientos = when (lugar) {
            "Casa sin equipamiento" -> listOf(Ejercicio.EQUIPAMIENTO_NINGUNO)
            "Casa con equipamiento" -> listOf(Ejercicio.EQUIPAMIENTO_NINGUNO, Ejercicio.EQUIPAMIENTO_BASICO)
            "Gym" -> listOf(Ejercicio.EQUIPAMIENTO_NINGUNO, Ejercicio.EQUIPAMIENTO_BASICO, Ejercicio.EQUIPAMIENTO_GYM)
            else -> listOf(Ejercicio.EQUIPAMIENTO_NINGUNO)
        }

        val ejercicios = mutableListOf<Ejercicio>()
        dbHelper.readableDatabase.use { db ->
            val placeholders = equipamientos.joinToString(",") { "?" }
            val cursor = db.rawQuery(
                "SELECT * FROM ejercicios WHERE equipamiento IN ($placeholders) AND activo = 1 ORDER BY nombre ASC",
                equipamientos.toTypedArray()
            )

            while (cursor.moveToNext()) {
                ejercicios.add(cursorToEjercicio(cursor))
            }
            cursor.close()
        }
        return ejercicios
    }


    fun obtenerPorGrupo(grupoMuscular: String, lugar: String): List<Ejercicio> {
        return obtenerPorLugar(lugar).filter { it.grupoMuscular == grupoMuscular }
    }


    fun obtenerPorNivelYLugar(nivelExperiencia: String?, lugar: String): List<Ejercicio> {
        return obtenerPorLugar(lugar).filter { it.esApropiado(nivelExperiencia) }
    }


    fun obtenerPorId(id: Int): Ejercicio? {
        dbHelper.readableDatabase.use { db ->
            val cursor = db.query(
                "ejercicios",
                null,
                "id = ?",
                arrayOf(id.toString()),
                null,
                null,
                null
            )

            return if (cursor.moveToFirst()) {
                val ejercicio = cursorToEjercicio(cursor)
                cursor.close()
                ejercicio
            } else {
                cursor.close()
                null
            }
        }
    }


    fun validarParaRutina(lugar: String, nivelExperiencia: String?): ValidationResult {
        val ejerciciosDisponibles = obtenerPorNivelYLugar(nivelExperiencia, lugar)

        val porGrupo = ejerciciosDisponibles.groupBy { it.grupoMuscular }

        return when {
            ejerciciosDisponibles.size < 10 -> ValidationResult(false, "Se necesitan al menos 10 ejercicios para este lugar y nivel")
            porGrupo["Pecho"]?.size ?: 0 < 2 -> ValidationResult(false, "Faltan ejercicios de Pecho")
            porGrupo["Espalda"]?.size ?: 0 < 2 -> ValidationResult(false, "Faltan ejercicios de Espalda")
            porGrupo["Piernas"]?.size ?: 0 < 2 -> ValidationResult(false, "Faltan ejercicios de Piernas")
            else -> ValidationResult(true, "Datos válidos para generar rutina")
        }
    }


    private fun cursorToEjercicio(cursor: Cursor): Ejercicio {
        return Ejercicio(
            id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
            nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
            grupoMuscular = cursor.getString(cursor.getColumnIndexOrThrow("grupo_muscular")),
            dificultad = cursor.getString(cursor.getColumnIndexOrThrow("dificultad")),
            equipamiento = cursor.getString(cursor.getColumnIndexOrThrow("equipamiento")),
            descripcion = cursor.getString(cursor.getColumnIndexOrThrow("descripcion")),
            imagenResource = cursor.getString(cursor.getColumnIndexOrThrow("imagen_resource")),
            activo = cursor.getInt(cursor.getColumnIndexOrThrow("activo")) == 1,
            fechaCreacion = cursor.getLong(cursor.getColumnIndexOrThrow("fecha_creacion"))
        )
    }

    data class ValidationResult(val isValid: Boolean, val message: String)
}