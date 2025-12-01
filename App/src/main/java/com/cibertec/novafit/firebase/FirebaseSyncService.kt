package com.cibertec.novafit.firebase

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.cibertec.novafit.data.DbHelper
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

class FirebaseSyncService(private val context: Context) {

    private val firestore = FirebaseConfig.firestore
    private val dbHelper = DbHelper(context)

    private var db: SQLiteDatabase? = null

    private var alimentosListener: ListenerRegistration? = null
    private var ejerciciosListener: ListenerRegistration? = null

    private var syncEnabled = true
    private var isInitialSync = false

    companion object {
        private const val TAG = "FirebaseSync"
    }

    private fun getWritableDb(): SQLiteDatabase {
        if (db == null || !db!!.isOpen) {
            db = dbHelper.writableDatabase
        }
        return db!!
    }

    fun desactivarSync() {
        syncEnabled = false
        detenerListeners()
        Log.d(TAG, "🛑 Sincronización desactivada temporalmente")
    }

    fun reactivarSync() {
        syncEnabled = true
        iniciarListeners()
        Log.d(TAG, "✅ Sincronización reactivada")
    }

    suspend fun sincronizarTodo(): SyncResult {
        if (!syncEnabled) {
            Log.w(TAG, "⚠️ Sincronización desactivada")
            return SyncResult(false, message = "Sincronización desactivada")
        }

        Log.d(TAG, "🔄 Iniciando sincronización COMPLETA...")
        isInitialSync = true

        return try {
            val alimentosCount = sincronizarAlimentos()
            val ejerciciosCount = sincronizarEjercicios()

            isInitialSync = false

            SyncResult(
                success = true,
                alimentosSync = alimentosCount,
                ejerciciosSync = ejerciciosCount,
                message = "Sincronización completada"
            )
        } catch (e: Exception) {
            isInitialSync = false
            Log.e(TAG, "❌ Error: ${e.message}", e)
            SyncResult(
                success = false,
                message = "Error: ${e.message}"
            )
        }
    }

    private suspend fun sincronizarAlimentos(): Int {
        Log.d(TAG, "📥 Sincronizando alimentos...")

        return try {
            // ✅ CAMBIO: Traer TODO desde Firebase (incluyendo inactivos)
            val snapshot = firestore.collection(FirebaseConfig.Collections.ALIMENTOS)
                .get()
                .await()

            if (snapshot.isEmpty) {
                Log.w(TAG, "⚠️ No hay alimentos en Firestore")
                return 0
            }

            Log.d(TAG, "📦 Documentos Firebase: ${snapshot.size()}")
            var procesados = 0

            val database = getWritableDb()

            // ✅ Primero marcar TODOS como inactivos
            database.execSQL("UPDATE alimentos SET activo = 0")
            Log.d(TAG, "🔄 Todos los alimentos marcados como inactivos temporalmente")

            snapshot.documents.forEach { doc ->
                try {
                    val nombre = doc.getString("nombre") ?: return@forEach
                    val activoFirebase = doc.getBoolean("activo") ?: true
                    val existe = verificarAlimentoExiste(database, nombre)

                    val fechaCreacion = doc.getTimestamp("fechaCreacion")?.toDate()?.time
                        ?: doc.getDate("fechaCreacion")?.time
                        ?: System.currentTimeMillis()

                    val cv = ContentValues().apply {
                        put("nombre", nombre)
                        put("categoria", doc.getString("categoria") ?: "proteina")
                        put("proteinas", doc.getDouble("proteinas") ?: 0.0)
                        put("grasas", doc.getDouble("grasas") ?: 0.0)
                        put("carbohidratos", doc.getDouble("carbohidratos") ?: 0.0)
                        put("calorias", doc.getDouble("calorias") ?: 0.0)
                        put("activo", if (activoFirebase) 1 else 0)  // ✅ Respetar estado Firebase
                        put("fecha_creacion", fechaCreacion)
                    }

                    if (existe) {
                        database.update("alimentos", cv, "nombre = ?", arrayOf(nombre))
                    } else {
                        database.insert("alimentos", null, cv)
                    }

                    procesados++
                    val estado = if (activoFirebase) "✅ Activo" else "🔕 Inactivo"
                    Log.d(TAG, "$estado: $nombre")

                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error: ${e.message}")
                }
            }

            Log.d(TAG, "✅ Total procesados: $procesados")
            procesados

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error sincronizando alimentos: ${e.message}")
            0
        }
    }

