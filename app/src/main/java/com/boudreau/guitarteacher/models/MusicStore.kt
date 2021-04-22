package com.boudreau.guitarteacher.models

import java.sql.Time

// Class to represent a music store
class MusicStore(val musicStoreId: Int,
                 val musicStoreName: String,
                 val latitude: Double,
                 val longitude: Double,
                 val open: Time?,
                 val close: Time?) {

    override fun toString(): String {
        return "$musicStoreName [ID: $musicStoreId, Lat: $latitude, " +
                "Lng: $longitude, Hours: $open - $close"
    }
}
