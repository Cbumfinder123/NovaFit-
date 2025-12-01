package com.cibertec.novafit.data
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.util.Log
import com.cibertec.novafit.model.Alimento
import com.cibertec.novafit.model.Ejercicio
class AlimentoRepository(context: Context) {
    private val dbHelper = DbHelper(context)


    fun obtenerTodos(): List<Alimento> {
        val alimentos = mutableListOf<Alimento>()
        dbHelper.readableDatabase.use { db ->
            val cursor = db.query(
                "alimentos",
                null,
                "activo = ?",
                arrayOf("1"),
                null,
                null,
                "nombre ASC"
            )

            while (cursor.moveToNext()) {
                alimentos.add(cursorToAlimento(cursor))
            }
            cursor.close()
        }
        Log.d("AlimentoRepo", "Obtenidos ${alimentos.size} alimentos")
        return alimentos
    }


    fun obtenerPorCategoria(categoria: String): List<Alimento> {
        val alimentos = mutableListOf<Alimento>()
        dbHelper.readableDatabase.use { db ->
            val cursor = db.query(
                "alimentos",
                null,
                "categoria = ? AND activo = ?",
                arrayOf(categoria, "1"),
                null,
                null,
                "nombre ASC"
            )

            while (cursor.moveToNext()) {
                alimentos.add(cursorToAlimento(cursor))
            }
            cursor.close()
        }
        return alimentos
    }


    fun obtenerProteinas(): List<Alimento> {
        return obtenerPorCategoria(Alimento.CATEGORIA_PROTEINA)
    }


    fun obtenerGrasas(): List<Alimento> {
        return obtenerPorCategoria(Alimento.CATEGORIA_GRASA)
    }


    fun obtenerCarbohidratos(): List<Alimento> {
        return obtenerPorCategoria(Alimento.CATEGORIA_CARBOHIDRATO)
    }


    fun obtenerPorIds(ids: List<Int>): List<Alimento> {
        if (ids.isEmpty()) return emptyList()

        val alimentos = mutableListOf<Alimento>()
        dbHelper.readableDatabase.use { db ->
            val placeholders = ids.joinToString(",") { "?" }
            val cursor = db.rawQuery(
                "SELECT * FROM alimentos WHERE id IN ($placeholders) AND activo = 1",
                ids.map { it.toString() }.toTypedArray()
            )

            while (cursor.moveToNext()) {
                alimentos.add(cursorToAlimento(cursor))
            }
            cursor.close()
        }
        return alimentos
    }


    fun obtenerPorId(id: Int): Alimento? {
        dbHelper.readableDatabase.use { db ->
            val cursor = db.query(
                "alimentos",
                null,
                "id = ?",
                arrayOf(id.toString()),
                null,
                null,
                null
            )

            return if (cursor.moveToFirst()) {
                val alimento = cursorToAlimento(cursor)
                cursor.close()
                alimento
            } else {
                cursor.close()
                null
            }
        }
    }


    fun validarParaDieta(): ValidationResult {
        val proteinas = obtenerProteinas()
        val grasas = obtenerGrasas()
        val carbos = obtenerCarbohidratos()

        return when {
            proteinas.size < 2 -> ValidationResult(false, "Se necesitan al menos 2 alimentos de proteína activos")
            grasas.size < 2 -> ValidationResult(false, "Se necesitan al menos 2 alimentos de grasa activos")
            carbos.size < 2 -> ValidationResult(false, "Se necesitan al menos 2 alimentos de carbohidratos activos")
            proteinas.any { !it.tieneMacrosPrincipales() } -> ValidationResult(false, "Algunos alimentos de proteína no tienen suficientes macros")
            grasas.any { !it.tieneMacrosPrincipales() } -> ValidationResult(false, "Algunos alimentos de grasa no tienen suficientes macros")
            carbos.any { !it.tieneMacrosPrincipales() } -> ValidationResult(false, "Algunos alimentos de carbohidratos no tienen suficientes macros")
            else -> ValidationResult(true, "Datos válidos para generar dieta")
        }
    }


    private fun cursorToAlimento(cursor: Cursor): Alimento {
        return Alimento(
            id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
            nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
            categoria = cursor.getString(cursor.getColumnIndexOrThrow("categoria")),
            proteinas = cursor.getDouble(cursor.getColumnIndexOrThrow("proteinas")),
            grasas = cursor.getDouble(cursor.getColumnIndexOrThrow("grasas")),
            carbohidratos = cursor.getDouble(cursor.getColumnIndexOrThrow("carbohidratos")),
            calorias = cursor.getDouble(cursor.getColumnIndexOrThrow("calorias")),
            activo = cursor.getInt(cursor.getColumnIndexOrThrow("activo")) == 1,
            fechaCreacion = cursor.getLong(cursor.getColumnIndexOrThrow("fecha_creacion"))
        )
    }

    data class ValidationResult(val isValid: Boolean, val message: String)
}
