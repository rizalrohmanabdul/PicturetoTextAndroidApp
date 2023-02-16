package com.rizal.picture_to_textapp.utils

import com.google.firebase.database.FirebaseDatabase


class FirebaseUtils {

    companion object {
        fun saveData(text: String, distance: String, duration: String) {
            val database = FirebaseDatabase.getInstance()
            val ref = database.getReference("data")

            val data = mapOf(
                "text" to text,
                "distance" to distance,
                "duration" to duration
            )

            ref.push().setValue(data)
        }
    }
}
