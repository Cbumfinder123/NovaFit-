package com.cibertec.novafit.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage


object FirebaseConfig {

    val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance().apply {
            firestoreSettings = com.google.firebase.firestore.FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        }
    }

    val storage: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    object Collections {
        const val ALIMENTOS = "alimentos"
        const val EJERCICIOS = "ejercicios"
        const val PERFILES = "perfiles"
    }

    object StoragePaths {
        const val EJERCICIOS_IMAGES = "ejercicios/"
    }
}