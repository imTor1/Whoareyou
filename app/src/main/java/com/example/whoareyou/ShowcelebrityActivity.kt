package com.example.whoareyou

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.*
import java.io.File
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType

class ShowcelebrityActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_showcelebrity)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.celebrityyou)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val imageView = findViewById<ImageView>(R.id.imageViewmy)
        val celebrity_name = findViewById<TextView>(R.id.celebrity_name)
        val deny_button = findViewById<Button>(R.id.deny_button)
        val allow_button = findViewById<Button>(R.id.allow_button)
        deny_button.setOnClickListener {
            finish()
        }
        allow_button.setOnClickListener {
            val intent = Intent(this, AddphotoActivity::class.java)
            startActivity(intent)
        }

        val imageUriString = intent.getStringExtra("imageUri")
        val imageUri = Uri.parse(imageUriString)
        imageView.setImageURI(imageUri)

    }

    private fun uploadImageToServer(imageUri: Uri) {
        val file = File(imageUri.path)
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", file.name, RequestBody.create("image/jpeg".toMediaType(), file)) // ใช้ toMediaType()
            .build()

        val request = Request.Builder()
            .url("root_url")  // URL ของ API
            .post(requestBody)
            .build()

        val client = OkHttpClient()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // จัดการกรณีที่เกิดข้อผิดพลาด
                runOnUiThread {
                    Toast.makeText(this@ShowcelebrityActivity, "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                // จัดการผลลัพธ์จากเซิร์ฟเวอร์
                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(this@ShowcelebrityActivity, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@ShowcelebrityActivity, "Failed to upload image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}