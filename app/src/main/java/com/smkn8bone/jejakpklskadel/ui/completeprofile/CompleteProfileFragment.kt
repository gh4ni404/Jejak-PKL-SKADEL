package com.smkn8bone.jejakpklskadel.ui.completeprofile

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.smkn8bone.jejakpklskadel.MainActivity
import com.smkn8bone.jejakpklskadel.databinding.FragmentCompleteProfileBinding
import com.smkn8bone.jejakpklskadel.utils.DriveServiceHelper
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CompleteProfileFragment : Fragment() {
    private var _binding: FragmentCompleteProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CompleteProfileViewModel by viewModels()

    private lateinit var cameraImageUri: Uri
    private var selectedImageUri: Uri? = null

    private val dataKelas = mapOf (
        "XII DKV" to Pair ("Desain Komunikasi Visual", listOf("Item A", "Item B", "Item C")),
        "XII MPLB" to Pair ("Manajemen Perkantoran dan Layanan Bisnis", listOf("Item 1", "Item 2", "Item 3")),
        "XII AT" to Pair ("Agribisnis Tanaman", listOf("Item I", "Item II", "Item III"))
    )

    private val takePictureLauncher = registerForActivityResult (ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            binding.ivProfileCompleteProfile.setImageURI (cameraImageUri)
            selectedImageUri = cameraImageUri
            uploadSelectedImage()
        }
    }
    private val pickGalleryLauncher = registerForActivityResult (ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            binding.ivProfileCompleteProfile.setImageURI (it)
            selectedImageUri = it
            uploadSelectedImage()
        }
    }

    private val requestCamera = registerForActivityResult (ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted){
            openCamera()
        } else {
            Toast.makeText (
                requireContext(),
                "Izin Kamera ditolak",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCompleteProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDropdowns()
        setupProfileImageSelection()
        observeUserData()

        binding.btnSaveCompleteProfile.setOnClickListener {
            val selectedKelas = binding.etClassCompleteProfile.text.toString().trim()
            val selectedJurusan = binding.etJurusanCompleteProfile.text.toString().trim()
            val selectedIndustri = binding.etIndustryCompleteProfile.text.toString().trim()
            val phoneNumber = binding.etPhoneCompleteProfile.text.toString().trim()
            val schoolName = binding.etSekolahCompleteProfile.text.toString().trim()

            if (selectedKelas.isEmpty() || selectedJurusan.isEmpty() || selectedIndustri.isEmpty() || phoneNumber.isEmpty()) {
                Toast.makeText(requireContext(), "Mohon isi semua data", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.saveProfile(
                selectedKelas,
                selectedJurusan,
                selectedIndustri,
                phoneNumber,
                schoolName
            ) { success, message ->
                if (success) {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

                    val intent = Intent(requireContext(), MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                } else {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupDropdowns() {
        val classList = dataKelas.keys.toList()
        val adapterClass = ArrayAdapter (
            requireContext(),
            com.google.android.material.R.layout.support_simple_spinner_dropdown_item,
            classList
        )
        binding.etClassCompleteProfile.setAdapter (adapterClass)
        binding.etClassCompleteProfile.setOnItemClickListener {_, _, position, _ ->
            val selectedClass = classList[position]
            val (jurusan, industri) = dataKelas[selectedClass] ?: return@setOnItemClickListener
            val adapterIndustri = ArrayAdapter (
                requireContext(),
                com.google.android.material.R.layout.support_simple_spinner_dropdown_item,
                industri
            )

            binding.etJurusanCompleteProfile.setText(jurusan, false)
            binding.etIndustryCompleteProfile.setAdapter (adapterIndustri)
            binding.etIndustryCompleteProfile.setText("", false)
        }

        val dataSekolah = listOf ("SMKN 8 Bone")
        val adapterSekolah = ArrayAdapter (
            requireContext(),
            com.google.android.material.R.layout.support_simple_spinner_dropdown_item,
            dataSekolah
        )
        binding.etSekolahCompleteProfile.setAdapter (adapterSekolah)
    }

    private fun setupProfileImageSelection() {
        val options = arrayOf ("Kamera", "Galeri")
        val alertDialogProfile = AlertDialog
            .Builder (requireContext())
            .setTitle ("Pilih sumber gambar")
            .setItems (options) { _, which ->
                when (which) {
                    0 -> checkCamera()
                    1 -> openGallery()
                }
            }
            .create()
        binding.ivSelectImageProfile.setOnClickListener { alertDialogProfile.show() }
    }

    private fun observeUserData() {
        viewModel.fetchUserData { name, email ->
            if (name != null && email != null) {
                binding.etNameCompleteProfile.setText(name)
                binding.etEmailCompleteProfile.setText(email)
            }
        }
    }

    private fun checkCamera() {
        when {
            ContextCompat.checkSelfPermission (requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            shouldShowRequestPermissionRationale (Manifest.permission.CAMERA) -> {
                AlertDialog.Builder (requireContext())
                    .setTitle ("Izin Kamera Dibutuhkan")
                    .setMessage ("Jejak PKL SKADEL membutuhkan izin kamera untuk mengambil foto")
                    .setPositiveButton ("OK") { _, _ -> requestCamera.launch (Manifest.permission.CAMERA) }
                    .setNegativeButton ("Batal") { dialog, _ -> dialog.dismiss() }
                    .show()
            }
            else -> {
                requestCamera.launch (Manifest.permission.CAMERA)
            }
        }
    }

    private fun openGallery() {
        pickGalleryLauncher.launch("image/*")
    }

    private fun openCamera() {
        val imageFile = createImageFile()
        cameraImageUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            imageFile
        )
        takePictureLauncher.launch(cameraImageUri)
    }

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}",
            ".jpg",
            storageDir
        )
    }

    private fun uploadSelectedImage() {
        val uri = selectedImageUri ?: return
        val driveService = DriveServiceHelper.getDriveService(requireContext()) ?: return
        val sharedFolderId = "1euMwZDqrcKembVtpnim5pI0AMBwQqU-3"

        DriveServiceHelper.checkFolderProfile(driveService, sharedFolderId) { folderId ->
            folderId?.let {
                viewModel.uploadImageToDrive(requireContext(), uri, it) { imageUri ->
                    viewModel.setUploadedImageUri(imageUri)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}