package com.smkn8bone.jejakpklskadel.ui.register

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun register(name: String, email: String, password: String, callback: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                    val userData = hashMapOf(
                        "uid" to uid,
                        "name" to name,
                        "email" to email,
                        "role" to "siswa"
                    )
                    firestore.collection("users")
                        .document(uid)
                        .set(userData)
                        .addOnSuccessListener {
                            callback(true, "Registrasi Berhasil")
                        }
                        .addOnFailureListener { e ->
                            callback(false, "Registrasi Gagal: ${e.message}")
                        }
                } else {
                    callback(false, "Registrasi Gagal: ${task.exception?.message}")
                }
            }
    }
}