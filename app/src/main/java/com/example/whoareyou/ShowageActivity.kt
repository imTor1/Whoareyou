package com.example.whoareyou

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ShowageActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_showage)

        // Adjust padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.showage)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Find the image view and buttons
        val imageView = findViewById<ImageView>(R.id.imageView)
        val home = findViewById<Button>(R.id.buttonupload)
        val back = findViewById<Button>(R.id.buttonback)

        // Get the image URI from the intent
        val imageUriString = intent.getStringExtra("imageUri")
        val imageUri = Uri.parse(imageUriString)
        imageView.setImageURI(imageUri)

        // Set listener for back button to finish activity
        back.setOnClickListener {
            finish()
        }

        // Set listener for upload button to navigate to AddphotoActivity
        home.setOnClickListener {
            val intent = Intent(this, AddphotoActivity::class.java)
            startActivity(intent)
        }
    }
}
