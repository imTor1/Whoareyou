package com.example.whoareyou

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toolbar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class confrimUpphotoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_confrim_upphoto)

        // Set window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Find the buttons
        val allowButton = findViewById<Button>(R.id.allow_button)
        val denyButton = findViewById<Button>(R.id.deny_button)

        // Set onClickListener for "อนุญาต" button
        allowButton.setOnClickListener {
            val intent = Intent(this, AddphotoActivity::class.java)
            startActivity(intent)
        }
        // Set onClickListener for "ไม่อนุญาต" button
        denyButton.setOnClickListener {
            finish()  // Close the activity and stop the app
        }


    }

}
