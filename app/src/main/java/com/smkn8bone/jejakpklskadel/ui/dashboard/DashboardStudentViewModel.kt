package com.smkn8bone.jejakpklskadel.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DashboardStudentViewModel : ViewModel() {

    private val _studentName = MutableLiveData<String>()
    val studentName: LiveData<String> get() = _studentName

    private val _photoUrl = MutableLiveData<String>()
    val photoUrl: LiveData<String> get() = _photoUrl

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    init {
        fetchStudentName()
    }

    private fun fetchStudentName() {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("name") ?: "Siswa"
                val imageUri = doc.getString("image_profile") ?: ""
                _studentName.value = "Hi, $name!"
                _photoUrl.value = imageUri

            }
            .addOnFailureListener {
                _studentName.value = "Hi, Siswa"
                _photoUrl.value = ""
            }
    }

    // Tambahkan logic untuk review status atau statistik di sini nanti
}