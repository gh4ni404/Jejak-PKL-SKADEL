package com.smkn8bone.jejakpklskadel.ui.upload

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.smkn8bone.jejakpklskadel.R
import com.smkn8bone.jejakpklskadel.databinding.FragmentUploadBinding
import com.smkn8bone.jejakpklskadel.utils.CameraUtils
import java.io.File

class UploadFragment : Fragment() {
    private var _binding: FragmentUploadBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UploadViewModel by viewModels()

    private lateinit var cameraUtils: CameraUtils
    private var imageFile: File? = null
    private lateinit var cameraImageUri: Uri
    private var selectedImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUploadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraUtils = CameraUtils

        setupObservers()
        setupListeners()
    }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            binding.ivPreviewUpload.setImageURI(cameraImageUri)
            selectedImageUri = cameraImageUri
//            uploadSelectedImage()
        }
    }
    private val pickGalleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            binding.ivPreviewUpload.setImageURI(it)
            selectedImageUri = it
//            uploadSelectedImage()
        }
    }
    private val requestCamera = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(requireContext(), "Izin Kamera ditolak", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupObservers() {
        // Nanti akan digunakan untuk memantau status upload, loading, dll
        viewModel.uploadState.observe(viewLifecycleOwner) { state ->
            // Tampilkan status sesuai keadaan (misal loading, sukses, gagal)
            when (state) {
                is UploadViewModel.UploadState.Loading -> {
                    binding.loadingOverlay.show()
                    binding.btnUpload.isEnabled = false
                }
                is UploadViewModel.UploadState.Success -> {
                    binding.loadingOverlay.hide()
                    binding.btnUpload.isEnabled = true
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                    // reset form
                    resetForm()
                    viewModel.resetUploadState()
                }
                is UploadViewModel.UploadState.Error -> {
                    binding.loadingOverlay.hide()
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()

                    viewModel.resetUploadState()
                }
                UploadViewModel.UploadState.Idle -> {
                    binding.loadingOverlay.hide()
                    binding.btnUpload.isEnabled = false
                }
            }
        }
    }

    private fun setupListeners() {
        val options = arrayOf("Kamera", "Galeri")
        val alertDialogUpload = AlertDialog
            .Builder(requireContext())
            .setTitle("Pilih Sumber Gambar")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> cameraUtils.checkAndRequestCameraPermission(
                        this, requestCamera,
                        onGranted = { openCamera() }
                    )
                    1 -> openGallery()
                }
            }
            .create()

        // Tombol pilih gambar
        binding.ivSelectImageUpload.setOnClickListener {
            // Trigger buka kamera atau galeri
            alertDialogUpload.show()
        }

        // Tombol upload
        binding.btnUpload.setOnClickListener {
            val titleDocumented = binding.etTitleUpload.text.toString().trim()
            val description = binding.etDescriptionUpload.text.toString().trim()

            if (selectedImageUri == null) {
                Toast.makeText(requireContext(), "Silahkan pilih gambar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (titleDocumented.isEmpty()) {
                Toast.makeText(requireContext(), "Isi Judul Dokumentasi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (description.isEmpty()) {
                Toast.makeText(requireContext(), "Isi Deskripsi Dokumentasi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.fetchUserData { name, _, _, kelas, _, _, _, _ ->
                if (name != null && kelas != null) {
                    val sharedFolderId = "1euMwZDqrcKembVtpnim5pI0AMBwQqU-3"
                    viewModel.uploadFullDocumentation(
                        context = requireContext(),
                        imageUri = selectedImageUri!!,
                        title = titleDocumented,
                        description = description,
                        userName = name,
                        className = kelas,
                        parentFolderId = sharedFolderId
                    )
                } else {
                    Toast.makeText(requireContext(), "Data pengguna tidak lengkap", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun openGallery() {
        pickGalleryLauncher.launch("image/*")
    }

    private fun openCamera() {
        imageFile = cameraUtils.createImageFile(requireContext())
        cameraImageUri = cameraUtils.getUriForFile(requireContext(), imageFile!!)
        takePictureLauncher.launch(cameraImageUri)
    }

    private fun resetForm() {
        selectedImageUri = null
        binding.etTitleUpload.setText("")
        binding.etDescriptionUpload.setText("")
        binding.ivPreviewUpload.setImageResource(R.drawable.ic_image_placeholder)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
