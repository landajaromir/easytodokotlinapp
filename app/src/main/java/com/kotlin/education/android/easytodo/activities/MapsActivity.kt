package com.kotlin.education.android.easytodo.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.kotlin.education.android.easytodo.R
import com.kotlin.education.android.easytodo.constants.IntentConstants
import kotlinx.android.synthetic.main.activity_main.*

/**
 * The activity for showing a map.
 * Interesting link. The Google Maps Utility.
 * https://github.com/googlemaps/android-maps-utils
 */
class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerDragListener {

    // all the static things for the activity
    companion object {

        /**
         * The method for creating intent to run the activity. The activity should be only run
         * using this method. This way no one doesn't need to know what to put to intent
         */
        fun createIntent(context: Context, latitude: Double?, longitude: Double?): Intent {
            val intent = Intent(context, MapsActivity::class.java)
            intent.putExtra(IntentConstants.LATITUDE, latitude)
            intent.putExtra(IntentConstants.LONGITUDE, longitude)
            return intent
        }

        /**
         * The default locations. Now set to Spilberk castle in Brno
         */
        private const val DEFAULT_LATITUDE = 49.195570
        private const val DEFAULT_LONGITUDE = 16.599395
    }

    private var latitude: Double? = null
    private var longitude: Double? = null
    private var locationChanged: Boolean = false
    private lateinit var mMap: GoogleMap


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        // set the locations coming from Intent. Used for location editing.
        if (intent.hasExtra(IntentConstants.LONGITUDE) && intent.hasExtra(IntentConstants.LONGITUDE)){
            latitude = intent.getDoubleExtra(IntentConstants.LATITUDE, Companion.DEFAULT_LATITUDE)
            longitude = intent.getDoubleExtra(IntentConstants.LONGITUDE, Companion.DEFAULT_LONGITUDE)
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
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

        val position: LatLng
        if (latitude != null && longitude != null){
            position = LatLng(latitude!!, longitude!!)
        } else {
            position = LatLng(Companion.DEFAULT_LATITUDE, Companion.DEFAULT_LATITUDE)
        }

        // the MarkerOptions is used to style the marker.
        val markerOptions: MarkerOptions = MarkerOptions().position(position).draggable(true)

        // Add a marker. The addMarker method returns the created marker for later processing.
        // We can store it to use later.
        var marker: Marker = mMap.addMarker(markerOptions)
        // move the camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(position))
        // set the listener for marker dragging.
        mMap.setOnMarkerDragListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_maps, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save -> {
                finishMapActivity()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Finishes the Map Activity by sending back the latitude and longitude.
     */
    private fun finishMapActivity(){
        val intent = Intent()
        if (locationChanged) {
            intent.putExtra(IntentConstants.LATITUDE, latitude)
            intent.putExtra(IntentConstants.LONGITUDE, longitude)
        }
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    /**
     * The implementation for the GoogleMap.OnMarkerDragListener.
     */
    override fun onMarkerDragEnd(p0: Marker?) {
        locationChanged = true
        latitude = p0?.position?.latitude
        longitude = p0?.position?.longitude
    }

    override fun onMarkerDragStart(p0: Marker?) {

    }

    override fun onMarkerDrag(p0: Marker?) {

    }

}
