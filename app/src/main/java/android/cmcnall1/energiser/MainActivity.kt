package android.cmcnall1.energiser

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.annotation.DrawableRes
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.*
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject
import java.util.*

class MainActivity : AppCompatActivity(), PlaceSelectionListener, MainPresenter.View,
    OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    GoogleMap.OnInfoWindowClickListener{

    //Main Presenter by injection
    private val presenter: MainPresenter by inject()

    //Google map variable
    private lateinit var map: GoogleMap

    //Seekbars
    private lateinit var priceSeekBar: SeekBar
    private lateinit var connectorSeekBar: SeekBar

    //Text Fields
    private lateinit var priceTextView: TextView
    private lateinit var connectorTextView: TextView

    //Buttons
    private lateinit var resetFilterButton: Button
    private lateinit var filterButton: ImageView
    private lateinit var locationButton: Button
    private lateinit var cancelButton: Button

    //Toggle Buttons
    private lateinit var showAllToggle: ToggleButton
    private lateinit var type2Toggle: ToggleButton
    private lateinit var rangeToggle: ToggleButton

    //View Variables
    private lateinit var filterView: View
    private lateinit var buttonView: View
    private lateinit var recyclerView: View
    private lateinit var cardView: View

    //Animation duration variable
    private var shortAnimationDuration: Int = 0

    //Google Maps API key
    private val apiKey = "AIzaSyA2W69fcSdTDZEptQSLI5Q7iKn3E7rljvY"

    //Variables to hold the user's preferred longitude and latitude
    private lateinit var userLongitude: String
    private lateinit var userLatitude: String

    //Fused location provider client
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        //Set the correct app theme after the splash screen has been displayed
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)

        //Hide the Action Bar/Title Bar
        try {
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) {
        }

        setContentView(R.layout.activity_main)

        // Initialize Places.
        Places.initialize(applicationContext, apiKey)

        //Declaration of the My Car Fragment
        val myCarFragment = MyCarFragment.newInstance()
        //Declaration of the Settings Fragment
        val settingsFragment = SettingsFragment.newInstance()

        //Tab bar navigation listener that listens for the user's tab selection
        val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                //Display the explore tab
                R.id.navigation_explore -> {
                    //Show sliding panel in the Explore tab, including the map
                    sliding_layout.visibility = View.VISIBLE
                    //Close the my car fragment
                    supportFragmentManager.beginTransaction().remove(myCarFragment).commitAllowingStateLoss()
                    //Close the settings fragment
                    supportFragmentManager.beginTransaction().remove(settingsFragment).commitAllowingStateLoss()
                    //Hide the fragment container
                    fragmentContainer.visibility = View.GONE

                    return@OnNavigationItemSelectedListener true
                }
                //Display the my car tab
                R.id.navigation_my_car -> {
                    //Hide sliding panel in the My Car tab including map
                    sliding_layout.visibility = View.GONE
                    //Show the fragment container
                    fragmentContainer.visibility = View.VISIBLE
                    //Close the my settings fragment
                    supportFragmentManager.beginTransaction().remove(settingsFragment).commitAllowingStateLoss()
                    //Open the my car fragment
                    openFragment(myCarFragment)

                    //Close filters
                    closeFilter()

                    return@OnNavigationItemSelectedListener true
                }
                //Display the settings tab
                R.id.navigation_settings -> {
                    //Hide sliding panel in the Settings tab including map
                    sliding_layout.visibility = View.GONE
                    //Show the fragment container
                    fragmentContainer.visibility = View.VISIBLE
                    //Close the my car fragment
                    supportFragmentManager.beginTransaction().remove(myCarFragment).commitAllowingStateLoss()
                    //Open the settings fragment
                    openFragment(settingsFragment)

                    //Close filters
                    closeFilter()

                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //Attach the view
        presenter.attachView(this)

        //Initialise the buttons
        locationButton = findViewById(R.id.location_button)
        filterButton = findViewById(R.id.filter_button)
        cancelButton = findViewById(R.id.cancelFilterButton)
        resetFilterButton = findViewById(R.id.resetButton)

        //Assigning views
        filterView = findViewById(R.id.filter_layout)
        buttonView = findViewById(R.id.buttonLayout)
        recyclerView = findViewById(R.id.recyclerView_main)
        cardView = findViewById(R.id.cardView)

        //SeekBar initialisation
        priceSeekBar = findViewById(R.id.priceSeekbar)
        priceSeekBar.incrementProgressBy(1)
        connectorSeekBar = findViewById(R.id.connectorsSeekbar)

        //Toggle Button Initialisation
        showAllToggle = findViewById(R.id.showAllToggle)
        type2Toggle = findViewById(R.id.type2Toggle)
        rangeToggle = findViewById(R.id.withinRangeToggle)

        //Filter Text View initialisation
        priceTextView = findViewById(R.id.showPrice)
        connectorTextView = findViewById(R.id.showConnectors)

        //Hide filters
        filterView.visibility = View.GONE

        //Retrieve and cache the system's default "short" animation time.
        shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)

        //Layout Manager for the recycler view
        recyclerView_main.layoutManager = LinearLayoutManager(this)

        //Create a value to hold the autocomplete fragment
        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                    as AutocompleteSupportFragment?

        //Use Place.Field to choose what information you would like
        //to return from the Places API
        autocompleteFragment?.setPlaceFields(
            Arrays.asList(Place.Field.ID,Place.Field.LAT_LNG,
                Place.Field.NAME))

        //Listen to what he user types in the autocomplete fragment
        //If a place is selected successfully, go to onPlaceSelected()
        //If an error occurs, got to onError()
        autocompleteFragment?.setOnPlaceSelectedListener(this)

        //Display the charge points around the user's current location at start up
        getCurrentLocation()

        //Button Listeners
        locationButton.setOnClickListener {
            getCurrentLocation()
        }

        //Set a variable for the sliding panel
        val slidingLayout: SlidingUpPanelLayout = findViewById(R.id.sliding_layout)

        //Set the anchor point. This is an intermediate stopping point for the sliding panel
        slidingLayout.anchorPoint = 0.11f

        //Make the recyclerView for the list of charge points scrollable within the sliding panel
        slidingLayout.setScrollableView(recyclerView_main)

        //Set a listener for the tab bar at the bottom of the activity
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        filterButton.setOnClickListener {
            showFilter()
        }

        cancelButton.setOnClickListener {
            closeFilter()
        }

    }

    override fun onPlaceSelected(place: Place) {
        //Latitude/Longitude object containing the places lat/long
        if(place.latLng != null) {
            val latLng: LatLng = place.latLng!!

            //Assign the lat and long of the place to the relevant variables
            userLatitude = latLng.latitude.toString()
            userLongitude = latLng.longitude.toString()

            //Request the presenter to get show charge points around the chosen place
            presenter.usePlaceLocation(userLongitude, userLatitude)

            //Show the place picked on the map
            showPlacePicked(latLng)
        }
    }

    //Function to open a specified fragment when called
    private fun openFragment(fragment: Fragment){
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.commit()
    }


    override fun onError(place: Status) {
        //Show toast if an error occurs during the place picking process
        Toast.makeText(this, "Error getting chosen location", Toast.LENGTH_LONG).show()
    }

    private fun showFilter() {
        filterView.apply {
            //Set the content view to 0% opacity but visible, so that is is
            //visible (but fully transparent) during the animation.
            alpha = 0f
            visibility = View.VISIBLE

            //Animate the content view to 100% opacity, and clear any
            //animation listener set on the view
            animate()
                .alpha(1f)
                .setDuration(shortAnimationDuration.toLong())
                .setListener(null)
        }

        //Max price SeekBar implementation
        priceSeekBar.incrementProgressBy(10)
        //Max price SeekBar implementation
        priceSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            var progressChangedValue: Float = 0.00F

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                progressChangedValue = progress.toFloat() / 100

                when (progressChangedValue) {
                    0.00F -> priceTextView.setText("Free")
                    10.00F -> priceTextView.setText("Any")
                    else -> priceTextView.setText("£" + "%.2f".format(progressChangedValue))
                }
                filterChargePoints(
                    type2Toggle.isChecked,
                    rangeToggle.isChecked,
                    priceSeekBar.progress.toString(),
                    connectorTextView.text.toString()
                )
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                filterButton.setImageResource(R.drawable.filter_set)
            }
        })

        //Number of connectors SeekBar implementation
        connectorSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            var progressChangedValue = 0

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                progressChangedValue = progress

                if (progressChangedValue == 0) {
                    connectorTextView.setText("Any")
                } else {
                    connectorTextView.setText(progressChangedValue.toString())
                }
                filterChargePoints(
                    type2Toggle.isChecked,
                    rangeToggle.isChecked,
                    priceSeekBar.progress.toString(),
                    connectorTextView.text.toString()
                )
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                filterButton.setImageResource(R.drawable.filter_set)
            }
        })

        //Toggle Button Listeners
        showAllToggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                rangeToggle.setChecked(false)
                type2Toggle.setChecked(false)
            }
            filterChargePoints(
                type2Toggle.isChecked,
                rangeToggle.isChecked,
                priceSeekBar.progress.toString(),
                connectorTextView.text.toString()
            )
        }

        type2Toggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showAllToggle.setChecked(false)
            }
            filterChargePoints(
                type2Toggle.isChecked,
                rangeToggle.isChecked,
                priceSeekBar.progress.toString(),
                connectorTextView.text.toString()
            )
            filterButton.setImageResource(R.drawable.filter_set)
        }

        rangeToggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showAllToggle.setChecked(false)
            }
            filterChargePoints(
                type2Toggle.isChecked,
                rangeToggle.isChecked,
                priceSeekBar.progress.toString(),
                connectorTextView.text.toString()
            )
            filterButton.setImageResource(R.drawable.filter_set)
        }

        resetFilterButton.setOnClickListener {
            connectorSeekBar.progress = 0
            priceSeekBar.progress = 1000
            showAllToggle.isChecked = true

            filterChargePoints(
                type2Toggle.isChecked,
                rangeToggle.isChecked,
                priceSeekBar.progress.toString(),
                connectorTextView.text.toString()
            )
            filterButton.setImageResource(R.drawable.filter_not_set)
        }

        hiddenCloseButton.setOnClickListener {
            closeFilter()
        }
    }

    private fun closeFilter(){
        if (!type2Toggle.isChecked && !rangeToggle.isChecked && priceSeekBar.progress == 100 && connectorSeekBar.progress == 0){
            filterButton.setImageResource(R.drawable.filter_not_set)
        }
        filterView.visibility = View.GONE
    }

    private fun filterChargePoints(type2: Boolean, withinRange: Boolean, maxPrice: String, numConnectors: String){

        presenter.filterChargePoints(type2, withinRange, maxPrice, numConnectors)

    }

    //Function to get the user's current location, display it on the map and get charge points around it
    private fun getCurrentLocation(){
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)!=
            PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fusedLocationClient.lastLocation.addOnSuccessListener(this){ location ->

            if(location != null){
                //User's latitude and longitude
                val long = location.longitude.toString()
                val lat = location.latitude.toString()
                //Create a latLng object for the current latitude and longitude
                val currentLatLng = LatLng(location.latitude, location.longitude)
                //Clear the map of all markers before getting the current location
                map.clear()
                //Move camera to show the current user position on the map
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
                //Request the presenter to get show charge points around user's location
                presenter.useCurrentLocation(long,lat)
            }
        }
    }

    //Implementation of the presenter function to show found charge points in the list and on the map
    override fun returnChargePoints(chargePoints: Array<ChargePoint?>) {

        runOnUiThread{
            //Clear all markers/items from map
            map.clear()
            //Pass the charge points to the adapter to be displayed
            recyclerView_main.adapter = MainAdapter(chargePoints)

            //Place a marker on the map for each charge point
            placeMarkersOnMap(chargePoints)
        }
    }

    //Manipulates the map once available. This callback is triggered when the map is ready to be used.
    //This is where we can add markers or lines, add listeners or move the camera.
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMarkerClickListener(this)
        map.setOnInfoWindowClickListener(this)

        //Function to set up the map
        setUpMap()
    }

    //Function to set up the map
    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        map.isMyLocationEnabled = true
        map.mapType = GoogleMap.MAP_TYPE_NORMAL
    }

    //Function to place markers at the location of the charge points
    private fun placeMarkersOnMap(chargePoints: Array<ChargePoint?>){

        //Loop through the charge points
        for (i in 0 until chargePoints.size) {
            //Check that the charge point location is not null
            if (chargePoints[i]?.AddressInfo?.Latitude != null && chargePoints[i]?.AddressInfo?.Longitude != null) {
                //Create a LatLng object for the charge point
                val location = LatLng(
                    chargePoints[i]?.AddressInfo?.Latitude!!.toDouble(),
                    chargePoints[i]?.AddressInfo?.Longitude!!.toDouble()
                )
                //Create a marker options object that will hold maker preferences like colour and title
                val markerOptions = MarkerOptions().position(location)

                //Marker title and information snippet that displays on click of the marker
                val titleStr = chargePoints[i]?.AddressInfo?.Title
                val snippetStr = "tap for more info"

                //add marker title and snippet to show info about the charge point
                markerOptions.title(titleStr)
                    .snippet(snippetStr)

                //set marker colour depending on the connection type
                if(chargePoints[i]?.Connections != null) {
                    if (chargePoints[i]?.checkConnectorType("type 2")!!) {
                        markerOptions.icon(bitmapDescriptorFromVector(this, R.drawable.charge_point_green))
                    } else {
                        markerOptions.icon(bitmapDescriptorFromVector(this, R.drawable.charge_point_red))
                    }
                } else {
                    markerOptions.icon(bitmapDescriptorFromVector(this, R.drawable.charge_point_amber))
                }


                //Marker object that displays on the map
                val locationMarker = map.addMarker(markerOptions)

                //associate the charge point data with the marker
                locationMarker.tag = chargePoints[i]
            }
        }
    }

    private fun bitmapDescriptorFromVector(context: Context, resID: Int): BitmapDescriptor{
        var vectorDrawable = ContextCompat.getDrawable(context, resID)
        vectorDrawable?.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight())
        var bitmap = Bitmap.createBitmap(vectorDrawable!!.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888)
        var canvas = Canvas(bitmap)
        vectorDrawable?.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }



    //function to show the place searched for and picked by the user on the map
    private fun showPlacePicked(location: LatLng){
//        //Create a marker options object that will hold maker preferences like colour and title
//        val markerOptions = MarkerOptions().position(location)
//
//        //set marker colour to the accent app colour
//        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(262.0f))
//
//        //Add the new searched for marker on the map
//        map.addMarker(markerOptions)
//
        //Clear all markers/items from map
        map.clear()

        //Zoom into the new marker
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 16f))
    }

    //Function to do something when the info window for a map marker is clicked
    override fun onInfoWindowClick(marker: Marker?) {

        //retrieve the charge point data from the marker tag
        val chargePoint = marker?.tag as ChargePoint

        //create an intent object to hold the intent to start the detail activity
        val intent = Intent(this, DetailActivity::class.java)

        //add the data to use in the detail activity
        intent.putExtra(TITLE_KEY, chargePoint.AddressInfo?.Title)
        intent.putExtra(ADDRESS_LINE_1_KEY, chargePoint.AddressInfo?.AddressLine1)
        intent.putExtra(POSTCODE_KEY, chargePoint.AddressInfo?.Postcode)
        intent.putExtra(COUNTRY_KEY, chargePoint.AddressInfo?.Country?.Title)
        intent.putExtra(DISTANCE_KEY, chargePoint.AddressInfo?.Distance)
        intent.putExtra(LATITUDE_KEY, chargePoint.AddressInfo?.Latitude)
        intent.putExtra(LONGITUDE_KEY, chargePoint.AddressInfo?.Longitude)

        //start the detail activity
        startActivity(intent)
    }

    override fun onMarkerClick(p0: Marker?): Boolean = false

    companion object {
        //Location permission code constant
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1

        //keys for starting the detail activity
        const val TITLE_KEY = "TITLE"
        const val ADDRESS_LINE_1_KEY = "ADDRESS_LINE_1"
        const val POSTCODE_KEY = "POSTCODE"
        const val COUNTRY_KEY = "COUNTRY"
        const val DISTANCE_KEY = "DISTANCE"
        const val LATITUDE_KEY = "LATITUDE"
        const val LONGITUDE_KEY = "LONGITUDE"
    }

    override fun onDestroy() {
        presenter.detachView()
        super.onDestroy()
    }
}
