package com.boudreau.guitarteacher.models

import android.graphics.Bitmap

// Class to represent chords
class Chord(val chordId: Int,
            val chordName: String,
            val chordType: String,
            val diagram: Bitmap) {

    override fun equals(other: Any?): Boolean {
        return if (other is Chord) {
            other.chordId == this.chordId
        }
        else {
            false
        }
    }

    override fun toString(): String {
        return "$chordName $chordType [ID: $chordId]"
    }

    override fun hashCode(): Int {
        var result = chordId
        result = 31 * result + chordName.hashCode()
        result = 31 * result + chordType.hashCode()
        result = 31 * result + diagram.hashCode()
        return result
    }
}