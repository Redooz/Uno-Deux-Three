package com.redoz.unodeuxthree

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.redoz.unodeuxthree.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) = try {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.txtViewSignUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

    } catch (ex: Exception) {
        error(ex)
    }
}