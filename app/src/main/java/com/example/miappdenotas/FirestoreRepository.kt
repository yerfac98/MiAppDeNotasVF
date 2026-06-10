package com.example.miappdenotas.repository

import com.example.miappdenotas.model.Nota
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreRepository {

    private val db = FirebaseFirestore.getInstance()

    suspend fun subirNota(nota: Nota) {
        if (nota.userId.isBlank()) return

        db.collection("usuarios")
            .document(nota.userId)
            .collection("notas")
            .document(nota.id.toString())
            .set(nota)
            .await()
    }

    suspend fun eliminarNotaEnNube(nota: Nota) {
        if (nota.userId.isBlank()) return

        db.collection("usuarios")
            .document(nota.userId)
            .collection("notas")
            .document(nota.id.toString())
            .delete()
            .await()
    }
}