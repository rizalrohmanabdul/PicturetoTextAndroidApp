package com.rizal.picture_to_textapp.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.maps.DirectionsApiRequest
import com.google.maps.GeoApiContext
import com.google.maps.model.TravelMode
import com.rizal.picture_to_textapp.MainActivity.Companion.REQUEST_LOCATION_PERMISSIONS
import com.rizal.picture_to_textapp.Manifest

// LocationUtils.kt

class LocationUtils(private val context: Context) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    fun getCurrentLocation(callback: (Location?) -> Unit) {
        // Check for location permissions
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request location permissions
            ActivityCompat.requestPermissions(context as Activity, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSIONS)
            callback(null)
            return
        }

        // Get the last known location
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                callback(location)
            } else {
                // If the last known location is null, request a new location update
                val locationRequest = LocationRequest.create()
                locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult?) {
                        super.onLocationResult(locationResult)
                        if (locationResult != null && locationResult.locations.isNotEmpty()) {
                            val newLocation = locationResult.locations[0]
                            callback(newLocation)
                        } else {
                            callback(null)
                        }
                    }
                }, Looper.getMainLooper())
            }
        }
    }

    fun calculateDistanceAndTime(startLocation: Location, endLocation: LatLng, callback: (String?, String?) -> Unit) {
        // Create a Directions API request
        val context = GeoApiContext.Builder()
            .apiKey("AIzaSyCa7Id2Gr9BOlDyya4eJsGxRYxrcoLoSEs")
            .build()
        val request = DirectionsApiRequest(context)
            .origin(com.google.maps.model.LatLng(startLocation.latitude, startLocation.longitude))
            .destination(com.google.maps.model.LatLng(endLocation.latitude, endLocation.longitude))
            .mode(TravelMode.DRIVING).await()


        // Parse the response and get the distance and duration
        val route = request.routes[0]
        val leg = route.legs[0]
        val distance = leg.distance.humanReadable
        val duration = leg.duration.humanReadable

        // Return the distance and duration
        callback(distance, duration)
    }
}
