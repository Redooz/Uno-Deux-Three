package com.redoz.unodeuxthree

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.redoz.unodeuxthree.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) = try {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        mAuth = FirebaseAuth.getInstance()

        binding.txtViewLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.btnSignup.setOnClickListener {
            val username = binding.txtUserName.text.toString().trim()
            val email = binding.txtEmail.text.toString().trim()
            val password = binding.txtPassword.text.toString().trim()

            signUp(username, email, password)
        }
    } catch (ex: Exception) {
        error(ex)
    }

    private fun signUp(username: String, email: String, password: String) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // code for jumping to home activity
                    mAuth.currentUser?.let { addUserToDB(username, email, it.uid) }

                    val intent = Intent(this@SignUpActivity, HomeActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this@SignUpActivity, "Authentication error", Toast.LENGTH_LONG)
                        .show()
                }
            }

    }

    private fun addUserToDB(username: String, email: String, uid: String) {
        mDbRef = FirebaseDatabase.getInstance().reference
        val newUser = User(username,email,uid)

        mDbRef.child("users").child(uid).setValue(newUser)
    }
}