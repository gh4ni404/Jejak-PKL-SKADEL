package com.smkn8bone.jejakpklskadel.ui.login

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

//    sealed class LoginState {
//        object Loading : LoginState()
//        object Success : LoginState()
//        data class Error(val message: String) : LoginState()
//    }
//
//    private val _loginState = MutableLiveData<LoginState>()
//    val loginState: LiveData<LoginState> = _loginState
//
//    fun login(email: String, password: String) {
//        _loginState.value = LoginState.Loading
//        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
//            .addOnSuccessListener {
//                _loginState.value = LoginState.Success
//            }
//            .addOnFailureListener { e ->
//                _loginState.value = LoginState.Error(e.message ?: "Login Failed")
//            }
//    }

    fun login(email: String, password: String, onResult: (Boolean, String, Boolean) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val uid = auth.currentUser?.uid
                if (uid != null) {
                    firestore.collection("users").document(uid).get()
                        .addOnSuccessListener { doc ->
                            val isComplete = doc.contains("image_profile") &&
                                    doc.contains("industri") &&
                                    doc.contains("kelas") &&
                                    doc.contains("phone") &&
                                    doc.contains("sekolah")
                            onResult(true, "Selamat Datang, ${doc.getString("name")}", isComplete)
                        }
                        .addOnFailureListener{ e ->
                            onResult(false, "Lengkapi Profile kamu terlebih dahulu ${e.message}", false)
                        }
                } else {
                    onResult(false, "UID tidak ditemukan", false)
                }
            }
            .addOnFailureListener { e ->
                onResult(false, e.message ?: "Login Failed", false)
            }
    }
}