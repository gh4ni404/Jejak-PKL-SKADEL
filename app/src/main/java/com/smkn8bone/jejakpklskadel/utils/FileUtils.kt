package com.smkn8bone.jejakpklskadel.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object FileUtils {
//    fun getFileNameFromUri(context: Context, uri: Uri): String? {
//        var result: String? = null
//        if (uri.scheme == "content") {
//            val cursor = context.contentResolver.query(uri, null, null, null, null)
//            try {
//                cursor?.let {
//                    if (it.moveToFirst()) {
//                        result = it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
//                    }
//                }
//            } finally {
//                cursor?.close()
//
//            }
//        }
//        if (result == null) {
//            result = uri.path
//            val cut = result?.lastIndexOf('/')
//            if (cut != null && cut != -1) {
//                result = result?.substring(cut + 1)
//            }
//        }
//        return result
//    }

    fun compressImage(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val compressedFile = File.createTempFile("compressed_", ".jpg", context.cacheDir)
        val outputStream = FileOutputStream(compressedFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        outputStream.flush()
        outputStream.close()
        return compressedFile
    }
}