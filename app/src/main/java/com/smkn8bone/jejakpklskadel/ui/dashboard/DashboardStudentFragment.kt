package com.smkn8bone.jejakpklskadel.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.smkn8bone.jejakpklskadel.AuthActivity
import com.smkn8bone.jejakpklskadel.MainActivity
import com.smkn8bone.jejakpklskadel.R
import com.smkn8bone.jejakpklskadel.databinding.FragmentDashboardStudentBinding

class DashboardStudentFragment : Fragment() {
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    private var _binding: FragmentDashboardStudentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardStudentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardStudentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupListeners()
        setupLogout()
    }

    private fun setupObservers() {
        // Nanti akan mengamati LiveData dari ViewModel
        viewModel.studentName.observe(viewLifecycleOwner) { name ->
            binding.tvGreeting.text = name
        }
        viewModel.photoUrl.observe(viewLifecycleOwner) { url ->
            Log.d("DashboardStudentFragment", "Photo URL: $url")
            if (!url.isNullOrEmpty()) {
                val thumbnail = "https://drive.google.com/thumbnail?id=$url"
                Glide.with(this)
                    .load(thumbnail)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .into(binding.ivAvatar)
            } else {
                binding.ivAvatar.setImageResource(R.drawable.ic_profile_placeholder)
            }
        }

        // Untuk observer review status atau menu lainnya nanti di sini
    }

    private fun setupListeners() {
        // Navigasi ke menu Upload
        binding.cardUpload.setOnClickListener {
            // Gunakan Navigation Component
//             findNavController().navigate(R.id.action_dashboard_to_upload)
            (requireActivity() as MainActivity).getBottomNavView().selectedItemId = R.id.uploadFragment
        }

        // Navigasi ke menu History
        binding.cardHistory.setOnClickListener {
            // findNavController().navigate(R.id.action_dashboard_to_history)
        }

        // Navigasi ke menu Profile
        binding.cardProfile.setOnClickListener {
            // findNavController().navigate(R.id.action_dashboard_to_profile)
        }
    }

    private fun setupLogout() {
        binding.btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(requireContext(), AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}