package com.smkn8bone.jejakpklskadel.ui.completeprofile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.smkn8bone.jejakpklskadel.utils.DriveServiceHelper
import com.smkn8bone.jejakpklskadel.utils.FileUtils

class CompleteProfileViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var uploadedImageUri: String? = null
    private var currentName: String? = null

    fun fetchUserData (
        onResult: (String?, String?,String?, String?,String?, String?,String?, String?) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return onResult(null, null, null, null, null, null, null, null)
        firestore.collection ("users").document (uid).get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val name = doc.getString("name")
                    val email = doc.getString("email")
                    val phone = doc.getString("phone")
                    val kelas = doc.getString("kelas")
                    val jurusan = doc.getString("jurusan")
                    val industri = doc.getString("industri")
                    val guruPembimbing = doc.getString("guru_pembimbing")
                    val sekolah = doc.getString("sekolah")

                    currentName = name
                    onResult(name, email, phone, kelas, jurusan, industri, guruPembimbing, sekolah)
                } else {
                    onResult(null, null, null, null, null, null, null, null)
                }
            }
            .addOnFailureListener {
                onResult(null, null, null, null, null, null, null, null)
            }
    }

    fun uploadImageToDrive (
        context: Context, uri: Uri, parentFolderId: String, onUploaded: (String?) -> Unit
    ) {
        val driveService = DriveServiceHelper.getDriveService(context)
        if (driveService == null) {
            onUploaded(null)
            return
        }

        Thread {
            try {
                val compressedFile = FileUtils.compressImage(context, uri)
                val uid = auth.currentUser?.uid ?: "nouser"
                val safeName = currentName?.replace(" ","_")?.lowercase() ?: "anonymous"
                val fileName = "profile_${safeName}_$uid.jpg"

                val fileId = DriveServiceHelper.uploadFile(driveService, compressedFile, fileName, parentFolderId)
//                val fileUrl = "https://drive.google.com/uc?id=$fileId"

                onUploaded(fileId)
            } catch (e: Exception) {
                e.printStackTrace()
                onUploaded(null)
            }
        }.start()
    }

    fun setUploadedImageUri (uri: String?) {
        uploadedImageUri = uri
    }

    fun saveProfile (
        selectedKelas: String,
        selectedJurusan: String,
        selectedIndustri: String,
        selectedTeacher: String,
        phoneNumber: String,
        schoolName: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return onResult(false, "User tidak ditemukan")

        val data = mapOf (
            "kelas" to selectedKelas,
            "jurusan" to selectedJurusan,
            "industri" to selectedIndustri,
            "guru_pembimbing" to selectedTeacher,
            "phone" to phoneNumber,
            "sekolah" to schoolName,
            "image_profile" to uploadedImageUri
        )
        firestore.collection("users").document(uid).update(data)
            .addOnSuccessListener { onResult(true, "Profil berhasil dilengkapi") }
            .addOnFailureListener { e -> onResult(false, "Profile gagal dilengkapi: ${e.message}") }
    }
}