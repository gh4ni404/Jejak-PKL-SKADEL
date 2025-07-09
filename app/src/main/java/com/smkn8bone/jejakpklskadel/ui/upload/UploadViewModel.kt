package com.smkn8bone.jejakpklskadel.ui.upload

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.smkn8bone.jejakpklskadel.utils.DriveServiceHelper
import com.smkn8bone.jejakpklskadel.utils.FileUtils

class UploadViewModel : ViewModel() {
    // Status Upload
    sealed class UploadState {
        data object Loading : UploadState()
        data object Idle : UploadState()
        data class Success(val message: String) : UploadState()
        data class Error(val message: String) : UploadState()
    }

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var uploadedImageUri: String? = null
    private var currentName: String? = null

    private val _uploadState = MutableLiveData<UploadState>()
    val uploadState: LiveData<UploadState> get() = _uploadState

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

    fun uploadDocumentation(context: Context, title: String, description: String) {
        val user = auth.currentUser
        if (user == null) {
            _uploadState.value = UploadState.Error("User tidak ditemukan")
            return
        }

        // Validasi judul
        if (title.isEmpty()) {
            _uploadState.value = UploadState.Error("Judul tidak boleh kosong")
            return
        }
        // Validasi deskripsi
        if (description.isEmpty()) {
            _uploadState.value = UploadState.Error("Deskripsi tidak boleh kosong")
            return
        }

        // Simulasi proses upload
        _uploadState.value = UploadState.Loading

        // simpan metadata ke Firestore
        uploadedImageUri?.let { saveMetadata(user.uid, it, title, description) }
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

    private fun saveMetadata(uid: String, imageUrl: String, title: String, description: String) {
        val timeStamp = System.currentTimeMillis()
        val metadata = hashMapOf(
            "uid" to uid,
            "imageUrl" to imageUrl,
            "title" to title,
            "description" to description,
            "time" to timeStamp
        )


        firestore.collection("upload_logs")
            .add(metadata)
            .addOnSuccessListener {
                _uploadState.value = UploadState.Success("Berhasil mengunggah dokumentasi")
            }
            .addOnFailureListener { e ->
                _uploadState.value = UploadState.Error(e.message ?: "Gagal menyimpan data ke firestore")
            }
    }

    fun resetUploadState() {
        _uploadState.value = UploadState.Idle
    }
}

