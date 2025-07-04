package com.smkn8bone.jejakpklskadel.utils

import android.content.Context
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import java.io.IOException
import java.io.InputStream
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

//    fun uploadFile (context: Context, driveService: Drive, javaFile: JavaFile, fileName: String, parentFolderId: String): String? {
    fun uploadFile (driveService: Drive, javaFile: JavaFile, fileName: String, parentFolderId: String): String? {
//        val contentResolver = context.contentResolver
//        val inputStream = contentResolver.openInputStream(uri) ?: return null
//        val mimeType = contentResolver.getType(uri) ?: "image/jpeg"

//        val mediaContent = InputStreamContent(mimeType, inputStream)
        val mediaContent = FileContent("image/jpeg", javaFile)

        val metadata = File().apply {
            name = fileName
            parents = listOf(parentFolderId)
        }

        val file = driveService.files().create(metadata, mediaContent).setFields("id").execute()

        return file.id
    }
}