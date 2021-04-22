package com.boudreau.guitarteacher.models

import android.graphics.*

class WelcomeBubble(private val bubblePaint: Paint) {
    // Class attributes for the welcome bubble
    var radius = 50F
    private val width = (1440/2).toFloat()
    private val height = (2980/2).toFloat()

    // Function to draw the welcome bubble on the welcome screen
    fun drawBubble(canvas: Canvas) {
        // Increase the size of the radius
        radius += 5F

        // Draw the circle using the canvas object
        canvas.drawCircle(width, height, radius, bubblePaint)
    }
}