package com.rizal.picture_to_textapp

import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextRecognizer
import com.google.firebase.database.FirebaseDatabase
import com.google.maps.DirectionsApiRequest
import com.google.maps.GeoApiContext
import com.google.maps.PendingResult
import com.google.maps.model.DirectionsResult
import com.google.maps.model.TravelMode
import com.rizal.picture_to_textapp.utils.ImageUtils
import com.rizal.picture_to_textapp.utils.LocationUtils

class MainActivity : AppCompatActivity() {

    companion object {
        const val  REQUEST_IMAGE_CAPTURE = 1
        const val  REQUEST_LOCATION_PERMISSIONS = 2
        const val TESS_DATA = "/tessdata"
        private val TAG = MainActivity::class.java.simpleName
        private val DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/Tess"
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
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
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

//            prepareTessData()

            onPictureTaken(imageBitmap)
        }
    }


    private fun onPictureTaken(bitmap: Bitmap) {
        val progressDialog = ProgressDialog.show(this, "", "Processing picture...", true)
        val textRecognizer = TextRecognizer.Builder(this).build()
        val frameImage = Frame.Builder().setBitmap(bitmap).build()
        val textBlockSparseArray = textRecognizer.detect(frameImage)
        var stringImageText = ""
        for (i in 0 until textBlockSparseArray.size()) {
            val textBlock = textBlockSparseArray[textBlockSparseArray.keyAt(i)]
            stringImageText = stringImageText + " " + textBlock.value
        }

        // Get the current location and calculate the distance and estimated time
       val locationUtils = LocationUtils(this)
        locationUtils.getCurrentLocation { location ->
            if (location != null) {
                val endLocation = LatLng(-6.195959, 106.820743) // Plaza Indonesia Jakarta
                estimateDistanceAndTime(location, endLocation, stringImageText, progressDialog)
            } else {
                Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun estimateDistanceAndTime(startLocation: Location, endLocation: LatLng, text: String, pd: ProgressDialog) {
        // Create a Directions API request to get the distance and estimated time

        val context = GeoApiContext.Builder()
            .apiKey("AIzaSyCa7Id2Gr9BOlDyya4eJsGxRYxrcoLoSEs")
            .build()
        val request = DirectionsApiRequest(context)
            .origin(com.google.maps.model.LatLng(startLocation.latitude, startLocation.longitude))
            .destination(com.google.maps.model.LatLng(endLocation.latitude, endLocation.longitude))
            .mode(TravelMode.DRIVING)

        // Execute the request asynchronously using the Google Maps API client
        request.setCallback(object : PendingResult.Callback<DirectionsResult> {
            override fun onResult(result: DirectionsResult) {
                // Extract the distance and estimated time from the response
                val distance = result.routes[0].legs[0].distance.humanReadable
                val time = result.routes[0].legs[0].duration.humanReadable

                // Upload the text, distance, and estimated time to Firebase
                val database = FirebaseDatabase.getInstance()
                val locationRef = database.getReference("locations")
                val data = mapOf(
                    "text" to text,
                    "distance" to distance,
                    "duration" to time
                )

                locationRef.setValue(data)
                pd.dismiss()
                // Launch the ResultActivity to display the text, distance, and estimated time
                val intent = Intent(this@MainActivity, DisplayActivity::class.java)
                intent.putExtra("text", text)
                intent.putExtra("distance", distance.toString())
                intent.putExtra("duration", time.toString())
                startActivity(intent)

            }

            override fun onFailure(e: Throwable) {
                pd.dismiss()
                Log.e(TAG, "Error getting directions: " + e.message)
                Toast.makeText(this@MainActivity, "Error getting directions", Toast.LENGTH_SHORT).show()
            }
        })
    }
}