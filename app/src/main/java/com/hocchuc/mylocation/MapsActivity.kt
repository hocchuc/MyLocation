package com.hocchuc.mylocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolygonOptions
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener {

    companion object {
        const val REQUEST_LOCATION_PERMISSION_CODE = 123
        const val MAX_LOCATION_SIZE = 100
        const val EARTH_RADIUS = 637810.00

    }

    private var mMap: GoogleMap? = null

    private var tvLatLng: TextView? = null


    private var isMapReady = false
    private var myLocationList = arrayListOf<LatLng>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        tvLatLng = findViewById(R.id.tvLatLng)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                REQUEST_LOCATION_PERMISSION_CODE
            )
            return
        } else {
            staringRequestUserLocation()
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
        isMapReady = true

        mMap?.setMaxZoomPreference(20f)
        if (myLocationList.size > 0) {
            mMap?.moveCamera(CameraUpdateFactory.newLatLng(myLocationList[0]))
            mMap?.animateCamera(CameraUpdateFactory.zoomBy(8f))
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (grantResults.isNotEmpty() && grantResults[0] ==
            PackageManager.PERMISSION_GRANTED
        ) {
            if ((ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED)
            ) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                staringRequestUserLocation()
            }
        } else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @SuppressLint("MissingPermission")
    private fun staringRequestUserLocation() {

        mMap?.isMyLocationEnabled = true

        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val cr = Criteria()
        val provider = locationManager.getBestProvider(cr, true)
        provider?.run {
            locationManager.requestLocationUpdates(provider, 5000, 0f, this@MapsActivity)
        }
    }

    private fun processLocation(location: Location) {
        val currentLocation = LatLng(location.latitude, location.longitude)
        addCurrentLocation(currentLocation)
        if (isMapReady) {

            val accuracy = min(location.accuracy.toDouble(), 100.0)

            val bearingAngle = location.bearing.toDouble()
            val polygon1 = (PolygonOptions()
                .clickable(false)
                .zIndex(1f)
                .add(
                    LatLng(
                        location.latitude + (20 / EARTH_RADIUS * sin(bearingAngle)),
                        location.longitude + (20 / EARTH_RADIUS * cos(bearingAngle))
                    ),
                    LatLng(
                        location.latitude+ (20 / EARTH_RADIUS * sin(bearingAngle - 90)),
                        location.longitude+ (20 / EARTH_RADIUS * cos(bearingAngle - 90))
                    ),
                    LatLng(
                        location.latitude + (20 / EARTH_RADIUS * sin(bearingAngle + 90)),
                        location.longitude + (20 / EARTH_RADIUS * cos(bearingAngle + 90))
                    ),
                    LatLng(
                        location.latitude + (20 / EARTH_RADIUS * sin(bearingAngle)),
                        location.longitude + (20 / EARTH_RADIUS * cos(bearingAngle))
                    ),
                ))
                .fillColor(Color.YELLOW)
            mMap?.addPolygon(polygon1)

            val circleOptions = CircleOptions()
                .center(LatLng(location.latitude, location.longitude))
            circleOptions.radius(1.0).zIndex(2f)
            circleOptions.strokeColor(Color.BLACK)
            circleOptions.fillColor(Color.BLUE)
            mMap?.addCircle(circleOptions)

            circleOptions.radius(accuracy).zIndex(3f) // In meters
            circleOptions.strokeColor(Color.BLACK)
            circleOptions.fillColor(Color.TRANSPARENT)
            mMap?.addCircle(circleOptions)


            mMap?.moveCamera(CameraUpdateFactory.newLatLng(LatLng(
                location.latitude ,
                location.longitude
            )))
            mMap?.animateCamera(CameraUpdateFactory.zoomBy(8f))


        }
        tvLatLng?.text = getString(
            R.string.lat_lng_value, location.latitude,
            location.longitude
        )
    }

    private fun addCurrentLocation(currentLocation: LatLng) {
        if (myLocationList.size > MAX_LOCATION_SIZE) {
            myLocationList.removeAt(0)
        }
        myLocationList.add(currentLocation)
    }

    override fun onLocationChanged(location: Location) {
        location.run {
            processLocation(location)
        }
    }
}