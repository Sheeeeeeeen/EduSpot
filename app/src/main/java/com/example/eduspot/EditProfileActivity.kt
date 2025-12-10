package com.example.eduspot

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.eduspot.databinding.ActivityEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        loadUserData()

        binding.btnSave.setOnClickListener {
            saveUserData()
        }

        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    private fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("Users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    binding.etFullName.setText(doc.getString("fullName"))
                    binding.etBio.setText(doc.getString("bio"))
                    binding.etPhone.setText(doc.getString("phone"))
                }
            }
    }

    private fun saveUserData() {
        val uid = auth.currentUser?.uid ?: return

        val data = mapOf(
            "fullName" to binding.etFullName.text.toString(),
            "bio" to binding.etBio.text.toString(),
            "phone" to binding.etPhone.text.toString()
        )

        db.collection("Users")
            .document(uid)
            .update(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error saving data!", Toast.LENGTH_SHORT).show()
            }
    }
}
