package android.cmcnall1.energiser

import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.google.android.apps.auto.sdk.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.*
import kotlin.collections.ArrayList
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import org.koin.core.KoinContext
import org.koin.standalone.StandAloneContext

class AutoActivity : CarActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener, MainPresenter.View, AutoListMenuAdapter.MenuCallbacks, AutoDetailFragment.ChargePointListener, AutoFiltersFragment.FilterListener{

    //Charge point variable to store the array of charge points received from the Charge Point Service
    private var myChargePoints: Array<ChargePoint?> = arrayOf()

    //Latitude and Longitude variable to hold the location the user has searched for
    private var chosenLatLng: LatLng? = null

    //Inline function to handle the Koin dependancy injection. Koin does not support CarActivitiy()
    //hence the need to create this inject function.
    inline fun <reified T : Any> inject(name: String = "")
            = lazy { (StandAloneContext.koinContext as KoinContext).get<T>(name) }

    //Main Presenter by injection
    private val presenter: MainPresenter by inject()

    //Google Maps API Key
    private val apiKey = "AIzaSyA2W69fcSdTDZEptQSLI5Q7iKn3E7rljvY"

    //Google Map variable
    private var map: GoogleMap? = null

    //Map View variable to hold the mao
    private var mapView: MapView? = null

    //List Menu Adapter to be used to display the list of charge points
    private var listMenuAdapter: AutoListMenuAdapter? = null

    //List of menu items to be used to store the list of items in the menu
    private val menuItems: ArrayList<MenuItem> = arrayListOf()

    //Menu controller that will be used to control the content and function of the menu
    private lateinit var menuController: MenuController

    //List of names for the menu items
    private val menuItemNames: List<String> = listOf("Explore Charge Points", "Favourites", "Filters")

    //Current marker
    private var currentMarker: Marker? = null

    private var currentChargePoint: ChargePoint? = null

    //Map buttons
    private lateinit var myZoomInButton: View
    private lateinit var myZoomOutButton: View
    private lateinit var myLocationButton: View

    private var autoDetailFragment: AutoDetailFragment? = null
    private var filtersFragment: AutoFiltersFragment? = null

    // Create a new token for the autocomplete session. Pass this to FindAutocompletePredictionsRequest,
    // and once again when the user makes a selection (for example when calling fetchPlace()).
    private var token: AutocompleteSessionToken = AutocompleteSessionToken.newInstance()

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        //Inflate the car main layout
        val view = layoutInflater.inflate(R.layout.car_main_layout, null)
        setContentView(view)

        // Initialize Places.
        Places.initialize(this, apiKey)

        //Attach view to the presenter
        presenter.attachView(this)

        //find and setup the map view
        mapView = view.findViewById(R.id.map_view) as MapView
        mapView?.onCreate(bundle)
        mapView?.visibility = View.VISIBLE
        mapView?.getMapAsync(this)


        //Adding the Explore Charge Points submenu to the list of menu items
        menuItems.add(MenuItem.Builder()
            .setTitle(menuItemNames[0])
            .setType(MenuItem.Type.SUBMENU)
            .build())

        //Adding the Favourites item to the list of menu items
        menuItems.add(MenuItem.Builder()
            .setTitle(menuItemNames[1])
            .setType(MenuItem.Type.ITEM)
            .build())

        //Adding the Filters item to the list of menu items
        menuItems.add(MenuItem.Builder()
            .setTitle(menuItemNames[2])
            .setType(MenuItem.Type.ITEM)
            .build())


        // Add this block to create the menu.
        menuController = carUiController.menuController
        menuController.setRootMenuAdapter(mRootMenuAdapter)
        menuController.showMenuButton()

        //Add this block to show the app name in the status bar.
        val statusBarController = carUiController.statusBarController
        statusBarController.setTitle(resources.getString(R.string.app_name))


