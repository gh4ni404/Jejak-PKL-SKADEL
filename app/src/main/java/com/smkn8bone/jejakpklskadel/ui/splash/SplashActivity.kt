package com.smkn8bone.jejakpklskadel.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
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
//            if (user != null) {
//                FirebaseFirestore.getInstance().collection("users")
//                    .document(user.uid)
//                    .get()
//                    .addOnSuccessListener { doc ->
//                        val isComplete = document.getString("selec")
//                    }
//            }
            startActivity(
                Intent(this, if (user == null) AuthActivity::class.java else MainActivity::class.java)
            )
        }, SPLASH_TIME_OUT)
    }
}