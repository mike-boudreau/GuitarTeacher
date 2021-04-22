package com.boudreau.guitarteacher

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.boudreau.guitarteacher.database.DbHelper
import com.boudreau.guitarteacher.managers.GtUserManager
import com.boudreau.guitarteacher.managers.MusicStoreManager
import com.boudreau.guitarteacher.models.User
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

class FindAShopActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var musicStoreMap: GoogleMap
    private lateinit var dbHelper: DbHelper
    private lateinit var supportMapFragment: SupportMapFragment
    private lateinit var loggedInUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.find_a_shop_activity)
        setSupportActionBar(findViewById(R.id.toolbar))

        // Initialize variables
        initializeVariables()
        
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        supportMapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        supportMapFragment.getMapAsync { map ->
            onMapReady(map)
        }
    }

    // Function to initialize class variables
    private fun initializeVariables() {
        // Initialize the database helper
        dbHelper = DbHelper(this)

        // Set the logged in user
        loggedInUser =
                GtUserManager.getUserById(dbHelper, intent.extras!!.getInt(LOGGED_IN_USER_ID))!!
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        musicStoreMap = googleMap

        // Get a LatLng object for the center of Green Bay
        val centerOfGreenBay = LatLng(44.51355, -88.02530)

        // Set the zoom
        musicStoreMap.moveCamera(CameraUpdateFactory.zoomTo(12F))

        // Set the maps center point
        musicStoreMap.moveCamera(CameraUpdateFactory.newLatLng(centerOfGreenBay))

        // Enable zoom/tilt as well as gestures
        musicStoreMap.uiSettings.isZoomControlsEnabled = true
        musicStoreMap.uiSettings.isTiltGesturesEnabled = true
        musicStoreMap.uiSettings.setAllGesturesEnabled(true)

        // Add the music stores from the database to the map
        for (musicStore in MusicStoreManager.getAllMusicStores(dbHelper)) {
            // Get the lat/long
            val latLng = LatLng(musicStore.latitude, musicStore.longitude)

            // Set a marker for the store
            val musicStoreMarker = musicStoreMap.addMarker(MarkerOptions()
                    .position(latLng)
                    .title(musicStore.musicStoreName)
                    .snippet("Hours: ${musicStore.open} - ${musicStore.close}")
                    .alpha(.78F))

            // Give each marker a tag
            musicStoreMarker.tag = musicStore.musicStoreId
        }

        // Ask the user to show their location, and display it if permission granted
        displayUserLocation()
    }

    // Function to display the user's location if permission is granted, otherwise ask permission
    @SuppressLint("MissingPermission")
    @AfterPermissionGranted(LOCATION_PERMISSION_REQUEST_CODE)
    private fun displayUserLocation() {
        // Check if the user has previously given permission
        if (EasyPermissions.hasPermissions(
                        this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Permission is already granted, display the user's location
            musicStoreMap.isMyLocationEnabled = true
        }
        else {
            // Notify user of permission request
            Snackbar.make(
                    supportMapFragment.requireView(),
                    R.string.permission_snackbar,
                    Snackbar.LENGTH_INDEFINITE
            ).setAction(R.string.permission_ok) {
                // Request the user's permission
                EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.permission_reason),
                    LOCATION_PERMISSION_REQUEST_CODE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            }.show()
        }
    }

    // Handle permission request results
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        // Handle the results using easy permissions
        EasyPermissions.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults,
            this
        )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            R.id.action_back -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}