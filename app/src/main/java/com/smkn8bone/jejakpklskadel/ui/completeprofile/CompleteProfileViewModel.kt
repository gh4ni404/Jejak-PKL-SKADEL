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
        onResult: (String?, String?) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return onResult(null, null)
        firestore.collection ("users").document (uid).get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val name = doc.getString("name")
                    val email = doc.getString("email")
                    currentName = name
                    onResult(name, email)
                } else {
                    onResult(null, null)
                }
            }
            .addOnFailureListener {
                onResult(null, null)
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
                val fileUrl = "https://drive.google.com/uc?id=$fileId"

                onUploaded(fileUrl)
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
        phoneNumber: String,
        schoolName: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return onResult(false, "User tidak ditemukan")

        val data = mapOf (
            "kelas" to selectedKelas,
            "jurusan" to selectedJurusan,
            "industri" to selectedIndustri,
            "phone" to phoneNumber,
            "sekolah" to schoolName,
            "image_profile" to (uploadedImageUri ?: "default_image_url")
        )
        firestore.collection("users").document(uid).update(data)
            .addOnSuccessListener { onResult(true, "Profil berhasil dilengkapi") }
            .addOnFailureListener { e -> onResult(false, "Profile gagal dilengkapi: ${e.message}") }
    }
}