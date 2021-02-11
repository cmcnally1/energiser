package android.cmcnall1.energiser

/*
    The Detail Presenter handles requests and updates from the Detail Activity and the Nearby Places Repo
 */

class DetailPresenter(private val nearbyPlacesRepo: NearbyPlacesRepo): NearbyPlacesRepo.Repository{

    var view: View? = null

    //Initialise the nearby places repository listener in the detail presenter
    init{
        nearbyPlacesRepo.repository = this
    }

    //When the view is created, it must attach to the presenter
    fun attachView(view: View){
        this.view = view
    }

    //detach from the presenter when the view is destroyed
    fun detachView() {
        this.view = null
    }

    //Function that requests the repo to get nearby places based on the location and place type specified
    fun getNearbyPlaces(lat: String, lng:String, placeType: String){
        nearbyPlacesRepo.getNearbyPlaces(lat,lng,placeType)
    }

    //On getting nearby places, return the places to the activity via the view
    override fun returnPlaces(nearbyPlaces: PlaceInfo?) {
        view?.returnPlaces(nearbyPlaces)
    }

    //View interface that will communicate with the detail activity
    interface View {
        fun returnPlaces(nearbyPlaces: PlaceInfo?)
    }

}