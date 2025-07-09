package com.smkn8bone.jejakpklskadel.ui.completeprofile

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.R
import com.smkn8bone.jejakpklskadel.MainActivity
import com.smkn8bone.jejakpklskadel.databinding.FragmentCompleteProfileBinding
import com.smkn8bone.jejakpklskadel.utils.CameraUtils
import com.smkn8bone.jejakpklskadel.utils.DriveServiceHelper
import java.io.File

class CompleteProfileFragment : Fragment() {
    private var _binding: FragmentCompleteProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CompleteProfileViewModel by viewModels()

    private lateinit var cameraUtils: CameraUtils
    private var imageFile: File? = null
    private lateinit var cameraImageUri: Uri
    private var selectedImageUri: Uri? = null

    private val dataKelas = mapOf (
        "XII DKV" to KelasData ("Desain Komunikasi Visual",
            listOf(
                IndustriData("Percetakan FIKHA", listOf("Muh. Haris, S. T")),
                IndustriData("WIM Printing", listOf("Mohd. Abdul Ghani, S. Kom", "A. Yulismayasanti, S. Pd")),
                IndustriData("Faizal Digital Printing", listOf("Marlina, S. Pd")),
                IndustriData("Unit Prod DKV", listOf("Rahmi Damayanti Nur DP, S. Pd")),
                IndustriData("BRI Unit Uloe", listOf("A. Yulismayasanti, S. Pd")),
                IndustriData("Kantor Desa Welado", listOf("Sapril, S. Pd")),
                IndustriData("Kantor Desa P. Pute", listOf("Besse Tenri, S. Pd"))
            )
        ),
        "XII MPLB" to KelasData ("Manajemen Perkantoran dan Layanan Bisnis",
            listOf(
                IndustriData("Kantor Camat Ajangale", listOf("Rahmawati, S. Sos")),
                IndustriData("BKKBN Ajangale", listOf("Rahmawati, S. Sos")),
                IndustriData("Kantor Desa Welado", listOf("Herlina, S. Sos")),
                IndustriData("SMKN 8 Bone", listOf("Herlina, S. Sos")),
                IndustriData("SMPN 2 Ajangale", listOf("Herlina, S. Sos")),
                IndustriData("MTsN 2 Bone", listOf("Titik Sulistiawati, S. Pd")),
                IndustriData("BRI Pompanua", listOf("Titik Sulistiawati, S. Pd")),
                IndustriData("KUA Ajangale", listOf("Titik Sulistiawati, S. Pd")),
                IndustriData("Puskesmas Ajangale", listOf("Nur Illang, S. Pd")),
                IndustriData("Kantor Camat Dua Boccoe", listOf("Nur Illang, S. Pd")),
                IndustriData("SMKN 8 Bone", listOf("Nur Illang, S. Pd"))
            )
        ),
        "XII AT" to KelasData ("Agribisnis Tanaman",
            listOf(
                IndustriData("P4S Lampoko", listOf("Elite Gizwati Samudry, S. Pd", "Sulfadli, S. Pd. I")),
                IndustriData("BPP Dua Boccoe", listOf("Jufri Adi Ya Fadli, S. Pd")),
                IndustriData("BPP Ajangale", listOf("Jufri Adi Ya Fadli, S. Pd", "A. Eva Kusumanegara, S. Pd", "Jumiati Enre, S.Pd")),
                IndustriData("Unit Prod DKV", listOf("Rahmi Damayanti Nur DP, S. Pd"))
            )
        )
    )

    data class KelasData (
        val jurusan: String,
        val industri: List<IndustriData>
    )

