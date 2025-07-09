package com.smkn8bone.jejakpklskadel.utils

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CameraUtils {

    fun createImageFile(context: Context): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}",
            ".jpg",
            storageDir
        )
    }

    fun getUriForFile(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }

    private fun hasCameraPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission (
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun shouldShowCameraRationale(fragment: Fragment): Boolean {
        return fragment.shouldShowRequestPermissionRationale (Manifest.permission.CAMERA)
    }

    private fun showPermissionDialog(
        context: Context,
        onPositive: () -> Unit,
        onNegative: () -> Unit = {}
    ) {
        AlertDialog.Builder (context)
            .setTitle ("Izin Kamera Dibutuhkan")
            .setMessage ("Jejak PKL SKADEL membutuhkan izin kamera untuk mengambil foto")
            .setPositiveButton ("OK") { _, _ -> onPositive() }
            .setNegativeButton ("Batal") { dialog, _ ->
                dialog.dismiss()
                onNegative()
            }
            .show()
    }

    fun checkAndRequestCameraPermission(
        fragment: Fragment,
        requestPermissionLauncher: ActivityResultLauncher<String>,
        onGranted: () -> Unit
    ) {
        val context = fragment.requireContext()
        when {
            hasCameraPermission(context) -> {
                onGranted()
            }

            shouldShowCameraRationale(fragment) -> {
                showPermissionDialog(
                    context,
                    onPositive = {requestPermissionLauncher.launch(Manifest.permission.CAMERA)}
                )
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
}