@file:Suppress("DEPRECATION")

package com.example.finproject

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Vibrator
import android.speech.tts.TextToSpeech
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlin.collections.HashMap

class BrailleKeyboardActivity : AppCompatActivity() {

    private lateinit var brailleOutput: TextView
    private lateinit var vibrator: Vibrator
    private lateinit var tts: TextToSpeech
    private val selectedDots = BooleanArray(6) { false }
    private var currentWord = StringBuilder()

    private val brailleMap: HashMap<String, Char> = hashMapOf(
        "100000" to 'A', "101000" to 'B', "110000" to 'C', "110100" to 'D', "100100" to 'E',
        "111000" to 'F', "111100" to 'G', "101100" to 'H', "011000" to 'I', "011100" to 'J',
        "100010" to 'K', "101010" to 'L', "110010" to 'M', "110110" to 'N', "100110" to 'O',
        "111010" to 'P', "111110" to 'Q', "101110" to 'R', "011010" to 'S', "011110" to 'T',
        "100011" to 'U', "101011" to 'V', "011101" to 'W', "110011" to 'X', "110111" to 'Y', "100111" to 'Z'
    )

    private val dotViews = mutableListOf<View>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_braille_keyboard)

        brailleOutput = findViewById(R.id.brailleOutput)
        vibrator = ContextCompat.getSystemService(this, Vibrator::class.java)!!

        tts = TextToSpeech(this) { status ->
            if (status != TextToSpeech.ERROR) {
                tts.language = java.util.Locale.ENGLISH
            }
        }

        // Add dots to the list
        dotViews.add(findViewById(R.id.dot1))
        dotViews.add(findViewById(R.id.dot2))
        dotViews.add(findViewById(R.id.dot3))
        dotViews.add(findViewById(R.id.dot4))
        dotViews.add(findViewById(R.id.dot5))
        dotViews.add(findViewById(R.id.dot6))

        setupTouchListeners()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchListeners() {
        val currentSwipe = mutableSetOf<Int>()

        val touchListener = View.OnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    dotViews.forEachIndexed { index, view ->
                        if (isInsideView(event, view) && !currentSwipe.contains(index)) {
                            vibrator.vibrate(100)
                            selectedDots[index] = true
                            currentSwipe.add(index) // Track swiped dots
                        }
                    }
                }
                MotionEvent.ACTION_UP -> {
                    // Convert swipe to Braille pattern and reset
                    processSwipe(currentSwipe)
                    currentSwipe.clear()
                }
            }
            true
        }

        dotViews.forEach { it.setOnTouchListener(touchListener) }
    }

    @SuppressLint("SetTextI18n")
    private fun processSwipe(swipe: Set<Int>) {
        if (swipe.isNotEmpty()) {
            val brailleString = selectedDots.joinToString(separator = "") { if (it) "1" else "0" }
            val letter = brailleMap[brailleString] ?: ' '

            // Append letter to the output
            currentWord.append(letter)
            brailleOutput.text = currentWord.toString()

            // Speak out the letter
            tts.speak(letter.toString(), TextToSpeech.QUEUE_FLUSH, null, null)

            // Scroll down after each new letter
            val scrollView: ScrollView = findViewById(R.id.scrollView)
            scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }

            // Reset selected dots after processing
            selectedDots.fill(false)
        }
    }

    private fun isInsideView(event: MotionEvent, view: View): Boolean {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val x = location[0]
        val y = location[1]
        return event.rawX >= x && event.rawX <= x + view.width && event.rawY >= y && event.rawY <= y + view.height
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            // Volume Up for deleting one letter
            KeyEvent.KEYCODE_VOLUME_UP -> {
                if (currentWord.isNotEmpty()) {
                    currentWord.deleteCharAt(currentWord.length - 1)
                    brailleOutput.text = currentWord.toString()
                    tts.speak("Deleted", TextToSpeech.QUEUE_FLUSH, null, null)
                }
                return true
            }
            // Volume Down for adding a space and reading out the word (if complete)
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                val text = currentWord.toString().trim()
                if (text.isNotEmpty() && text.last().isLetter()) {
                    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
                }

                brailleOutput.text = currentWord.toString()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }


    override fun onDestroy() {
        if (tts.isSpeaking) {
            tts.stop()
        }
        tts.shutdown()
        super.onDestroy()
    }
}
