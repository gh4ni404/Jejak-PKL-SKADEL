package com.smkn8bone.jejakpklskadel.utils

import android.content.Context
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.googleapis.media.MediaHttpUploader
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.io.File as JavaFile

object DriveServiceHelper {
    fun getDriveService(context: Context): Drive? {
        return try {
            val assetManager = context.assets
            val inputStream: InputStream = assetManager.open("service_account.json")

            val credential = GoogleCredential.fromStream(inputStream)
                .createScoped(listOf("https://www.googleapis.com/auth/drive"))

            Drive.Builder(
                NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                credential
            )
                .setApplicationName("Jejak PKL SKADEL")
                .build()
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } catch (e: GoogleJsonResponseException) {
            e.printStackTrace()
            null
        }
    }

    fun checkFolderProfile (
        driveService: Drive,
        parentFolderId: String,
        onResult: (String?) -> Unit
    ) {
        Thread {
            try {
                val query = "name = 'StudentProfile' and mimeType = 'application/vnd.google-apps.folder' and '$parentFolderId' in parents and trashed = false"
                val result = driveService.files().list().setQ(query).setSpaces("drive").setFields("files(id, name)").execute()

                if (result.files.isNotEmpty()) {
                    val folderId = result.files[0].id
                    onResult(folderId)
                } else {
                    val metadata = File().apply {
                        name = "StudentProfile"
                        mimeType = "application/vnd.google-apps.folder"
                        parents = listOf(parentFolderId)
                    }

                    val createdFolder = driveService.files().create(metadata).setFields("id").execute()
                    onResult(createdFolder.id)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(null)
            }
        }.start()
    }

    fun checkFolderDocumentation (
        driveService: Drive,
        folderName: String,
        parentFolderId: String,
        onResult: (String?) -> Unit
    ) {
        Thread {
            try {
                val query = "name = '${folderName}' and mimeType = 'application/vnd.google-apps.folder' and '$parentFolderId' in parents and trashed = false"
                val result = driveService.files().list().setQ(query).setSpaces("drive").setFields("files(id, name)").execute()

                if (result.files.isNotEmpty()) {
                    val folderId = result.files[0].id
                    onResult(folderId)
                } else {
                    val metadata = File().apply {
                        name = folderName
                        mimeType = "application/vnd.google-apps.folder"
                        parents = listOf(parentFolderId)
                    }

                    val createdFolder = driveService.files().create(metadata).setFields("id").execute()
                    onResult(createdFolder.id)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(null)
            }
        }.start()
    }

    fun checkFolderClass(driveService: Drive, className: String, rootId: String, callback: (String?) -> Unit) {
        checkFolderDocumentation(driveService, className, rootId, callback)
    }

    fun checkFolderStudent(driveService: Drive, studentName: String, classFolderId: String, callback: (String?) -> Unit) {
        checkFolderDocumentation(driveService, studentName, classFolderId, callback)
    }

    fun generateFileName(studentName: String, activityName: String, uid: String): String {
        val timeStamp = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        return "${timeStamp}_${studentName}_${activityName}_${uid}.jpg"
    }

    fun uploadFile (driveService: Drive, javaFile: JavaFile, fileName: String, parentFolderId: String): String? {
        val mediaContent = FileContent("image/jpeg", javaFile)
        val metadata = File().apply {
            name = fileName
            parents = listOf(parentFolderId)
        }

        val file = driveService.files().create(metadata, mediaContent).setFields("id")

        val uploader = file.mediaHttpUploader
        uploader.isDirectUploadEnabled = false
        uploader.chunkSize = MediaHttpUploader.MINIMUM_CHUNK_SIZE
        val uploadedFile = file.execute()
        return uploadedFile.id
    }
}