        // Add this block to enable search.
        val searchController = carUiController.searchController
        searchController.setSearchHint(getString(R.string.auto_search_hint))
        searchController.setSearchCallback(mSearchCallback)
        searchController.showSearchBox()
    }

    //Menu adapter to set up the menu and to handle the user selection
    private val mRootMenuAdapter = object : MenuAdapter() {
        private val MAX_ITEMS = 3

        override fun getMenuItemCount(): Int {
            return MAX_ITEMS
        }

        override fun getMenuItem(i: Int): MenuItem {
            return menuItems[i]
        }

        override fun onMenuItemClicked(position: Int) {
            if (position == 2){

                if (filtersFragment ==null) {
                    filtersFragment = AutoFiltersFragment()
                    filtersFragment?.setFilterListener(this@AutoActivity)

                    supportFragmentManager.beginTransaction()
                        .replace(R.id.charge_point_details_card, filtersFragment!!, "filterChargePoints")
                        .commit()
                } else {
                    supportFragmentManager.beginTransaction()
                        .show(filtersFragment!!)
                        .commit()
                }

            }
        }

        override fun onEnter() {
            listMenuAdapter?.updateChargePointList()
            super.onEnter()
        }

        override fun onLoadSubmenu(position: Int): MenuAdapter? {
            val menuItem = getMenuItem(position)

            if (menuItem.title == "Explore Charge Points"){
                return listMenuAdapter
            }
            throw IllegalArgumentException("Unrecognized submenu requested, position: $position")
        }
    }

    //Handler object to handle any interaction that is needed with the UI thread
    //(i.e. any UI changes that need to be made after initial start up, such as map updates)
    private val handler: Handler = object : Handler(Looper.getMainLooper()) {
        //The handler works by receiving a message and depending on the content of the message,
        //the handler will execute a certain section of code.
        override fun handleMessage(msg: Message?) {

            when (msg?.what){

                //if the message is PLACE_CHARGE_POINTS_ON_MAP (0), display charge point locations on map
                0 -> placeMarkersOnMap(myChargePoints)

                //if the message is ZOOM_MAP_CAMERA_TO_LOCATION (1), zoom to the user's chosen location
                1 -> map?.animateCamera(CameraUpdateFactory.newLatLngZoom(chosenLatLng, 12f))

                //if the message is CLEAR_MAP (2), clear all content from the map
                2 -> map?.clear()

                //if message ADJUST_MAP_CARD_NOT_SHOWING (3), adjust the map accordingly
                3 -> adjustMapPadding(false)

                //if message is ADJUST_MAP_CARD_SHOWING (4), adjust the map accordingly
                4 -> adjustMapPadding(true)

                //If it is none of those messages, don't do anything
                else -> super.handleMessage(msg)
            }
        }
    }

    //Override function from the Main Presenter to handle the returned charge points
    override fun returnChargePoints(chargePoints: Array<ChargePoint?>) {
        //Set the my charge point variable to be the returned charge points
        myChargePoints = chargePoints
        //Clear any current points from the map via the handler
        handler.sendEmptyMessage(CLEAR_MAP)
        //Place the new charge points on the map via the handler
        handler.sendEmptyMessage(PLACE_CHARGE_POINTS_ON_MAP)

        listMenuAdapter = AutoListMenuAdapter(this@AutoActivity, myChargePoints)
    }

    //Once the map has been created and is ready to be used, the code within this function is executed
    override fun onMapReady(googleMap: GoogleMap) {

        //set the activity's map variable to be the created map
        map = googleMap
        //Turn off the built-in zoom controls as custom controls will be used
        map?.uiSettings?.isZoomControlsEnabled = false
        //Turn off the built-in my location button as a custom button will be used
        map?.uiSettings?.isMyLocationButtonEnabled = false
        //Set the marker click listener
        map?.setOnMarkerClickListener(this)
        //adjust the map's padding on creation of the map. No detail card is showing
        handler.sendEmptyMessage(ADJUST_MAP_CARD_NOT_SHOWING)

        //User location permission check
        if (ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(AppCompatActivity().parent,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE)
                return
            }
        //Enable the user's location to be shown on the map
        map?.isMyLocationEnabled = true

        //Initialise the map after the above changes have been made
        try {
            MapsInitializer.initialize(this)
        } catch (e: GooglePlayServicesNotAvailableException) {
            e.printStackTrace()
        }

        //Set up the custom map controls by passing the buttons to the setup function
        setupMapControls(
            findViewById(R.id.zoom_in_button),
            findViewById(R.id.zoom_out_button),
            findViewById(R.id.my_location_button)
        )

        //Call the current location function to get the charge points around the user's location on setup
        getCurrentLocation()
    }

    //Function to place markers at the location of a given set of charge points on the map
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

                //Marker title that displays on click of the marker
                val titleStr = chargePoints[i]?.AddressInfo?.Title

                //add marker title to show info about the charge point
                markerOptions.title(titleStr)

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
                val locationMarker = map?.addMarker(markerOptions)

                //associate the charge point data with the marker
                locationMarker?.tag = chargePoints[i]
            }
        }
    }

    private fun bitmapDescriptorFromVector(context: Context, resID: Int): BitmapDescriptor {
        var vectorDrawable = ContextCompat.getDrawable(context, resID)
        vectorDrawable?.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight())
        var bitmap = Bitmap.createBitmap(vectorDrawable!!.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888)
        var canvas = Canvas(bitmap)
        vectorDrawable?.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }


    //Function to set up the custom map controls
    private fun setupMapControls(zoomInButton: View, zoomOutButton: View, locationButton: View){
        myZoomInButton = zoomInButton
        myZoomOutButton = zoomOutButton
        myLocationButton = locationButton

        //When the zoom in button is pressed, zoom in the map camera
        myZoomInButton.setOnClickListener {
            val cameraUpdate = CameraUpdateFactory.zoomBy(1f)
            map?.animateCamera(cameraUpdate)
        }

        //When the zoom out button is pressed, zoom out the map camera
        myZoomOutButton.setOnClickListener {
            val cameraUpdate = CameraUpdateFactory.zoomBy(-1f)
            map?.animateCamera(cameraUpdate)
        }

        //When the my location button is pressed, call the current location function
        myLocationButton.setOnClickListener {
            closeDetailsFragment()
            getCurrentLocation()
        }
    }

    //Current location function that gets the user's current location and displays the charge points nearest to it
    private fun getCurrentLocation() {

        //Location permission check
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                AppCompatActivity().parent,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
            return
        }

        //Fused location client to hold the user's location
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fusedLocationClient.lastLocation.addOnSuccessListener{ location ->

            if(location != null){
                //Create a latLng object for the current latitude and longitude
                val currentLatLng = LatLng(location.latitude, location.longitude)

                //Zoom the camera to the user's current location
                map?.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))

                //Get the charge points around the user's current location
                presenter.useCurrentLocation(location.longitude.toString(), location.latitude.toString())
            }
        }
    }

    //Search callback object that controls what happens when the user interacts with the search bar
    private val mSearchCallback: SearchCallback = object: SearchCallback() {

        //Called when a search is submitted
        override fun onSearchSubmitted(s: String?): Boolean {

            //Places client used to communicate with the Places API
            val placesClient: PlacesClient = Places.createClient(applicationContext)

            //Autocomplete prediction request used to query the Places API with the user's search term
            val request: FindAutocompletePredictionsRequest = FindAutocompletePredictionsRequest.builder()
                .setSessionToken(token)
                .setQuery(s)
                .build()

            //Array to hold the results of the Places API query
            val results: ArrayList<SearchItem> = ArrayList()

            //Find results for the user's search via the Places client
            placesClient.findAutocompletePredictions(request).addOnSuccessListener { response ->
                //loop through the array of predictions received from the Places API
                for (prediction in response.autocompletePredictions) {
                    //Transform each result into a SearchItem and add it to the array of results
                    val result = SearchItem.Builder()
                        .setType(SearchItem.Type.SEARCH_RESULT)
                        .setTitle(prediction.getPrimaryText(null).toString())
                        .setSubDescription(prediction.placeId)
                        .build()
                    results.add(result)
                }

                //add the set of SearchItems to be shown on the UI
                carUiController.searchController.setSearchItems(results)

            }.addOnFailureListener { exception ->
                if (exception is ApiException) {
                    Log.e(TAG, "Place not found: " + exception.statusCode)
                }
            }
            return false
        }

        //Called as the user types their search
        override fun onSearchTextChanged(s: String?) {

            //Places client used to communicate with the Places API
            val placesClient: PlacesClient = Places.createClient(applicationContext)

            //Autocomplete prediction request used to query the Places API with the user's search term
            val request: FindAutocompletePredictionsRequest = FindAutocompletePredictionsRequest.builder()
                .setSessionToken(token)
                .setQuery(s)
                .build()

            //Array to hold the returned search suggestions
            val suggestions: ArrayList<SearchItem> = ArrayList()

            //Find suggestions for the user's search via the Places client
            placesClient.findAutocompletePredictions(request).addOnSuccessListener { response ->
                for (prediction in response.autocompletePredictions) {
                    val item = SearchItem.Builder()
                        .setType(SearchItem.Type.SUGGESTION)
                        .setTitle(prediction.getPrimaryText(null).toString())
                        .build()
                    suggestions.add(item)
                }

                //Show the suggestions on the UI
                carUiController.searchController.setSearchItems(suggestions)

            }.addOnFailureListener { exception ->
                if (exception is ApiException) {
                    Log.e(TAG, "Place not found: " + exception.statusCode)
                }
            }
        }

        //Called when the user selects an item from their list of returned search items
        override fun onSearchItemSelected(searchItem: SearchItem?) {

            //Places client used to communicate with the Places API
            val placesClient: PlacesClient = Places.createClient(applicationContext)

            //List of fields to be returned from the places API
            //In this case, only latitude and longitude is needed
            val fields = listOf(Place.Field.LAT_LNG)

            //Fetch Place Request creation using the place id of the chosen search result
            val request = FetchPlaceRequest.builder(searchItem?.subDescription.toString(), fields).build()

            //Close the details fragment if it is open
            closeDetailsFragment()

            //Fetch the latitude and longitude of the place chosen by the user
            placesClient.fetchPlace(request).addOnSuccessListener{ response ->

                //place variable to hold the fetched response
                val place = response.place

                //latitude and longitude of the fetched placed
                val latitude = place.latLng?.latitude
                val longitude = place.latLng?.longitude

                //Set the chosen LatLng variable
                chosenLatLng = place.latLng

                //Zoom the camera to the chosen location via the handler
                handler.sendEmptyMessage(ZOOM_MAP_CAMERA_TO_LOCATION)

                //Get the charge points near the chosen location via the presenter
                presenter.usePlaceLocation(longitude.toString(), latitude.toString())
            }
        }
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        val selectedChargePoint = marker?.tag as ChargePoint

        currentChargePoint = selectedChargePoint

        chosenLatLng = LatLng(
            selectedChargePoint.AddressInfo?.Latitude?.toDouble()!!,
            selectedChargePoint.AddressInfo?.Longitude?.toDouble()!!
        )

        autoDetailFragment = AutoDetailFragment()
        autoDetailFragment!!.setChargePoint(selectedChargePoint)
        autoDetailFragment!!.setChargePointListener(this)

        supportFragmentManager.beginTransaction()
            .replace(R.id.charge_point_details_card, autoDetailFragment!!, "chargePointDetails")
            .commit()

        handler.sendEmptyMessage(ADJUST_MAP_CARD_SHOWING)
        handler.sendEmptyMessage(ZOOM_MAP_CAMERA_TO_LOCATION)

        return false
    }

    override fun onChargePointSelected(chargePoint: ChargePoint) {

        chosenLatLng = LatLng(chargePoint.AddressInfo?.Latitude?.toDouble()!!, chargePoint.AddressInfo?.Longitude?.toDouble()!!)

        currentChargePoint = chargePoint

        autoDetailFragment = AutoDetailFragment()
        autoDetailFragment!!.setChargePoint(chargePoint)
        autoDetailFragment!!.setChargePointListener(this)

        supportFragmentManager.beginTransaction()
            .replace(R.id.charge_point_details_card, autoDetailFragment!!, "chargePointDetails")
            .commit()

        handler.sendEmptyMessage(ADJUST_MAP_CARD_SHOWING)
        handler.sendEmptyMessage(ZOOM_MAP_CAMERA_TO_LOCATION)
    }

    private fun adjustMapPadding(isDetailCardShowing: Boolean){
        val left = resources.getDimensionPixelOffset(
            if (isDetailCardShowing) {
                R.dimen.map_card_side_padding
            } else{
                R.dimen.map_side_padding_left
            }
        )
        val right = 0
        val top = resources.getDimensionPixelOffset(R.dimen.map_top_padding)
        val bottom = 0

        map?.setPadding(left, top, right, bottom)
    }

    override fun closeFiltersFragment() {
        if (filtersFragment != null){
            supportFragmentManager.beginTransaction()
                .hide(filtersFragment!!)
                .commit()
        }

        handler.sendEmptyMessage(ADJUST_MAP_CARD_NOT_SHOWING)
    }

    override fun returnFilters(type2: Boolean, withinRange: Boolean, maxPrice: String, numConnectors: String) {
        presenter.filterChargePoints(type2, withinRange, maxPrice, numConnectors)
    }

    override fun navigateTo(chargePoint: ChargePoint) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun closeDetailsFragment() {
        if (autoDetailFragment != null) {
            supportFragmentManager.beginTransaction()
                .remove(autoDetailFragment!!)
                .commit()
        }

        handler.sendEmptyMessage(ADJUST_MAP_CARD_NOT_SHOWING)
    }

    override fun onResume() {
        mapView?.onResume()
        super.onResume()
    }

    //Constant values and codes used
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1

        private const val ADJUST_MAP_CARD_SHOWING = 4

        private const val ADJUST_MAP_CARD_NOT_SHOWING = 3

        private const val CLEAR_MAP = 2

        private const val ZOOM_MAP_CAMERA_TO_LOCATION = 1

        private const val PLACE_CHARGE_POINTS_ON_MAP = 0
    }

}