package com.example.finproject


import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)


        // Initialize the Braille Keyboard button
        val brailleKeyboardButton: Button = findViewById(R.id.brailleKeyboardButton)
        brailleKeyboardButton.setOnClickListener {
            // Start the com.example.finproject.BrailleKeyboardActivity when the button is clicked
            val intent = Intent(this, BrailleKeyboardActivity::class.java)
            startActivity(intent)

        }
    }

}