    private suspend fun sincronizarEjercicios(): Int {
        Log.d(TAG, "📥 Sincronizando ejercicios...")

        return try {
            // ✅ CAMBIO: Traer TODO desde Firebase (incluyendo inactivos)
            val snapshot = firestore.collection(FirebaseConfig.Collections.EJERCICIOS)
                .get()
                .await()

            if (snapshot.isEmpty) {
                Log.w(TAG, "⚠️ No hay ejercicios en Firestore")
                return 0
            }

            Log.d(TAG, "📦 Documentos Firebase: ${snapshot.size()}")
            var procesados = 0

            val database = getWritableDb()

            // ✅ Primero marcar TODOS como inactivos
            database.execSQL("UPDATE ejercicios SET activo = 0")
            Log.d(TAG, "🔄 Todos los ejercicios marcados como inactivos temporalmente")

            snapshot.documents.forEach { doc ->
                try {
                    val nombre = doc.getString("nombre") ?: return@forEach
                    val activoFirebase = doc.getBoolean("activo") ?: true
                    val existe = verificarEjercicioExiste(database, nombre)

                    val fechaCreacion = doc.getTimestamp("fechaCreacion")?.toDate()?.time
                        ?: doc.getDate("fechaCreacion")?.time
                        ?: System.currentTimeMillis()

                    val cv = ContentValues().apply {
                        put("nombre", nombre)
                        put("grupo_muscular", doc.getString("grupoMuscular") ?: "Pecho")
                        put("dificultad", doc.getString("dificultad") ?: "Intermedio")
                        put("equipamiento", doc.getString("equipamiento") ?: "ninguno")
                        put("descripcion", doc.getString("descripcion") ?: "")
                        put("imagen_resource", doc.getString("imagenUrl") ?: "")
                        put("activo", if (activoFirebase) 1 else 0)  // ✅ Respetar estado Firebase
                        put("fecha_creacion", fechaCreacion)
                    }

                    if (existe) {
                        database.update("ejercicios", cv, "nombre = ?", arrayOf(nombre))
                    } else {
                        database.insert("ejercicios", null, cv)
                    }

                    procesados++
                    val estado = if (activoFirebase) "✅ Activo" else "🔕 Inactivo"
                    Log.d(TAG, "$estado: $nombre")

                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error: ${e.message}")
                }
            }

            Log.d(TAG, "✅ Total procesados: $procesados")
            procesados

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error sincronizando ejercicios: ${e.message}")
            0
        }
    }

    private fun verificarAlimentoExiste(database: SQLiteDatabase, nombre: String): Boolean {
        val cursor = database.rawQuery(
            "SELECT COUNT(*) FROM alimentos WHERE nombre = ?",
            arrayOf(nombre)
        )
        val existe = cursor.use {
            it.moveToFirst() && it.getInt(0) > 0
        }
        return existe
    }

    private fun verificarEjercicioExiste(database: SQLiteDatabase, nombre: String): Boolean {
        val cursor = database.rawQuery(
            "SELECT COUNT(*) FROM ejercicios WHERE nombre = ?",
            arrayOf(nombre)
        )
        val existe = cursor.use {
            it.moveToFirst() && it.getInt(0) > 0
        }
        return existe
    }

