package com.example.whoareyou

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.whoareyou.databinding.ActivityAddphotoBinding

class AddphotoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddphotoBinding
    private val PICK_IMAGE = 1
    private val CAMERA_REQUEST = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inflate the layout using ViewBinding
        binding = ActivityAddphotoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply system window insets to add padding for status and navigation bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.addphoto)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Check permissions for camera and external storage
        checkPermissions()

        // Set click listener for the ImageView (to upload image)
        binding.imageView.setOnClickListener {
            showImageChooser()
        }

        // Set click listener for the confirm button
        binding.confirmButton.setOnClickListener {
            Toast.makeText(this, "Image confirmed!", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to show options for choosing image (camera or gallery)
    private fun showImageChooser() {
        val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Select Option")
        builder.setItems(options) { dialog, item ->
            when {
                options[item] == "Take Photo" -> {
                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(cameraIntent, CAMERA_REQUEST)
                }
                options[item] == "Choose from Gallery" -> {
                    val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    galleryIntent.type = "image/*"
                    startActivityForResult(galleryIntent, PICK_IMAGE)
                }
                options[item] == "Cancel" -> dialog.dismiss()
            }
        }
        builder.show()

    }

    // Function to check if camera and storage permissions are granted
    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE), 101)
        }
    }

    fun onNext(selectedImageUri: Uri) {
        // Make the confirm button visible
        binding.confirmButton.visibility = View.VISIBLE

        // Set click listener using the binding reference
        binding.confirmButton.setOnClickListener {
            // Create an Intent to start the next activity
//            val intent = Intent(this, ConfirmUpphotoActivity::class.java)
//            // Pass the selected image URI to the next activity
//            intent.putExtra("imageUri", selectedImageUri.toString())
//            startActivity(intent)
        }
    }

    // Handle the result of the image picker or camera
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE -> {
                    val selectedImage: Uri? = data?.data
                    selectedImage?.let {
                        // Set the image to ImageView
                        binding.imageView.setImageURI(it)
                        binding.textViewFile.text = it.lastPathSegment
                        binding.textViewFile.text = "รายละเอียดชื่อไฟล์: " + it.lastPathSegment
                        binding.textViewFile.visibility = View.VISIBLE
                        // Hide the instruction TextView
                        binding.textView.visibility = View.GONE
                        onNext(it)
                    }
                }
                CAMERA_REQUEST -> {
                    val photo: Bitmap = data?.extras?.get("data") as Bitmap
                    // Set the captured photo to ImageView
                    binding.imageView.setImageBitmap(photo)
                    // Set a general message as there is no file name
                    binding.textViewFile.text = "Captured Image"
                    binding.textViewFile.visibility = View.VISIBLE
                    // Hide the instruction TextView
                    binding.textView.visibility = View.GONE
                    // If you want to save the captured image as a file and pass the URI, you need to first save it
                    // Pass a placeholder URI or handle this case appropriately
                    val placeholderUri = Uri.parse("captured_image_placeholder")
                    onNext(placeholderUri)
                }
            }
        }
    }




}
