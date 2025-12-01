package com.cibertec.novafit.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DbHelper(context: Context) : SQLiteOpenHelper(context, "novafit.db", null, 29) {

    override fun onCreate(db: SQLiteDatabase) {
        crearTablas(db)
        insertarDatosIniciales(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.d("DbHelper", "Actualizando DB de v$oldVersion a v$newVersion")


        db.execSQL("DROP TABLE IF EXISTS rutinas")
        db.execSQL("DROP TABLE IF EXISTS dietas")
        db.execSQL("DROP TABLE IF EXISTS perfiles")
        db.execSQL("DROP TABLE IF EXISTS historial_entrenamientos")
        db.execSQL("DROP TABLE IF EXISTS alimentos")
        db.execSQL("DROP TABLE IF EXISTS ejercicios")

        crearTablas(db)
        insertarDatosIniciales(db)
    }

    private fun crearTablas(db: SQLiteDatabase) {

        db.execSQL("""
            CREATE TABLE perfiles (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                email TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL,
                nombre TEXT NOT NULL,
                fechaNacimiento INTEGER NOT NULL,
                edad INTEGER NOT NULL,
                genero TEXT NOT NULL,
                peso REAL NOT NULL,
                altura REAL NOT NULL,
                circunferenciaCuello REAL,
                circunferenciaCintura REAL,
                circunferenciaCadera REAL,
                nivelActividad TEXT,
                objetivo TEXT NOT NULL,
                tipoCuerpo TEXT,
                nivelExperiencia TEXT
            )
        """.trimIndent())


        db.execSQL("""
            CREATE TABLE alimentos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT UNIQUE NOT NULL,
                categoria TEXT NOT NULL,
                proteinas REAL NOT NULL,
                grasas REAL NOT NULL,
                carbohidratos REAL NOT NULL,
                calorias REAL NOT NULL,
                activo INTEGER DEFAULT 1,
                fecha_creacion INTEGER NOT NULL
            )
        """.trimIndent())


        db.execSQL("CREATE INDEX idx_alimentos_categoria ON alimentos(categoria)")
        db.execSQL("CREATE INDEX idx_alimentos_activo ON alimentos(activo)")


        db.execSQL("""
            CREATE TABLE ejercicios (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT UNIQUE NOT NULL,
                grupo_muscular TEXT NOT NULL,
                dificultad TEXT NOT NULL,
                equipamiento TEXT NOT NULL,
                descripcion TEXT,
                imagen_resource TEXT,
                activo INTEGER DEFAULT 1,
                fecha_creacion INTEGER NOT NULL
            )
        """.trimIndent())


        db.execSQL("CREATE INDEX idx_ejercicios_grupo ON ejercicios(grupo_muscular)")
        db.execSQL("CREATE INDEX idx_ejercicios_equip ON ejercicios(equipamiento)")
        db.execSQL("CREATE INDEX idx_ejercicios_activo ON ejercicios(activo)")


        db.execSQL("""
            CREATE TABLE dietas (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                email TEXT UNIQUE NOT NULL,
                numero_comidas INTEGER NOT NULL,
                proteinas_ids TEXT NOT NULL,
                grasas_ids TEXT NOT NULL,
                carbohidratos_ids TEXT NOT NULL,
                plan_semanal TEXT NOT NULL,
                calorias INTEGER NOT NULL,
                proteinas INTEGER NOT NULL,
                grasas INTEGER NOT NULL,
                carbohidratos INTEGER NOT NULL,
                fecha_creacion INTEGER NOT NULL,
                FOREIGN KEY (email) REFERENCES perfiles(email) ON DELETE CASCADE
            )
        """.trimIndent())


        db.execSQL("""
            CREATE TABLE rutinas (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                email TEXT UNIQUE NOT NULL,
                dias_semana INTEGER NOT NULL,
                lugar TEXT NOT NULL,
                objetivo TEXT NOT NULL,
                nivel_experiencia TEXT NOT NULL,
                hash_perfil TEXT NOT NULL,
                plan_semanal TEXT NOT NULL,
                fecha_creacion INTEGER NOT NULL,
                FOREIGN KEY (email) REFERENCES perfiles(email) ON DELETE CASCADE
            )
        """.trimIndent())


        db.execSQL("""
            CREATE TABLE historial_entrenamientos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                email TEXT NOT NULL,
                fecha INTEGER NOT NULL,
                dia_rutina TEXT NOT NULL,
                ejercicio_nombre TEXT NOT NULL,
                series_planificadas INTEGER NOT NULL,
                series_completadas INTEGER NOT NULL,
                peso_usado REAL NOT NULL,
                repeticiones TEXT NOT NULL,
                duracion_segundos INTEGER NOT NULL,
                FOREIGN KEY (email) REFERENCES perfiles(email) ON DELETE CASCADE
            )
        """.trimIndent())


        db.execSQL("CREATE INDEX idx_historial_fecha ON historial_entrenamientos(email, fecha)")
    }

    private fun insertarDatosIniciales(db: SQLiteDatabase) {
        Log.d("DbHelper", "Insertando datos iniciales...")

        val timestamp = System.currentTimeMillis()


        val alimentos = listOf(
            // PROTEÍNAS
            Alimento("Pechuga de pollo", "proteina", 23.0, 3.6, 0.0, 165.0),
            Alimento("Atún en agua", "proteina", 25.0, 1.0, 0.0, 110.0),
            Alimento("Huevos", "proteina", 13.0, 11.0, 1.0, 155.0),
            Alimento("Carne magra", "proteina", 26.0, 5.0, 0.0, 150.0),
            Alimento("Salmón", "proteina", 20.0, 13.0, 0.0, 208.0),
            Alimento("Tofu", "proteina", 8.0, 4.0, 2.0, 76.0),
            Alimento("Pavo", "proteina", 24.0, 1.0, 0.0, 135.0),
            Alimento("Merluza", "proteina", 17.0, 1.0, 0.0, 90.0),

            // GRASAS
            Alimento("Aguacate", "grasa", 2.0, 15.0, 9.0, 160.0),
            Alimento("Almendras", "grasa", 21.0, 50.0, 22.0, 579.0),
            Alimento("Aceite de oliva", "grasa", 0.0, 100.0, 0.0, 884.0),
            Alimento("Mantequilla de maní", "grasa", 25.0, 50.0, 20.0, 588.0),
            Alimento("Nueces", "grasa", 15.0, 65.0, 14.0, 654.0),
            Alimento("Aceite de coco", "grasa", 0.0, 99.0, 0.0, 862.0),

            // CARBOHIDRATOS
            Alimento("Arroz integral", "carbohidrato", 7.0, 2.0, 77.0, 370.0),
            Alimento("Avena", "carbohidrato", 13.0, 7.0, 66.0, 379.0),
            Alimento("Papa", "carbohidrato", 2.0, 0.1, 20.0, 87.0),
            Alimento("Quinoa", "carbohidrato", 14.0, 6.0, 64.0, 368.0),
            Alimento("Pan integral", "carbohidrato", 9.0, 4.0, 43.0, 247.0),
            Alimento("Batata", "carbohidrato", 2.0, 0.2, 20.0, 86.0),
            Alimento("Pasta integral", "carbohidrato", 13.0, 2.0, 71.0, 348.0),
            Alimento("Plátano", "carbohidrato", 1.0, 0.3, 23.0, 89.0)
        )

        alimentos.forEach { alimento ->
            val cv = ContentValues().apply {
                put("nombre", alimento.nombre)
                put("categoria", alimento.categoria)
                put("proteinas", alimento.proteinas)
                put("grasas", alimento.grasas)
                put("carbohidratos", alimento.carbohidratos)
                put("calorias", alimento.calorias)
                put("activo", 1)
                put("fecha_creacion", timestamp)
            }
            db.insert("alimentos", null, cv)
        }


        val ejercicios = listOf(
            // PECHO - Sin equipamiento
            Ejercicio("Flexiones estándar", "Pecho", "Principiante", "ninguno", "ejercicio_flexiones_estandar"),
            Ejercicio("Flexiones diamante", "Pecho", "Intermedio", "ninguno", "ejercicio_flexiones_diamante"),
            Ejercicio("Flexiones declinadas", "Pecho", "Intermedio", "ninguno", "ejercicio_flexiones_declinadas"),
            Ejercicio("Flexiones archer", "Pecho", "Avanzado", "ninguno", "ejercicio_flexiones_archer"),

            // PECHO - Equipamiento básico
            Ejercicio("Press con mancuernas plano", "Pecho", "Intermedio", "basico", "ejercicio_press_mancuernas"),
            Ejercicio("Aperturas con mancuernas", "Pecho", "Intermedio", "basico", "ejercicio_aperturas_mancuernas"),
            Ejercicio("Press inclinado con mancuernas", "Pecho", "Intermedio", "basico", "ejercicio_press_inclinado_mancuernas"),
            Ejercicio("Pullover con mancuerna", "Pecho", "Avanzado", "basico", "ejercicio_pullover_mancuerna"),

            // PECHO - Gym
            Ejercicio("Press de banca con barra", "Pecho", "Intermedio", "gym", "ejercicio_press_banca_barra"),
            Ejercicio("Press inclinado con barra", "Pecho", "Intermedio", "gym", "ejercicio_press_inclinado_barra"),
            Ejercicio("Cruces en polea", "Pecho", "Intermedio", "gym", "ejercicio_cruces_polea"),
            Ejercicio("Press en máquina", "Pecho", "Principiante", "gym", "ejercicio_press_maquina"),
            Ejercicio("Fondos en paralelas", "Pecho", "Avanzado", "gym", "ejercicio_fondos_paralelas"),

            // ESPALDA - Sin equipamiento
            Ejercicio("Dominadas asistidas", "Espalda", "Principiante", "ninguno", "ejercicio_dominadas_asistidas"),
            Ejercicio("Superman", "Espalda", "Principiante", "ninguno", "ejercicio_superman"),
            Ejercicio("Remo invertido en mesa", "Espalda", "Intermedio", "ninguno", "ejercicio_remo_invertido_mesa"),

            // ESPALDA - Equipamiento básico
            Ejercicio("Remo con mancuerna", "Espalda", "Intermedio", "basico", "ejercicio_remo_mancuerna"),
            Ejercicio("Peso muerto rumano con mancuernas", "Espalda", "Avanzado", "basico", "ejercicio_peso_muerto_rumano_mancuernas"),

            // ESPALDA - Gym
            Ejercicio("Peso muerto convencional", "Espalda", "Avanzado", "gym", "ejercicio_peso_muerto_convencional"),
            Ejercicio("Remo con barra", "Espalda", "Intermedio", "gym", "ejercicio_remo_barra"),
            Ejercicio("Jalón al pecho", "Espalda", "Intermedio", "gym", "ejercicio_jalon_pecho"),
            Ejercicio("Dominadas pronación", "Espalda", "Avanzado", "gym", "ejercicio_dominadas_pronacion"),

            // PIERNAS - Sin equipamiento
            Ejercicio("Sentadillas", "Piernas", "Principiante", "ninguno", "ejercicio_sentadillas"),
            Ejercicio("Sentadilla búlgara", "Piernas", "Intermedio", "ninguno", "ejercicio_sentadilla_bulgara"),
            Ejercicio("Zancadas", "Piernas", "Principiante", "ninguno", "ejercicio_zancadas"),
            Ejercicio("Elevaciones de gemelos", "Piernas", "Principiante", "ninguno", "ejercicio_elevaciones_gemelos"),
            Ejercicio("Puente de glúteos", "Piernas", "Principiante", "ninguno", "ejercicio_puente_gluteos"),

            // PIERNAS - Equipamiento básico
            Ejercicio("Sentadilla goblet con mancuerna", "Piernas", "Intermedio", "basico", "ejercicio_sentadilla_goblet_mancuerna"),
            Ejercicio("Zancadas con mancuernas", "Piernas", "Intermedio", "basico", "ejercicio_zancadas_mancuernas"),

            // PIERNAS - Gym
            Ejercicio("Sentadilla con barra", "Piernas", "Intermedio", "gym", "ejercicio_sentadilla_barra"),
            Ejercicio("Prensa de piernas", "Piernas", "Intermedio", "gym", "ejercicio_prensa_piernas"),
            Ejercicio("Peso muerto con barra", "Piernas", "Avanzado", "gym", "ejercicio_peso_muerto_barra"),
            Ejercicio("Extensiones de cuádriceps", "Piernas", "Principiante", "gym", "ejercicio_extensiones_cuadriceps"),
            Ejercicio("Curl femoral", "Piernas", "Principiante", "gym", "ejercicio_curl_femoral"),

            // HOMBROS - Sin equipamiento
            Ejercicio("Flexiones pike", "Hombros", "Intermedio", "ninguno", "ejercicio_flexiones_pike"),

            // HOMBROS - Equipamiento básico
            Ejercicio("Press militar con mancuernas", "Hombros", "Intermedio", "basico", "ejercicio_press_militar_mancuernas"),
            Ejercicio("Elevaciones laterales", "Hombros", "Principiante", "basico", "ejercicio_elevaciones_laterales"),
            Ejercicio("Elevaciones frontales", "Hombros", "Principiante", "basico", "ejercicio_elevaciones_frontales"),

            // HOMBROS - Gym
            Ejercicio("Press militar con barra", "Hombros", "Intermedio", "gym", "ejercicio_press_militar_barra"),
            Ejercicio("Press Arnold", "Hombros", "Avanzado", "gym", "ejercicio_press_arnold"),
            Ejercicio("Face pulls", "Hombros", "Intermedio", "gym", "ejercicio_face_pulls"),

            // BÍCEPS - Equipamiento básico
            Ejercicio("Curl con mancuernas", "Bíceps", "Principiante", "basico", "ejercicio_curl_mancuernas"),
            Ejercicio("Curl martillo", "Bíceps", "Principiante", "basico", "ejercicio_curl_martillo"),
            Ejercicio("Curl concentrado", "Bíceps", "Intermedio", "basico", "ejercicio_curl_concentrado"),

            // BÍCEPS - Gym
            Ejercicio("Curl con barra Z", "Bíceps", "Intermedio", "gym", "ejercicio_curl_barra_z"),
            Ejercicio("Curl en banco Scott", "Bíceps", "Intermedio", "gym", "ejercicio_curl_banco_scott"),
            Ejercicio("Curl en polea", "Bíceps", "Intermedio", "gym", "ejercicio_curl_polea"),

            // TRÍCEPS - Sin equipamiento
            Ejercicio("Fondos en banco", "Tríceps", "Principiante", "ninguno", "ejercicio_fondos_banco"),
            Ejercicio("Flexiones cerradas", "Tríceps", "Intermedio", "ninguno", "ejercicio_flexiones_cerradas"),

            // TRÍCEPS - Equipamiento básico
            Ejercicio("Extensión de tríceps con mancuerna", "Tríceps", "Intermedio", "basico", "ejercicio_extension_triceps_mancuerna"),
            Ejercicio("Patada de tríceps", "Tríceps", "Principiante", "basico", "ejercicio_patada_triceps"),

            // TRÍCEPS - Gym
            Ejercicio("Press francés con barra", "Tríceps", "Intermedio", "gym", "ejercicio_press_frances_barra"),
            Ejercicio("Extensión de tríceps en polea", "Tríceps", "Principiante", "gym", "ejercicio_extension_triceps_polea"),

            // CORE - Sin equipamiento
            Ejercicio("Plancha frontal", "Core", "Principiante", "ninguno", "ejercicio_plancha_frontal"),
            Ejercicio("Plancha lateral", "Core", "Intermedio", "ninguno", "ejercicio_plancha_lateral"),
            Ejercicio("Crunches", "Core", "Principiante", "ninguno", "ejercicio_crunches"),
            Ejercicio("Bicicleta", "Core", "Intermedio", "ninguno", "ejercicio_bicicleta"),
            Ejercicio("Mountain climbers", "Core", "Intermedio", "ninguno", "ejercicio_mountain_climbers"),

            // CORE - Gym
            Ejercicio("Elevaciones de piernas colgado", "Core", "Avanzado", "gym", "ejercicio_elevaciones_piernas_colgado"),
            Ejercicio("Ab wheel", "Core", "Avanzado", "gym", "ejercicio_ab_wheel"),
            Ejercicio("Cable crunch", "Core", "Intermedio", "gym", "ejercicio_cable_crunch")
        )

        ejercicios.forEach { ejercicio ->
            val cv = ContentValues().apply {
                put("nombre", ejercicio.nombre)
                put("grupo_muscular", ejercicio.grupoMuscular)
                put("dificultad", ejercicio.dificultad)
                put("equipamiento", ejercicio.equipamiento)
                put("imagen_resource", ejercicio.imagenResource)
                put("activo", 1)
                put("fecha_creacion", timestamp)
            }
            db.insert("ejercicios", null, cv)
        }

        Log.d("DbHelper", "Datos iniciales insertados: ${alimentos.size} alimentos, ${ejercicios.size} ejercicios")
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }


    private data class Alimento(
        val nombre: String,
        val categoria: String,
        val proteinas: Double,
        val grasas: Double,
        val carbohidratos: Double,
        val calorias: Double
    )

    private data class Ejercicio(
        val nombre: String,
        val grupoMuscular: String,
        val dificultad: String,
        val equipamiento: String,
        val imagenResource: String
    )
}