    data class IndustriData (
        val namaIndustri: String,
        val guruPembimbing: List<String>
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

        cameraUtils = CameraUtils

        setupDropdowns()
        setupProfileImageSelection()
        observeUserData()

        binding.btnSaveCompleteProfile.setOnClickListener {
            val selectedKelas = binding.etClassCompleteProfile.text.toString().trim()
            val selectedJurusan = binding.etJurusanCompleteProfile.text.toString().trim()
            val selectedIndustri = binding.etIndustryCompleteProfile.text.toString().trim()
            val selectedTeacher = binding.etGuruPembimbingCompleteProfile.text.toString().trim()
            val phoneNumber = binding.etPhoneCompleteProfile.text.toString().trim()
            val schoolName = binding.etSekolahCompleteProfile.text.toString().trim()

            if (selectedKelas.isEmpty() || selectedJurusan.isEmpty() || selectedIndustri.isEmpty() || selectedTeacher.isEmpty() || phoneNumber.isEmpty()) {
                Toast.makeText(requireContext(), "Mohon isi semua data", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.saveProfile(
                selectedKelas,
                selectedJurusan,
                selectedIndustri,
                selectedTeacher,
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
            R.layout.support_simple_spinner_dropdown_item,
            classList
        )
        binding.etClassCompleteProfile.setAdapter (adapterClass)
        binding.etClassCompleteProfile.setOnItemClickListener {_, _, position, _ ->
            val selectedClass = classList[position]
            val kelasData = dataKelas[selectedClass] ?: return@setOnItemClickListener
            val industri = kelasData.industri.map { it.namaIndustri }
            val adapterIndustri = ArrayAdapter (
                requireContext(),
                R.layout.support_simple_spinner_dropdown_item,
                industri
            )

            binding.etJurusanCompleteProfile.setText(kelasData.jurusan, false)
            binding.etIndustryCompleteProfile.setAdapter (adapterIndustri)
            binding.etIndustryCompleteProfile.setText("", false)

            binding.etIndustryCompleteProfile.setOnItemClickListener { _, _, industryPosition, _ ->
                val selectedIndustry = kelasData.industri[industryPosition]
                val adapterGuru = ArrayAdapter (
                    requireContext(),
                    R.layout.support_simple_spinner_dropdown_item,
                    selectedIndustry.guruPembimbing
                )

                Log.d("guru", selectedIndustry.guruPembimbing.size.toString())
                Log.d("guru", selectedIndustry.guruPembimbing[0])
                if (selectedIndustry.guruPembimbing.size == 1) {
                    binding.etGuruPembimbingCompleteProfile.setText(selectedIndustry.guruPembimbing[0], false)
                    binding.etGuruPembimbingCompleteProfile.setAdapter(null)
                } else {
                    binding.etGuruPembimbingCompleteProfile.setAdapter(adapterGuru)
                    binding.etGuruPembimbingCompleteProfile.setText("", false)
                }
            }
        }

        val dataSekolah = listOf ("SMKN 8 Bone")
        val adapterSekolah = ArrayAdapter (
            requireContext(),
            R.layout.support_simple_spinner_dropdown_item,
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
                    0 -> cameraUtils.checkAndRequestCameraPermission(
                        this, requestCamera,
                        onGranted = { openCamera() }
                    )
                    1 -> openGallery()
                }
            }
            .create()
        binding.ivSelectImageProfile.setOnClickListener { alertDialogProfile.show() }
    }

    private fun observeUserData() {
        viewModel.fetchUserData { name, email, phone, kelas, jurusan, industri, guruPembimbing, sekolah ->
            if (name != null && email != null || phone != null || kelas != null || jurusan != null || industri != null || guruPembimbing != null || sekolah != null) {
                binding.etNameCompleteProfile.setText(name)
                binding.etEmailCompleteProfile.setText(email)
                binding.etPhoneCompleteProfile.setText(phone)
                binding.etClassCompleteProfile.setText(kelas)
                binding.etJurusanCompleteProfile.setText(jurusan)
                binding.etIndustryCompleteProfile.setText(industri)
                binding.etGuruPembimbingCompleteProfile.setText(guruPembimbing)
                binding.etSekolahCompleteProfile.setText(sekolah)
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

    private fun uploadSelectedImage() {
        val uri = selectedImageUri ?: return
        val driveService = DriveServiceHelper.getDriveService(requireContext()) ?: return
        val sharedFolderId = "1euMwZDqrcKembVtpnim5pI0AMBwQqU-3"

        binding.loadingOverlay.show()
        DriveServiceHelper.checkFolderProfile(driveService, sharedFolderId) { folderId ->
            folderId?.let {
                viewModel.uploadImageToDrive(requireContext(), uri, it) { imageUri ->
                    requireActivity().runOnUiThread {

                        if(imageUri != null) {
                            binding.loadingOverlay.hide()
                            viewModel.setUploadedImageUri(imageUri)
                        } else {
                            binding.loadingOverlay.show()
                            Toast.makeText(requireContext(), "Gagal upload gambar", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } ?: run {
                requireActivity().runOnUiThread {
                    binding.loadingOverlay.show()
                    Toast.makeText(requireContext(), "Folder tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}