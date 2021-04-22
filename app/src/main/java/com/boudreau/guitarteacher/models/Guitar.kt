package com.boudreau.guitarteacher.models

import android.graphics.Bitmap

// Class to represent guitars
class Guitar(private val guitarId: Int,
             val brand: String,
             val model: String,
             val guitarType: String,
             val numberOfFrets: Int,
             val yearInvented: String,
             val price: String,
             val largeImage: Bitmap,
             val smallImage: Bitmap) {

    override fun toString(): String {
        return "$brand $model [ID: $guitarId, Type: $guitarType, Frets: $numberOfFrets, " +
                "Year Invented: $yearInvented, Price: $price]"
    }
}