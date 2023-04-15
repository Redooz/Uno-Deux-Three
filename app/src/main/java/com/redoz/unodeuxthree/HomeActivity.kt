package com.redoz.unodeuxthree

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.redoz.unodeuxthree.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)

        setContentView(binding.root)

        supportActionBar?.hide()
    }
}