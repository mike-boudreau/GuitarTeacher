package com.boudreau.guitarteacher.models

import android.graphics.Canvas
import android.graphics.Paint

class WelcomeText(private val textPaint: Paint) {

    // Function to draw the welcome text on the welcome screen
    fun drawText(canvas: Canvas) {
        // Increase the size of the welcome text
        textPaint.textSize += 1f

        // Draw the text with the canvas object
        canvas.drawText("Welcome!", 425F, 600F, textPaint)
    }
}