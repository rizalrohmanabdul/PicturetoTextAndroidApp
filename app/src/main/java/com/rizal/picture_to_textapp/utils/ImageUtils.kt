package com.rizal.picture_to_textapp.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import com.rizal.picture_to_textapp.MainActivity.Companion.REQUEST_IMAGE_CAPTURE


object ImageUtils {

    fun captureImage(context: Context) {
        // Create an Intent to open the camera app
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        // Start the camera activity and wait for the result
        (context as Activity).startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

}
