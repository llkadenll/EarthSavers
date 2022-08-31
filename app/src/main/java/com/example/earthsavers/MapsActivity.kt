package com.example.earthsavers

import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.earthsavers.databinding.ActivityMapsBinding
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    val db = DBhelper(this)

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener

    private lateinit var user_position_marker: Marker

    private lateinit var btn_add: Button
    private lateinit var btn_delete: Button

    private lateinit var current_marker: Marker
    private var current_marker_set: Boolean = false

    private lateinit var selected_marker: Marker
    private var selected_marker_set: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        btn_add = findViewById(R.id.btn_add)
        btn_delete = findViewById(R.id.btn_delete)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        btn_add.setOnClickListener() {
            if (current_marker_set) {
                db.addPlace(current_marker.getPosition())
                showPlaces()
                Toast.makeText(applicationContext, "Place added successfully", Toast.LENGTH_LONG).show()
            }
        }
        btn_delete.setOnClickListener() {
            if (selected_marker_set) {
                db.deletePlace(selected_marker.getPosition())
                showPlaces()
                Toast.makeText(applicationContext, "Place deleted successfully", Toast.LENGTH_LONG).show()
            }
        }
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
        mMap = googleMap
        mMap.setOnMapClickListener(listener)
        mMap.setOnMarkerClickListener(markerListener)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val current_location = LatLng(location.latitude, location.longitude)
                user_position_marker = mMap.addMarker(MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(240F)).position(current_location).title("You are here"))!!
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current_location, 15f))
                val geocoder = Geocoder(this@MapsActivity, Locale.getDefault())
                try {
                    val address_list = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    if (address_list.size > 0) {
                        println(address_list.get(0).toString())
                    }
                } catch (e:Exception) {
                    e.printStackTrace()
                }
            }

        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1f, locationListener)
            val lastknownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (lastknownLocation != null) {
                val last_knowLatlng = LatLng(lastknownLocation.latitude, lastknownLocation.longitude)
                user_position_marker = mMap.addMarker(MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(240F)).position(last_knowLatlng).title("You are here"))!!
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(last_knowLatlng, 15f))
            }
        }

        showPlaces()

//        // Add a marker in Sydney and move the camera
//        val sydney = LatLng(-34.0, 151.0)
//        val marker2 = LatLng(-35.0,152.0)
//        mMap.addMarker(MarkerOptions().position(sydney).title("Sydney"))
//        mMap.addMarker(MarkerOptions().position(marker2).title("Marker2"))
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 15f))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1) {
            if (grantResults.size < 1) {
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1f, locationListener)
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    val listener = object : GoogleMap.OnMapClickListener {
        override fun onMapClick(p0: LatLng) {

            if (current_marker_set) {
                current_marker_set = false
                current_marker.remove()
            }

//            mMap.clear()
            val geocoder = Geocoder(this@MapsActivity, Locale.getDefault())
            if (p0 != null) {
                var address = ""
                try {
                    val addressList = geocoder.getFromLocation(p0.latitude, p0.longitude, 1)
                    if (addressList.size > 0) {
                        if (addressList.get(0).thoroughfare != null) {
                            address += addressList.get(0).thoroughfare
                            if (addressList.get(0).subThoroughfare != null) {
                                address += addressList.get(0).subThoroughfare
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                current_marker = mMap.addMarker(MarkerOptions().position(p0).title(address))!!
                btn_delete.visibility = View.INVISIBLE
                btn_add.visibility = View.VISIBLE
                current_marker_set = true
            }
        }
    }

    val markerListener = object : GoogleMap.OnMarkerClickListener {
        override fun onMarkerClick(p0: Marker): Boolean {
            if (p0 != user_position_marker && (!current_marker_set || (p0 != current_marker))) {
                btn_add.visibility = View.INVISIBLE
                btn_delete.visibility = View.VISIBLE
                selected_marker_set = true
                selected_marker = p0
            }
            return false
        }
    }

    fun showPlaces() {
        mMap.clear()

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val current_location = LatLng(location.latitude, location.longitude)
                user_position_marker = mMap.addMarker(MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(240F)).position(current_location).title("You are here"))!!
                val geocoder = Geocoder(this@MapsActivity, Locale.getDefault())
                try {
                    val address_list = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    if (address_list.size > 0) {
                        println(address_list.get(0).toString())
                    }
                } catch (e:Exception) {
                    e.printStackTrace()
                }
            }

        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1f, locationListener)
            val lastknownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (lastknownLocation != null) {
                val last_knowLatlng = LatLng(lastknownLocation.latitude, lastknownLocation.longitude)
                user_position_marker = mMap.addMarker(MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(240F)).position(last_knowLatlng).title("You are here"))!!
            }
        }

        var places = db.allPlaces
        println(places)
        for (place in places) {
            mMap.addMarker(MarkerOptions().position(place.position).title("ID: " + place.id.toString() + ", Created at: " + java.sql.Timestamp(place.created_at.toLong())))!!
        }
    }
}