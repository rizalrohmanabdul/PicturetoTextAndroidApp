package com.rizal.picture_to_textapp.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Environment
import android.provider.MediaStore
import com.googlecode.tesseract.android.TessBaseAPI
import com.rizal.picture_to_textapp.MainActivity.Companion.REQUEST_IMAGE_CAPTURE


object ImageUtils {

    fun captureImage(context: Context) {
        // Create an Intent to open the camera app
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        // Start the camera activity and wait for the result
        (context as Activity).startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

    fun convertImageToText(bitmap: Bitmap): String {
        // Create a Tesseract instance and set the data path and language
        val tess = TessBaseAPI()
        val DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/tessdata/"
        tess.init(DATA_PATH, "eng")

        // Set the image and get the recognized text
        tess.setImage(bitmap)
        val recognizedText = tess.utF8Text

        // End the Tesseract instance
        tess.end()

        // Return the recognized text
        return recognizedText
    }
}
