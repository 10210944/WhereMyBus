package com.example.wheremybus


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlin.random.Random
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.wheremybus.database.Favorite




class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val busMarkers = mutableListOf<Marker>()
    private val handler = Handler(Looper.getMainLooper())

    private val db by lazy { AppDatabase.getDatabase(this) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

 override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // --- Set map type from Settings ---
        val sharedPref = getSharedPreferences("user_settings", Context.MODE_PRIVATE)
        when (sharedPref.getString("map_type", "Normal")) {
            "Normal" -> mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            "Satellite" -> mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            "Terrain" -> mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
        }

        // --- Simulated bus list ---
        // Replace this with your real bus data if available
        val busList = listOf(
            Bus("bus1", LatLng(-33.9249, 18.4241), "Bus 1"),
            Bus("bus2", LatLng(-33.9255, 18.4230), "Bus 2"),
            Bus("bus3", LatLng(-33.9260, 18.4220), "Bus 3")
        )

        // --- Add markers to map ---
        for (bus in busList) {
            val marker = mMap.addMarker(
                MarkerOptions()
                    .position(bus.location)
                    .title(bus.name)
            )
            marker?.tag = bus.id  // store busId for favorites
        }

        // --- Add favorites toggle on marker tap ---
        mMap.setOnMarkerClickListener { marker ->
            val busId = marker.tag as String
            val busName = marker.title ?: "Bus"

            lifecycleScope.launch(Dispatchers.IO) {
                val existing = db.favoriteDao().getFavoriteByBusId(busId)
                if (existing == null) {
                    db.favoriteDao().insertFavorite(Favorite(busId = busId, busName = busName))
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "$busName added to favorites", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    db.favoriteDao().deleteFavorite(existing)
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "$busName removed from favorites", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            true
        }
    }


    private fun addBusMarkers() {
        TODO("Not yet implemented")
    }

    private fun enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
            return
        }
        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val userLatLng = LatLng(it.latitude, it.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
            }
        }
    }

    private fun addSimulatedBuses(center: LatLng) {
        for (i in 1..5) {
            val lat = center.latitude + Random.nextDouble(-0.02, 0.02)
            val lng = center.longitude + Random.nextDouble(-0.02, 0.02)
            val marker = mMap.addMarker(
                MarkerOptions()
                    .position(LatLng(lat, lng))
                    .title("Bus #$i")
            )
            marker?.let { busMarkers.add(it) }
        }
    }

    private fun animateBuses() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                busMarkers.forEach { marker ->
                    val newLat = marker.position.latitude + Random.nextDouble(-0.001, 0.001)
                    val newLng = marker.position.longitude + Random.nextDouble(-0.001, 0.001)
                    marker.position = LatLng(newLat, newLng)
                }
                handler.postDelayed(this, 2000) // Move every 2 seconds
            }
        }, 2000)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableUserLocation()
        }
    }
}
data class Bus(
    val id: String,
    val location: LatLng,
    val name: String
)
