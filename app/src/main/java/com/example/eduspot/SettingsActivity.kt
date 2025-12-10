package com.example.eduspot

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.eduspot.databinding.ActivitySettingsBinding
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        setupClickListeners()
        setupBottomNavigation()
    }

    private fun setupClickListeners() {
        // Back arrow click - go back to MainActivity
        binding.ivBack.setOnClickListener {
            finish() // This will go back to MainActivity
        }

        // Logout button click
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }

        // Edit Profile click
        binding.btnEditProfile.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        // Update Password click
        binding.btnUpdatePassword.setOnClickListener {
            Toast.makeText(this, "Update Password feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        // Push Notifications click
        binding.btnPushNotifications.setOnClickListener {
            Toast.makeText(this, "Push Notifications settings coming soon!", Toast.LENGTH_SHORT).show()
        }

        // Room Status Changes click
        binding.btnRoomStatusChanges.setOnClickListener {
            Toast.makeText(this, "Room Status Changes settings coming soon!", Toast.LENGTH_SHORT).show()
        }

        // Help click
        binding.tvHelp.setOnClickListener {
            Toast.makeText(this, "Help section coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        // Sign out from Firebase
        auth.signOut()

        // Navigate to LoginActivity and clear back stack
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
        
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
    }

    private fun setupBottomNavigation() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                com.example.eduspot.R.id.nav_home -> {
                    // Navigate to MainActivity
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                    true
                }
                com.example.eduspot.R.id.nav_notifications -> {
                    Toast.makeText(this, "Notifications feature coming soon!", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }
}

