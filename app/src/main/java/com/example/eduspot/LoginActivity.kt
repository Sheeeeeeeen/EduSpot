package com.example.eduspot

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.eduspot.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Check if user is already logged in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // User is already logged in, go to MainActivity
            navigateToMainActivity()
            return
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Login button click
        binding.btnLogin.setOnClickListener {
            performLogin()
        }

        // Forgot password click
        binding.tvForgotPassword.setOnClickListener {
            Toast.makeText(this, "Forgot password feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        // Register link click
        binding.tvRegisterLink.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Enter key on password field
        binding.etPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performLogin()
                true
            } else {
                false
            }
        }
    }

    private fun performLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // Validate input
        if (email.isEmpty()) {
            binding.tilEmail.error = "Email is required"
            binding.etEmail.requestFocus()
            return
        } else {
            binding.tilEmail.error = null
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            binding.etPassword.requestFocus()
            return
        } else {
            binding.tilPassword.error = null
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Please enter a valid email"
            binding.etEmail.requestFocus()
            return
        } else {
            binding.tilEmail.error = null
        }

        // Show loading state
        binding.btnLogin.isEnabled = false
        binding.btnLogin.text = "Logging in..."

        // Simple validation (for demo purposes)
        // In production, use Firebase Auth
        if (email == "eduspot@gmail.com" && password == "123456") {
            // Success - navigate to MainActivity
            binding.btnLogin.isEnabled = true
            binding.btnLogin.text = "LOGIN"
            
            // Optionally sign in with Firebase (uncomment if you want Firebase Auth)
            // signInWithFirebase(email, password)
            
            // For now, just navigate directly
            navigateToMainActivity()
        } else {
            // Show error
            binding.btnLogin.isEnabled = true
            binding.btnLogin.text = "LOGIN"
            binding.tilPassword.error = "Invalid email or password"
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
        }
    }

    private fun signInWithFirebase(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                binding.btnLogin.isEnabled = true
                binding.btnLogin.text = "LOGIN"

                if (task.isSuccessful) {
                    // Sign in success
                    val user: FirebaseUser? = auth.currentUser
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    navigateToMainActivity()
                } else {
                    // Sign in failed
                    binding.tilPassword.error = "Invalid email or password"
                    Toast.makeText(
                        this,
                        "Authentication failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

