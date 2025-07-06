package com.smkn8bone.jejakpklskadel

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.smkn8bone.jejakpklskadel.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
//    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }

}