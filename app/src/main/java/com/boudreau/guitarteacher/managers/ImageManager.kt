package com.boudreau.guitarteacher.managers

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap
import java.io.ByteArrayOutputStream

object ImageManager {

    // Function to convert a drawable resource to a byte array
    fun drawableToByteArray(image: Drawable, compressFormat: Bitmap.CompressFormat) : ByteArray {
        // Convert the drawable passed in to a bitmap
        val bitmapDiagram = image.toBitmap()

        // Create a byte array output stream
        val byteOutputStream = ByteArrayOutputStream()

        // Compress into the stream
        bitmapDiagram.compress(compressFormat, 100, byteOutputStream)

        // Convert the stream to a byte array and return it
        return byteOutputStream.toByteArray()
    }
}