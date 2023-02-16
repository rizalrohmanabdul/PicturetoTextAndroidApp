package com.rizal.picture_to_textapp

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DisplayActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display)

        val textViewText = findViewById<TextView>(R.id.textViewText)
        val textViewDistance = findViewById<TextView>(R.id.textViewDistance)
        val textViewDuration = findViewById<TextView>(R.id.textViewDuration)

        val text = intent.getStringExtra("text")
        val distance = intent.getStringExtra("distance")
        val duration = intent.getStringExtra("duration")

        textViewText.text = text
        textViewDistance.text = distance
        textViewDuration.text = duration
    }
}