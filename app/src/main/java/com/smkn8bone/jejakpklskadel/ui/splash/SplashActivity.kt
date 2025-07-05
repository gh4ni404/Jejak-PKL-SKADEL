package com.smkn8bone.jejakpklskadel.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.smkn8bone.jejakpklskadel.AuthActivity
import com.smkn8bone.jejakpklskadel.MainActivity
import com.smkn8bone.jejakpklskadel.R
import com.smkn8bone.jejakpklskadel.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    private val SPLASH_TIME_OUT: Long = 3000
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val user = FirebaseAuth.getInstance().currentUser

        binding.ivLogo.startAnimation(
            AnimationUtils
                .loadAnimation(this, R.anim.scale_animation)
        )


        Handler(Looper.getMainLooper()).postDelayed({
            binding.splashRootLayout.startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.fade_out)
            )
            if (user != null) {
                FirebaseFirestore.getInstance().collection("users").document(user.uid).get()
                    .addOnSuccessListener { doc ->
                        val isComplete = doc.getString("image_profile") != null &&
                                doc.getString("industri") != null &&
                                doc.getString("kelas") != null &&
                                doc.getString("phone") != null &&
                                doc.getString("sekolah") != null

                        val nextIntent = if (isComplete) {
                            Intent(this, MainActivity::class.java)
                        } else {
                            Intent(this, AuthActivity::class.java).apply{
                                putExtra("redirectToCompleteProfile", true)
                            }
                        }
                        startActivity(nextIntent)
                    }
                    .addOnFailureListener {
                        startActivity(
                            Intent(this@SplashActivity, AuthActivity::class.java)
                        )
                    }
            } else {
                startActivity(
                    Intent(this@SplashActivity, AuthActivity::class.java)
                )
            }
//            startActivity(
//                Intent(this, if (user == null) AuthActivity::class.java else MainActivity::class.java)
//            )
        }, SPLASH_TIME_OUT)
    }
}