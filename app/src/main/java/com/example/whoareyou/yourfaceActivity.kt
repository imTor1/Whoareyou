package com.example.whoareyou

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class yourfaceActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_yourface)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.yourface)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val imageUriString = intent.getStringExtra("imageUri")
        val imageUri = Uri.parse(imageUriString)
        val imageView = findViewById<ImageView>(R.id.imageView)
        val back = findViewById<Button>(R.id.buttonback)
        val buttonage = findViewById<Button>(R.id.buttonage)
        val buttonstar = findViewById<Button>(R.id.buttonstar)
        back.setOnClickListener {
            finish()
        }
        imageView.setImageURI(imageUri)
        buttonage.setOnClickListener {
            val intent = android.content.Intent(this, ShowageActivity::class.java)
            intent.putExtra("imageUri", imageUriString.toString())
            startActivity(intent)
        }
        buttonstar.setOnClickListener {
            val intent = android.content.Intent(this, ShowcelebrityActivity::class.java)
            intent.putExtra("imageUri", imageUriString.toString())
            startActivity(intent)
        }


    }
}