package com.rizal.picture_to_textapp

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.LatLng
import com.rizal.picture_to_textapp.utils.FirebaseUtils
import com.rizal.picture_to_textapp.utils.ImageUtils
import com.rizal.picture_to_textapp.utils.LocationUtils

class MainActivity : AppCompatActivity() {

    companion object {
        const val  REQUEST_IMAGE_CAPTURE = 1
        const val  REQUEST_LOCATION_PERMISSIONS = 2
    }
    lateinit var imageView: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btn = findViewById<Button>(R.id.btn_capture)
        imageView = findViewById(R.id.img_preview)

        if (!arePermissionsGranted()){
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ),
                REQUEST_LOCATION_PERMISSIONS
            )
        }

        btn.setOnClickListener {
            ImageUtils.captureImage(this)
        }

    }

    private fun arePermissionsGranted(): Boolean {
        return (ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Get the captured image as a Bitmap
            val imageBitmap = data?.extras?.get("data") as Bitmap

            // Set the ImageView to display the captured image
            imageView.setImageBitmap(imageBitmap)

            onPictureTaken(imageBitmap)
        }
    }


    private fun onPictureTaken(bitmap: Bitmap) {
        // Convert the bitmap to text
        val text = ImageUtils.convertImageToText(bitmap)

        // Get the current location and calculate the distance and estimated time
       val locationUtils = LocationUtils(this)
        locationUtils.getCurrentLocation { location ->
            if (location != null) {
                val endLocation = LatLng(-6.195959, 106.820743) // Plaza Indonesia Jakarta
                locationUtils.calculateDistanceAndTime(location, endLocation) { distance, duration ->
                    // Save the data to Firebase
                    FirebaseUtils.saveData(text, distance.orEmpty(), duration.orEmpty())

                    // Navigate to the DisplayActivity
                    val intent = Intent(this, DisplayActivity::class.java)
                    intent.putExtra("text", text)
                    intent.putExtra("distance", distance)
                    intent.putExtra("duration", duration)
                    startActivity(intent)
                }
            } else {
                Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show()
            }
        }
    }
}