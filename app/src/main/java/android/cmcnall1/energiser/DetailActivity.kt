package android.cmcnall1.energiser

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.TextView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import kotlinx.android.synthetic.main.detail_activity.*
import org.koin.android.ext.android.inject
import android.widget.AdapterView.OnItemSelectedListener
import com.google.android.gms.maps.model.*

/*
    The Detail Activity controls and manages the Detail view that displays to the user
    an in-depth look at their selected charge point including its location, the distance to it and
    things to do nearby. The Detail View is accessed by the user selecting a charge point on the map
    or from the list of charge points
 */

class DetailActivity : AppCompatActivity() , OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    DetailPresenter.View, AdapterView.OnItemSelectedListener{

    //Declaring the presenter of type DetailPresenter by injection
    private val presenter: DetailPresenter by inject()

    //Google Map object
    private lateinit var map: GoogleMap

    //Object that holds a latitude and longitude
    private lateinit var latLng: LatLng

    //Spinner that will allow the user to select the type of thing to do nearby the charge point
    private lateinit var placesSpinner: Spinner

    //String that holds a type of thing to do (e.g. Cafe, Restaurant, etc.)
    private lateinit var placeType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        //Change the app theme back to the correct theme after the splash screen has displayed
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)

        //Hide the Action Bar/Title Bar
        try {
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) {
        }

        //Display the detail activity layout
        setContentView(R.layout.detail_activity)

        //attach the detail activity view to the presenter
        presenter.attachView(this)

        //Retrieve the values that are attached to the intent object
        val chargePointTitle = intent.getStringExtra(CustomViewHolder.TITLE_KEY)
        val addressLine1 = intent.getStringExtra(CustomViewHolder.ADDRESS_LINE_1_KEY)
        val postcode = intent.getStringExtra(CustomViewHolder.POSTCODE_KEY)
        val country = intent.getStringExtra(CustomViewHolder.COUNTRY_KEY)
        val distanceFloat = intent.getFloatExtra(CustomViewHolder.DISTANCE_KEY, 0.0f)
        val latitude = intent.getFloatExtra(CustomViewHolder.LATITUDE_KEY, 0.0f)
        val longitude = intent.getFloatExtra(CustomViewHolder.LONGITUDE_KEY, 0.0f)

        //Create a LatLng object from the received latitude and longitude
        latLng = LatLng(latitude.toDouble(), longitude.toDouble())

        //Definition of how the distance will be displayed
        val distance = "%.2f".format(distanceFloat) + " Miles"

        //Assign the textviews that will display the data to the user
        val chargePointTitleText: TextView = findViewById(R.id.nameText)
        val addressLine1Text: TextView = findViewById(R.id.addressLine1Text)
        val postcodeText: TextView = findViewById(R.id.postcodeText)
        val countryText: TextView = findViewById(R.id.countryText)
        val distanceText: TextView = findViewById(R.id.distanceText)

        //Set the text of the textviews to be the corresponding data
        chargePointTitleText.text = chargePointTitle
        addressLine1Text.text = addressLine1
        postcodeText.text = postcode
        countryText.text = country
        distanceText.text = distance

        //Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //Assign the spinner
        placesSpinner = findViewById(R.id.placesSpinner)

        //Set an onItemSelectedListener to listen to the user's spinner selection
        placesSpinner.onItemSelectedListener = object : OnItemSelectedListener {

            //When the user selects an item, get nearby places related to that selection
            //by passing the charge points location and the type of place selected by the user
            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View, position: Int, id: Long) {
                if (placesSpinner.getItemAtPosition(position).toString() == "Cafe"){
                    placeType = "cafe"
                    getNearbyPlace(latitude, longitude, placeType)
                }
                else if (placesSpinner.getItemAtPosition(position).toString() == "Convenience Store"){
                    placeType = "convenience_store"
                    getNearbyPlace(latitude, longitude, placeType)
                }
                else if (placesSpinner.getItemAtPosition(position).toString() == "Gym"){
                    placeType = "gym"
                    getNearbyPlace(latitude, longitude, placeType)
                }
                else if (placesSpinner.getItemAtPosition(position).toString() == "Library"){
                    placeType = "library"
                    getNearbyPlace(latitude, longitude, placeType)
                }
                else if (placesSpinner.getItemAtPosition(position).toString() == "Cinema"){
                    placeType = "movie_theater"
                    getNearbyPlace(latitude, longitude, placeType)
                }
                else if (placesSpinner.getItemAtPosition(position).toString() == "Museum"){
                    placeType = "museum"
                    getNearbyPlace(latitude, longitude, placeType)
                }
                else if (placesSpinner.getItemAtPosition(position).toString() == "Pharmacy"){
                    placeType = "pharmacy"
                    getNearbyPlace(latitude, longitude, placeType)
                }
                else if (placesSpinner.getItemAtPosition(position).toString() == "Restaurant"){
                    placeType = "restaurant"
                    getNearbyPlace(latitude, longitude, placeType)
                }
            }

            //If the user doesn't select a place, the default is cafe.
            override fun onNothingSelected(parentView: AdapterView<*>) {
                placeType = "cafe"
            }
        }

        //Creation of an Intent object that will pass the charge points latitude and longitude
        //out to the google maps application to allow turn-by-turn navigation to the charge point
        var gmIntentUri: Uri = Uri.parse("google.navigation:q=$latitude,$longitude")
        var mapIntent = Intent(Intent.ACTION_VIEW, gmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")

        //Implementation of the button that will activate the turn-by-turn navigation through google maps
        goThereButton.setOnClickListener {
            startActivity(mapIntent)
        }
    }

    //Implementation of the spinner's onNothingSelected method
    override fun onNothingSelected(parent: AdapterView<*>?) {}

    //Implementation of the spinner's onItemSelected method
    //Set the place type to be the user's selection from the spinner
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        placeType = parent?.getItemAtPosition(position).toString()
    }

    //Function that passes the location of the charge point and the user's preferred things to do place type
    //to the presenter
    private fun getNearbyPlace(lat: Float, lng: Float, placeType: String){
        val latString = lat.toString()
        val lngString = lng.toString()
        presenter.getNearbyPlaces(latString, lngString, placeType)
    }

    //Implementation of the presenter's returnPlaces method that will send the nearbyPlaces object to
    //the Detail Adapter to be displayed
    override fun returnPlaces(nearbyPlaces: PlaceInfo?) {
        runOnUiThread{
            //Pass the charge points to the adapter to be displayed
            recyclerView_places.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            recyclerView_places.adapter = DetailAdapter(nearbyPlaces)
        }
    }

    //onMapReady and setUpMap set the map up when the activity is started
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMarkerClickListener(this)

        setUpMap()
    }

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                DetailActivity.LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        map.isMyLocationEnabled = true

        map.uiSettings.isMyLocationButtonEnabled = false

        map.uiSettings.isZoomControlsEnabled = false

        map.mapType = GoogleMap.MAP_TYPE_NORMAL

        placeMarkerOnMap(latLng)

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))

    }

    //Function to place a marker on the map at a defined location
    private fun placeMarkerOnMap(location: LatLng){

        val markerOptions = MarkerOptions().position(location)

        markerOptions.icon(bitmapDescriptorFromVector(this, R.drawable.power_cord))

        map.addMarker(markerOptions)
    }

    private fun bitmapDescriptorFromVector(context: Context, resID: Int): BitmapDescriptor {
        var vectorDrawable = ContextCompat.getDrawable(context, resID)
        vectorDrawable?.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight())
        var bitmap = Bitmap.createBitmap(vectorDrawable!!.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888)
        var canvas = Canvas(bitmap)
        vectorDrawable?.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    //Implementation of the onMarkerClick method, this is not used in this activity
    override fun onMarkerClick(p0: Marker?): Boolean {
        TODO("not implemented")
    }

    //Constants needed in this activity
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    //Detach the presenter view when this activity is destroyed
    override fun onDestroy() {
        presenter.detachView()
        super.onDestroy()
    }


}

