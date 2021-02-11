package android.cmcnall1.energiser

class NearbyPlacesRepo(private val nearbyPlacesService: NearbyPlacesService): NearbyPlacesService.DataListener{

    var repository: Repository? = null

    init {
        nearbyPlacesService.listener = this
    }

    fun getNearbyPlaces(lat: String, lng: String, placeType:String){
        nearbyPlacesService.fetchJson(lng, lat, placeType)
    }

    override fun onHttpResponseReceived(response: PlaceInfo?) {
        repository?.returnPlaces(response)
    }

    interface Repository {
        fun returnPlaces(nearbyPlaces: PlaceInfo?)
    }
}