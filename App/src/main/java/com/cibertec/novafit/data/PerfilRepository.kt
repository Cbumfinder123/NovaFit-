package com.cibertec.novafit.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.cibertec.novafit.firebase.FirebaseConfig
import com.cibertec.novafit.model.Perfil
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await

class PerfilRepository(context: Context) {
    private val helper = DbHelper(context)
    private val firestore = FirebaseConfig.firestore

    companion object {
        private const val TAG = "PerfilRepo"
    }

    fun guardarPerfil(perfil: Perfil) {
        guardarEnSQLite(perfil)

        try {
            sincronizarAFirestore(perfil)
        } catch (e: Exception) {
            Log.e(TAG, "⚠️ Error sincronizando a Firestore (no crítico): ${e.message}")
        }
    }

    private fun guardarEnSQLite(perfil: Perfil) {
        helper.writableDatabase.use { db ->
            val cv = ContentValues().apply {
                put("email", perfil.email)
                put("password", perfil.password)
                put("nombre", perfil.nombre)
                put("fechaNacimiento", perfil.fechaNacimiento)
                put("edad", perfil.edad)
                put("genero", perfil.genero)
                put("peso", perfil.peso)
                put("altura", perfil.altura)
                put("circunferenciaCuello", perfil.circunferenciaCuello)
                put("circunferenciaCintura", perfil.circunferenciaCintura)
                put("circunferenciaCadera", perfil.circunferenciaCadera)
                put("nivelActividad", perfil.nivelActividad)
                put("objetivo", perfil.objetivo)
                put("tipoCuerpo", perfil.tipoCuerpo)
                put("nivelExperiencia", perfil.nivelExperiencia)
            }

            Log.d(TAG, "Guardando en SQLite: ${perfil.email}")
            val result = db.insertWithOnConflict("perfiles", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
            if (result == -1L) {
                Log.e(TAG, "❌ Error insertando en SQLite")
            } else {
                Log.d(TAG, "✅ Guardado en SQLite con ID: $result")
            }
        }
    }

    private fun sincronizarAFirestore(perfil: Perfil) {
        Thread {
            try {
                val perfilData = hashMapOf(
                    "email" to perfil.email,
                    "nombre" to perfil.nombre,
                    "edad" to perfil.edad,
                    "genero" to perfil.genero,
                    "peso" to perfil.peso,
                    "altura" to perfil.altura,
                    "circunferenciaCuello" to (perfil.circunferenciaCuello ?: 0.0),
                    "circunferenciaCintura" to (perfil.circunferenciaCintura ?: 0.0),
                    "circunferenciaCadera" to (perfil.circunferenciaCadera ?: 0.0),
                    "nivelActividad" to (perfil.nivelActividad ?: "Sedentario"),
                    "objetivo" to perfil.objetivo,
                    "tipoCuerpo" to (perfil.tipoCuerpo ?: "Ectomorfo"),
                    "nivelExperiencia" to (perfil.nivelExperiencia ?: "Principiante"),
                    "fechaRegistro" to FieldValue.serverTimestamp(),
                    "activo" to true
                )

                firestore.collection("usuarios")
                    .whereEqualTo("email", perfil.email)
                    .limit(1)
                    .get()
                    .addOnSuccessListener { existente ->
                        if (existente.isEmpty) {
                            firestore.collection("usuarios")
                                .add(perfilData)
                                .addOnSuccessListener {
                                    Log.d(TAG, "✅ Usuario creado en Firestore: ${perfil.email}")
                                }
                                .addOnFailureListener { e ->
                                    Log.e(TAG, "❌ Error creando en Firestore: ${e.message}")
                                }
                        } else {
                            val docId = existente.documents[0].id
                            firestore.collection("usuarios")
                                .document(docId)
                                .update(perfilData as Map<String, Any>)
                                .addOnSuccessListener {
                                    Log.d(TAG, "✅ Usuario actualizado en Firestore: ${perfil.email}")
                                }
                                .addOnFailureListener { e ->
                                    Log.e(TAG, "❌ Error actualizando en Firestore: ${e.message}")
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "❌ Error buscando en Firestore: ${e.message}")
                    }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error en sincronización: ${e.message}")
            }
        }.start()
    }

    fun getPerfilByEmail(email: String): Perfil? {
        helper.readableDatabase.use { db ->
            Log.d(TAG, "Buscando email: $email")
            val cursor = db.query("perfiles", null, "email=?", arrayOf(email), null, null, null)
            if (cursor.moveToFirst()) {
                val perfil = Perfil(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    email = cursor.getString(cursor.getColumnIndexOrThrow("email")),
                    password = cursor.getString(cursor.getColumnIndexOrThrow("password")),
                    nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
                    fechaNacimiento = cursor.getLong(cursor.getColumnIndexOrThrow("fechaNacimiento")),
                    edad = cursor.getInt(cursor.getColumnIndexOrThrow("edad")),
                    genero = cursor.getString(cursor.getColumnIndexOrThrow("genero")),
                    peso = cursor.getDouble(cursor.getColumnIndexOrThrow("peso")),
                    altura = cursor.getDouble(cursor.getColumnIndexOrThrow("altura")),
                    circunferenciaCuello = if (cursor.isNull(cursor.getColumnIndexOrThrow("circunferenciaCuello"))) null else cursor.getDouble(cursor.getColumnIndexOrThrow("circunferenciaCuello")),
                    circunferenciaCintura = if (cursor.isNull(cursor.getColumnIndexOrThrow("circunferenciaCintura"))) null else cursor.getDouble(cursor.getColumnIndexOrThrow("circunferenciaCintura")),
                    circunferenciaCadera = if (cursor.isNull(cursor.getColumnIndexOrThrow("circunferenciaCadera"))) null else cursor.getDouble(cursor.getColumnIndexOrThrow("circunferenciaCadera")),
                    nivelActividad = cursor.getString(cursor.getColumnIndexOrThrow("nivelActividad")),
                    objetivo = cursor.getString(cursor.getColumnIndexOrThrow("objetivo")),
                    tipoCuerpo = cursor.getString(cursor.getColumnIndexOrThrow("tipoCuerpo")),
                    nivelExperiencia = cursor.getString(cursor.getColumnIndexOrThrow("nivelExperiencia"))
                )
                Log.d(TAG, "Encontrado: $perfil")
                cursor.close()
                return perfil
            }
            Log.w(TAG, "No encontrado email: $email")
            cursor.close()
            return null
        }
    }

    fun borrarTodosLosPerfiles() {
        helper.writableDatabase.use { db ->
            db.delete("perfiles", null, null)
            Log.d(TAG, "Todos los perfiles borrados")
        }
    }


    suspend fun descargarPerfilDesdeFirebase(email: String): Perfil? {
        return try {
            Log.d(TAG, "🔍 Buscando perfil en Firebase: $email")

            val snapshot = firestore.collection("usuarios")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .await()

            if (snapshot.isEmpty) {
                Log.w(TAG, "⚠️ No encontrado en Firebase: $email")
                return null
            }

            val doc = snapshot.documents[0]
            val activo = doc.getBoolean("activo") ?: true

            if (!activo) {
                Log.w(TAG, "⛔ Usuario desactivado: $email")
                return null
            }


            val perfil = Perfil(
                email = doc.getString("email") ?: email,
                password = "",  // La contraseña se asigna después de validar
                nombre = doc.getString("nombre") ?: "",
                fechaNacimiento = System.currentTimeMillis(),  // Fecha actual
                edad = (doc.getLong("edad") ?: 0).toInt(),
                genero = doc.getString("genero") ?: "",
                peso = doc.getDouble("peso") ?: 0.0,
                altura = doc.getDouble("altura") ?: 0.0,
                circunferenciaCuello = doc.getDouble("circunferenciaCuello"),
                circunferenciaCintura = doc.getDouble("circunferenciaCintura"),
                circunferenciaCadera = doc.getDouble("circunferenciaCadera"),
                nivelActividad = doc.getString("nivelActividad"),
                objetivo = doc.getString("objetivo") ?: "",
                tipoCuerpo = doc.getString("tipoCuerpo"),
                nivelExperiencia = doc.getString("nivelExperiencia")
            )

            Log.d(TAG, "✅ Perfil descargado: ${perfil.nombre}")
            perfil

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error descargando perfil: ${e.message}")
            null
        }
    }


    fun validarLogin(email: String, password: String, callback: (Boolean, String) -> Unit) {

        val perfilLocal = getPerfilByEmail(email)

        if (perfilLocal != null) {

            if (perfilLocal.password != password) {
                callback(false, "❌ Contraseña incorrecta")
                return
            }


            verificarEstadoFirebase(email, perfilLocal.nombre, callback)
            return
        }


        Log.d(TAG, "🔍 Usuario no encontrado localmente, buscando en Firebase...")

        Thread {
            try {
                kotlinx.coroutines.runBlocking {
                    val perfilFirebase = descargarPerfilDesdeFirebase(email)

                    if (perfilFirebase == null) {
                        callback(false, "❌ Usuario no encontrado")
                        return@runBlocking
                    }


                    val perfilCompleto = perfilFirebase.copy(
                        password = password
                    )

                    guardarEnSQLite(perfilCompleto)
                    Log.d(TAG, "✅ Perfil descargado y guardado localmente")

                    callback(true, "✅ Bienvenido ${perfilCompleto.nombre}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error: ${e.message}")
                callback(false, "❌ Error de conexión: ${e.message}")
            }
        }.start()
    }

    private fun verificarEstadoFirebase(email: String, nombre: String, callback: (Boolean, String) -> Unit) {
        Thread {
            try {
                firestore.collection("usuarios")
                    .whereEqualTo("email", email)
                    .limit(1)
                    .get()
                    .addOnSuccessListener { docs ->
                        if (docs.isEmpty) {
                            Log.w(TAG, "⚠️ Usuario no encontrado en Firestore, permitiendo login local")
                            callback(true, "✅ Bienvenido $nombre")
                        } else {
                            val activo = docs.documents[0].getBoolean("activo") ?: true

                            if (!activo) {
                                Log.w(TAG, "⛔ Usuario desactivado: $email")
                                callback(false, "⛔ Tu cuenta ha sido desactivada por un administrador.\n\nContacta al soporte si crees que es un error.")
                            } else {
                                Log.d(TAG, "✅ Usuario activo, permitiendo login")
                                callback(true, "✅ Bienvenido $nombre")
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "⚠️ No se pudo verificar Firestore: ${e.message}")
                        callback(true, "✅ Bienvenido $nombre (sin conexión)")
                    }
            } catch (e: Exception) {
                Log.e(TAG, "⚠️ Error verificando Firestore: ${e.message}")
                callback(true, "✅ Bienvenido $nombre")
            }
        }.start()
    }
}