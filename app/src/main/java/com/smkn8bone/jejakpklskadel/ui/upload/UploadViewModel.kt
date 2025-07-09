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

    private fun saveMetadata(uid: String, name: String, kelas: String, imageUrl: String, title: String, description: String) {
        val timeStamp = System.currentTimeMillis()
        val metadata = hashMapOf(
            "uid" to uid,
            "nama" to name,
            "kelas" to kelas,
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

    fun uploadFullDocumentation(
        context: Context,
        imageUri: Uri,
        title: String,
        description: String,
        userName: String,
        className: String,
        parentFolderId: String
    ) {
        val driveService = DriveServiceHelper.getDriveService(context)
        val uid = auth.currentUser?.uid

        if (driveService == null || uid == null) {
            _uploadState.postValue(UploadState.Error("Gagal mengakses Google Drive"))
            return
        }

        _uploadState.postValue(UploadState.Loading)

        Thread {
            try {
                val compressedFile = FileUtils.compressImage(context, imageUri)
                val fileName = DriveServiceHelper.generateFileName(title, userName, uid)

                // 1. Cek folder root: Documentation
                DriveServiceHelper.checkFolderDocumentation(driveService, "Documentation", parentFolderId) { rootId ->
                    if (rootId == null) {
                        _uploadState.postValue(UploadState.Error("Folder 'Documentation' tidak ditemukan"))
                        return@checkFolderDocumentation
                    }

                    // 2. Cek folder kelas
                    DriveServiceHelper.checkFolderClass(driveService, className, rootId) { classFolderId ->
                        if (classFolderId == null) {
                            _uploadState.postValue(UploadState.Error("Folder kelas tidak ditemukan"))
                            return@checkFolderClass
                        }

                        // 3. Cek folder nama siswa
                        DriveServiceHelper.checkFolderStudent(driveService, userName, classFolderId) { studentFolderId ->
                            if (studentFolderId == null) {
                                _uploadState.postValue(UploadState.Error("Folder siswa tidak ditemukan"))
                                return@checkFolderStudent
                            }

                            // 4. Upload gambar ke folder siswa
                            val uploadedFileId = DriveServiceHelper.uploadFile(driveService, compressedFile, fileName, studentFolderId)

                            if (uploadedFileId != null) {
                                saveMetadata(uid, userName,className, uploadedFileId, title, description)
                            } else {
                                _uploadState.postValue(UploadState.Error("Gagal mengunggah gambar"))
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uploadState.postValue(UploadState.Error("Terjadi kesalahan saat mengunggah"))
            }
        }.start()
    }

    fun resetUploadState() {
        _uploadState.value = UploadState.Idle
    }
}

