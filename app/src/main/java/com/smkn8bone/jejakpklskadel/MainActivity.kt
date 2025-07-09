package com.smkn8bone.jejakpklskadel

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.smkn8bone.jejakpklskadel.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
//    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomNavView: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        bottomNavView = binding.bottomNav

        binding.bottomNav.setupWithNavController(navController)
//        val bottomNav = binding.bottomNav
//        NavigationUI.setupWithNavController(bottomNav, navController)

    }

    fun getBottomNavView(): BottomNavigationView {
        return binding.bottomNav
    }

}