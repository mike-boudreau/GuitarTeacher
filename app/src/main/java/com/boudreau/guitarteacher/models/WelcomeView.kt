package com.boudreau.guitarteacher.models

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat.startActivity
import com.boudreau.guitarteacher.LOGGED_IN_USER_ID
import com.boudreau.guitarteacher.MainActivity
import com.boudreau.guitarteacher.R

class WelcomeView(context: Context, private val loggedInUserId: Int, private val savedInstanceState: Bundle?) : View(context) {

    // Class variables
    private val welcomeBubble: WelcomeBubble
    private val welcomeText: WelcomeText
    private val bubblePaint = Paint()
    private val textPaint = Paint()

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw the welcome bubble and welcome text
        welcomeBubble.drawBubble(canvas)
        welcomeText.drawText(canvas)

        // Sleep the thread for 10 milliseconds
        try {
            Thread.sleep(10)
        } catch (e: InterruptedException) {
            // If there's a problem log the error
            Log.d("ThreadSleepError", e.message.toString())
        }

        // Check the radius of the welcome bubble
        if (welcomeBubble.radius < 700F) {
            // Redraw until the radius is 700
            invalidate()
        }
        else {
            // Open the main activity
            val mainIntent = Intent(this.context, MainActivity::class.java)
            mainIntent.putExtra(LOGGED_IN_USER_ID, loggedInUserId)
            startActivity(this.context, mainIntent, savedInstanceState)
        }
    }

    init {
        // Set the color for the welcome bubble and instantiate the bubble itself
        bubblePaint.color = resources.getColor(R.color.orange_web)
        welcomeBubble = WelcomeBubble(bubblePaint)

        // Set the color/size for the welcome text and instantiate it as well
        textPaint.color = resources.getColor(R.color.oxford_blue)
        textPaint.textSize = 10f
        welcomeText = WelcomeText(textPaint)
    }
}