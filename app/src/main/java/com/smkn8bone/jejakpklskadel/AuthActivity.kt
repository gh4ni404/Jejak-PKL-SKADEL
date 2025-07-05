package com.smkn8bone.jejakpklskadel

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment

class AuthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        val shouldRedirect = intent.getBooleanExtra("redirectToCompleteProfile", false)
        if (shouldRedirect) {
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.auth_nav_host_fragment) as? NavHostFragment

            val navController = navHostFragment?.navController
            navController?.navigate(R.id.completeProfileFragment)
        }
    }
}