package com.example.mechanicfinder

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.location.LocationManagerCompat.requestLocationUpdates
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.location.*
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.location.LocationListener
import com.example.mechanicfinder.databinding.ActivityMechanicMapBinding
import com.google.android.gms.maps.model.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MechanicMapActivity : AppCompatActivity(), OnMapReadyCallback,LocationListener{

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMechanicMapBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var refference: DatabaseReference
    private var currentMarker: Marker? = null
    private lateinit var locationManager: LocationManager
    private val locationPermissionCode = 2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMechanicMapBinding.inflate(layoutInflater)
        setContentView(binding.root)
        database = FirebaseDatabase.getInstance()
        refference = database.reference
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
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
        mMap.mapType=GoogleMap.MAP_TYPE_TERRAIN
        getLocation()
        mMap.setOnMarkerClickListener(object : GoogleMap.OnMarkerClickListener {
            override fun onMarkerClick(marker: Marker): Boolean {

                val dialog = MaterialAlertDialogBuilder(applicationContext)
                dialog.setTitle("LogOut")
                dialog.setMessage("Do you really want to Book this Mechanic?")
                dialog.setIcon(R.drawable.ic_baseline_login_24)

                dialog.setPositiveButton("Yes") { _, _ ->
                    refference.child("availableMechanics").addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val user = dataSnapshot.getValue(User::class.java)
                            if (user == null) {
                                Log.e(TAG, "User data is null")
                                return
                            }
                            refference.child("BookedUsers").child(marker.title.toString()).setValue(FirebaseAuth.getInstance().currentUser!!.uid.toString())
                        }

                        override fun onCancelled(error: DatabaseError) {
                            getLocation()
                            Log.e(TAG, "Failed to read user", error.toException())
                        }
                    })
                    Toast.makeText(applicationContext, "Booked Successfully", Toast.LENGTH_SHORT).show()
                }

                dialog.setNeutralButton("Cancel") { _, _ ->
                    Toast.makeText(applicationContext, "Cancelled", Toast.LENGTH_SHORT).show()

                }

                dialog.create()
                dialog.setCancelable(false)
                dialog.show()
                return false
            }
        })
        refference.child("Bookings").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(User::class.java)
                if (user == null) {
                    Log.e(TAG, "User data is null")
                    Toast.makeText(this@MechanicMapActivity,"No available bookings at the moment",Toast.LENGTH_LONG).show()
                    return
                }
                val mechanic = LatLng(user.latitude.toDouble(), user.longitude.toDouble())
                mMap.addMarker(MarkerOptions().position(mechanic).title(user.name).icon(BitmapDescriptorFactory.fromResource(R.drawable.logo)))
                mMap.animateCamera(CameraUpdateFactory.newLatLng(mechanic))
            }

            override fun onCancelled(error: DatabaseError) {
                getLocation()
                Log.e(TAG, "Failed to read user", error.toException())
            }
        })

    }

    private fun getLocation() {
        var fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_DENIED || ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_DENIED
            ) {
                val permission = arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
                requestPermissions(permission, locationPermissionCode)
            } else {
                //permission already granted
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        val latitude = location.latitude
                        val longitude = location.longitude
                        val latlong=LatLng(latitude,longitude)
                        mMap.addMarker(MarkerOptions().position(latlong).title("Your current Position"))
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlong,20F))
                        // Got last known location. In some rare situations this can be null.
                        refference.child("mechanic").child(FirebaseAuth.getInstance().currentUser!!.uid).setValue(latlong).addOnCompleteListener {
                                Toast.makeText(this@MechanicMapActivity,"location updated successfully",Toast.LENGTH_LONG).show()
                        }.addOnFailureListener {
                            Toast.makeText(this@MechanicMapActivity,"Location update error",Toast.LENGTH_LONG).show()
                        }



                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed on getting current location",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
        } else {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    val latitude = location!!.latitude
                    val longitude = location!!.longitude
                    val latlong=LatLng(latitude,longitude)
                    mMap.addMarker(MarkerOptions().position(latlong).title("Your current Position").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_car)))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlong,20F))
                    // Got last known location. In some rare situations this can be null.
                    refference.child("carOwner").child(FirebaseAuth.getInstance().currentUser!!.uid).setValue(latlong).addOnCompleteListener {
                        Toast.makeText(this@MechanicMapActivity,"location updated successfully",Toast.LENGTH_LONG).show()

                    }.addOnFailureListener {
                        Toast.makeText(this@MechanicMapActivity,"Location update error",Toast.LENGTH_LONG).show()
                    }



                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed on getting current location",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
                }

    override fun onLocationChanged(location: Location) {
        val latitude = location.latitude
        val longitude = location.longitude
        mMap.addMarker(
            MarkerOptions().position(LatLng(latitude, longitude)).title("Your current Position").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_car))
        )
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude),20F))
    }

    override fun onResume() {
        getLocation()
        super.onResume()
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }

        }
    }
}


