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
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

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
        val denyButton = findViewById<Button>(R.id.deny_button)
        val allowButton = findViewById<Button>(R.id.allow_button)

        denyButton.setOnClickListener {
            finish()
        }

        allowButton.setOnClickListener {
            clearAppCache()
            val intent = Intent(this, AddphotoActivity::class.java)
            startActivity(intent)
        }

        val imageUriString = intent.getStringExtra("imageUri")
        Log.d("ShowcelebrityActivity", "Image URI: $imageUriString")
        if (imageUriString != null) {
            val imageUri = Uri.parse(imageUriString)
            imageView.setImageURI(imageUri)
            uploadImageToServer(imageUri)
        } else {
            Toast.makeText(this, "Image URI is invalid", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImageToServer(imageUri: Uri) {
        val file = getFileFromUri(imageUri)
        if (file == null || !file.exists()) {
            Toast.makeText(this, "File not found or unable to create file", Toast.LENGTH_SHORT).show()
            return
        }

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", file.name, file.asRequestBody("image/jpeg".toMediaType()))
            .build()

        val url = resources.getString(R.string.root_url) + "/predict"
        if (url.isEmpty()) {
            Toast.makeText(this, "API URL is missing", Toast.LENGTH_SHORT).show()
            return
        }

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val client = OkHttpClient()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@ShowcelebrityActivity, "Failed to upload image: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    runOnUiThread {
                        Toast.makeText(this@ShowcelebrityActivity, "Image uploaded successfully", Toast.LENGTH_SHORT).show()

                        if (responseBody != null) {
                            try {
                                val jsonObject = JSONObject(responseBody)
                                val predictedClass = jsonObject.getString("predicted_class")
                                val confidenceScore = jsonObject.getDouble("confidence_score")
                                val celebrity_name = findViewById<TextView>(R.id.celebrity_name)
                                val similarity_percentage = findViewById<TextView>(R.id.similarity_percentage)

                                celebrity_name.text = predictedClass
                                similarity_percentage.text = "$confidenceScore %"

                                // ส่งข้อมูลไปยัง API /similarity
                                sendSimilarityDataToServer(predictedClass, confidenceScore)

                            } catch (e: JSONException) {
                                Toast.makeText(this@ShowcelebrityActivity, "Failed to parse response: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this@ShowcelebrityActivity, "Empty response", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    val errorBody = response.body?.string() ?: "Unknown error"
                    runOnUiThread {
                        Toast.makeText(this@ShowcelebrityActivity, "Error: $errorBody", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun sendSimilarityDataToServer(predictedClass: String, confidenceScore: Double) {
        getThaiCelebrityIdFromServer(predictedClass) { thaiCelebrityId ->
            if (thaiCelebrityId != null) {
                val client = OkHttpClient()

                // สร้างวันที่ปัจจุบันในรูปแบบ "yyyy-MM-dd"
                val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                // สร้าง JSON object สำหรับส่งข้อมูล
                val jsonObject = JSONObject().apply {
                    put("similarity_Date", currentDate)  // ใช้วันที่ปัจจุบัน
                    put("similarityDetail_Percent", confidenceScore)
                    put("ThaiCelebrities_ID", thaiCelebrityId)
                }

                val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url(resources.getString(R.string.root_url) + "/similarity")
                    .post(requestBody)
                    .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        runOnUiThread {
                            Toast.makeText(this@ShowcelebrityActivity, "Failed to send data: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onResponse(call: Call, response: Response) {
                        if (response.isSuccessful) {
                            runOnUiThread {
                                Toast.makeText(this@ShowcelebrityActivity, "Data sent successfully", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            val errorBody = response.body?.string() ?: "Unknown error"
                            runOnUiThread {
                                Toast.makeText(this@ShowcelebrityActivity, "Error: $errorBody", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                })
            } else {
                runOnUiThread {
                    Toast.makeText(this@ShowcelebrityActivity, "Celebrity not found", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getThaiCelebrityIdFromServer(celebrityName: String, callback: (Int?) -> Unit) {
        val client = OkHttpClient()

        val url = resources.getString(R.string.root_url) + "/get-celebrity-id?name=" + celebrityName
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@ShowcelebrityActivity, "Failed to retrieve celebrity data: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        try {
                            val jsonObject = JSONObject(responseBody)
                            val celebrityId = jsonObject.getInt("celebrityId")
                            callback(celebrityId)
                        } catch (e: JSONException) {
                            runOnUiThread {
                                Toast.makeText(this@ShowcelebrityActivity, "Failed to parse celebrity data: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                            callback(null)
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@ShowcelebrityActivity, "Empty response", Toast.LENGTH_SHORT).show()
                        }
                        callback(null)
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@ShowcelebrityActivity, "Error: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                    callback(null)
                }
            }
        })
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
                Log.e("ShowcelebrityActivity", "InputStream is null")
            }
        } catch (e: Exception) {
            Log.e("ShowcelebrityActivity", "Failed to get file from URI: ${e.message}", e)
        }
        return null
    }

    private fun clearAppCache() {
        try {
            val cacheDir = cacheDir
            if (cacheDir.isDirectory) {
                val children = cacheDir.list()
                if (children != null) {
                    for (child in children) {
                        val success = File(cacheDir, child).delete()
                        if (!success) {
                            Log.d("ShowcelebrityActivity", "Failed to delete cache file: $child")
                        }
                    }
                }
            }
            Log.d("ShowcelebrityActivity", "Cache cleared successfully")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
