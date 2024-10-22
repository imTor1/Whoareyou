package com.example.whoareyou

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class homeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)  // ตรวจสอบว่า activity_home คือ XML ที่ใช้อยู่
        enableEdgeToEdge()
        // ผูกปุ่ม playButton กับการทำงาน
        val playButton: ImageView = findViewById(R.id.playButton)

        playButton.setOnClickListener {
            // เมื่อกดปุ่ม ให้เปลี่ยนไปหน้า activity_confirm_upphoto
            val intent = Intent(this, confrimUpphotoActivity  ::class.java)
            startActivity(intent)
        }
    }
}
