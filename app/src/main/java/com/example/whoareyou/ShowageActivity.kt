package com.example.whoareyou

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONException
import org.json.JSONObject

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

        val imageView: ImageView = findViewById(R.id.imageView)
        val uploadButton: Button = findViewById(R.id.buttonupload) // ปุ่มสำหรับอัปโหลด
        val backButton: Button = findViewById(R.id.buttonback)
        val ageTextView: TextView = findViewById(R.id.textViewAge) // TextView สำหรับแสดงอายุ

        // Get image URI from the intent
        val imageUriString = intent.getStringExtra("imageUri")
        val imageUri = Uri.parse(imageUriString)

        imageView.setImageURI(imageUri)

        backButton.setOnClickListener {
            finish()
        }

        uploadButton.setOnClickListener {
            // เปลี่ยนการทำงานของปุ่มให้เปิด AddphotoActivity
            val intent = Intent(this, AddphotoActivity::class.java)
            startActivity(intent)
        }

        // หากต้องการให้ทำนายอายุโดยอัตโนมัติเมื่อ Activity ถูกเปิด
        imageUri?.let { uri ->
            val file = getFileFromUri(uri)
            file?.let {
                uploadImage(it, ageTextView)
            } ?: run {
                Toast.makeText(this, "Failed to get image file", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Toast.makeText(this, "Image URI is invalid", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getFileFromUri(uri: Uri): File? {
        try {
            val contentResolver = contentResolver
            val inputStream = contentResolver.openInputStream(uri)
            if (inputStream != null) {
                val file = File(cacheDir, "uploaded_image.jpg")
                inputStream.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                return file
            } else {
                Log.e("ShowageActivity", "InputStream is null")
            }
        } catch (e: Exception) {
            Log.e("ShowageActivity", "Failed to get file from URI: ${e.message}", e)
        }
        return null
    }

    private fun uploadImage(imageFile: File, ageTextView: TextView) {
        // Create request body
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", imageFile.name, imageFile.asRequestBody("image/jpeg".toMediaType()))
            .build()

        // API URL
        val url = resources.getString(R.string.root_url) + "/ai/predict/age"
        Log.d("ShowageActivity", "Uploading image to $url")

        // Create the request
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val client = OkHttpClient()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ShowageActivity", "Failed to upload image: ${e.localizedMessage}")
                runOnUiThread {
                    Toast.makeText(this@ShowageActivity, "Failed to upload image: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    Log.d("ShowageActivity", "Response: $responseBody")
                    runOnUiThread {
                        responseBody?.let {
                            try {
                                val jsonObject = JSONObject(it)
                                val predictedAge = jsonObject.getDouble("predicted_age")
                                Log.d("ShowageActivity", "Predicted age: $predictedAge") // Log อายุที่ทำนาย
                                ageTextView.text = "${predictedAge.toInt()} ปี" // แสดงอายุที่ทำนาย
                                Toast.makeText(this@ShowageActivity, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                            } catch (e: JSONException) {
                                Log.e("ShowageActivity", "Failed to parse response: ${e.message}")
                                Toast.makeText(this@ShowageActivity, "Failed to parse response: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    val errorBody = response.body?.string() ?: "Unknown error"
                    Log.e("ShowageActivity", "Error: $errorBody")
                    runOnUiThread {
                        Toast.makeText(this@ShowageActivity, "Error: $errorBody", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

}