    // ✅ LISTENERS AHORA ESCUCHAN TODO (no solo activos)
    fun iniciarListeners() {
        if (!syncEnabled) {
            Log.w(TAG, "⚠️ Listeners no iniciados (sync desactivado)")
            return
        }

        if (alimentosListener != null || ejerciciosListener != null) {
            Log.w(TAG, "⚠️ Listeners ya están activos")
            return
        }

        Log.d(TAG, "👂 Iniciando listeners...")

        // ✅ CAMBIO: Sin filtro de activo, detecta TODO
        alimentosListener = firestore.collection(FirebaseConfig.Collections.ALIMENTOS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "❌ Error listener alimentos: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot?.metadata?.isFromCache == false && !isInitialSync) {
                    snapshot.documentChanges.forEach { change ->
                        when (change.type) {
                            com.google.firebase.firestore.DocumentChange.Type.ADDED -> {
                                actualizarAlimentoLocal(change.document)
                                Log.d(TAG, "➕ Nuevo alimento: ${change.document.getString("nombre")}")
                            }
                            com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                                actualizarAlimentoLocal(change.document)
                                Log.d(TAG, "🔄 Modificado alimento: ${change.document.getString("nombre")}")
                            }
                            com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                                // Físicamente eliminado de Firebase (raro)
                                marcarInactivoAlimentoLocal(change.document.getString("nombre") ?: "")
                            }
                        }
                    }
                }
            }

        ejerciciosListener = firestore.collection(FirebaseConfig.Collections.EJERCICIOS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "❌ Error listener ejercicios: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot?.metadata?.isFromCache == false && !isInitialSync) {
                    snapshot.documentChanges.forEach { change ->
                        when (change.type) {
                            com.google.firebase.firestore.DocumentChange.Type.ADDED -> {
                                actualizarEjercicioLocal(change.document)
                                Log.d(TAG, "➕ Nuevo ejercicio: ${change.document.getString("nombre")}")
                            }
                            com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                                actualizarEjercicioLocal(change.document)
                                Log.d(TAG, "🔄 Modificado ejercicio: ${change.document.getString("nombre")}")
                            }
                            com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                                marcarInactivoEjercicioLocal(change.document.getString("nombre") ?: "")
                            }
                        }
                    }
                }
            }

        Log.d(TAG, "✅ Listeners activos")
    }

    fun detenerListeners() {
        alimentosListener?.remove()
        ejerciciosListener?.remove()
        alimentosListener = null
        ejerciciosListener = null
        Log.d(TAG, "🛑 Listeners detenidos")
    }

    private fun actualizarAlimentoLocal(doc: com.google.firebase.firestore.DocumentSnapshot) {
        if (!syncEnabled) return

        try {
            val nombre = doc.getString("nombre") ?: return
            val activoFirebase = doc.getBoolean("activo") ?: true
            val database = getWritableDb()
            val existe = verificarAlimentoExiste(database, nombre)

            val fechaCreacion = doc.getTimestamp("fechaCreacion")?.toDate()?.time
                ?: doc.getDate("fechaCreacion")?.time
                ?: System.currentTimeMillis()

            val cv = ContentValues().apply {
                put("nombre", nombre)
                put("categoria", doc.getString("categoria") ?: "proteina")
                put("proteinas", doc.getDouble("proteinas") ?: 0.0)
                put("grasas", doc.getDouble("grasas") ?: 0.0)
                put("carbohidratos", doc.getDouble("carbohidratos") ?: 0.0)
                put("calorias", doc.getDouble("calorias") ?: 0.0)
                put("activo", if (activoFirebase) 1 else 0)
                put("fecha_creacion", fechaCreacion)
            }

            if (existe) {
                val result = database.update("alimentos", cv, "nombre = ?", arrayOf(nombre))
                if (result > 0) {
                    val estado = if (activoFirebase) "✅" else "🔕"
                    Log.d(TAG, "$estado Actualizado: $nombre")
                }
            } else {
                database.insert("alimentos", null, cv)
                Log.d(TAG, "➕ Insertado: $nombre")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error: ${e.message}")
        }
    }

    private fun actualizarEjercicioLocal(doc: com.google.firebase.firestore.DocumentSnapshot) {
        if (!syncEnabled) return

        try {
            val nombre = doc.getString("nombre") ?: return
            val activoFirebase = doc.getBoolean("activo") ?: true
            val database = getWritableDb()
            val existe = verificarEjercicioExiste(database, nombre)

            val fechaCreacion = doc.getTimestamp("fechaCreacion")?.toDate()?.time
                ?: doc.getDate("fechaCreacion")?.time
                ?: System.currentTimeMillis()

            val cv = ContentValues().apply {
                put("nombre", nombre)
                put("grupo_muscular", doc.getString("grupoMuscular") ?: "Pecho")
                put("dificultad", doc.getString("dificultad") ?: "Intermedio")
                put("equipamiento", doc.getString("equipamiento") ?: "ninguno")
                put("descripcion", doc.getString("descripcion") ?: "")
                put("imagen_resource", doc.getString("imagenUrl") ?: "")
                put("activo", if (activoFirebase) 1 else 0)
                put("fecha_creacion", fechaCreacion)
            }

            if (existe) {
                val result = database.update("ejercicios", cv, "nombre = ?", arrayOf(nombre))
                if (result > 0) {
                    val estado = if (activoFirebase) "✅" else "🔕"
                    Log.d(TAG, "$estado Actualizado: $nombre")
                }
            } else {
                database.insert("ejercicios", null, cv)
                Log.d(TAG, "➕ Insertado: $nombre")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error: ${e.message}")
        }
    }

    private fun marcarInactivoAlimentoLocal(nombre: String) {
        if (!syncEnabled || nombre.isEmpty()) return

        try {
            val database = getWritableDb()
            val cv = ContentValues().apply {
                put("activo", 0)
            }
            val result = database.update("alimentos", cv, "nombre = ?", arrayOf(nombre))
            if (result > 0) {
                Log.d(TAG, "🔕 Desactivado: $nombre")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error: ${e.message}")
        }
    }

    private fun marcarInactivoEjercicioLocal(nombre: String) {
        if (!syncEnabled || nombre.isEmpty()) return

        try {
            val database = getWritableDb()
            val cv = ContentValues().apply {
                put("activo", 0)
            }
            val result = database.update("ejercicios", cv, "nombre = ?", arrayOf(nombre))
            if (result > 0) {
                Log.d(TAG, "🔕 Desactivado: $nombre")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error: ${e.message}")
        }
    }

    fun cleanup() {
        detenerListeners()
        db?.close()
        db = null
        Log.d(TAG, "🗑️ Recursos liberados")
    }

    data class SyncResult(
        val success: Boolean,
        val alimentosSync: Int = 0,
        val ejerciciosSync: Int = 0,
        val message: String
    )
}