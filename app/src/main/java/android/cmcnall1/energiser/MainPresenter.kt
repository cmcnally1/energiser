package android.cmcnall1.energiser

/*
    The Main Presenter handles requests and updates from the Main Activity and the Charge Point Repo
 */

class MainPresenter(private val chargePointRepo: ChargePointRepo): ChargePointRepo.Repository{

    private var view: View? = null

    //Initialise the charge point repository listener in the main presenter
    init{
        chargePointRepo.repository = this
    }

    //When the view is created, it must attach to the presenter
    fun attachView(view: View){
        this.view = view
    }

    //detach from the presenter when the view is destroyed
    fun detachView() {
        this.view = null
    }

    //Function to request the charge point repo to get charge points near the user's location
    fun useCurrentLocation(long: String, lat: String){
        chargePointRepo.getCurrentLocation(long, lat)
    }

    //Function to request the charge point repo to get charge points near the user's chosen location
    fun usePlaceLocation(long: String, lat: String){
        chargePointRepo.usePlaceLocation(long, lat)
    }

    //Filter the charge points
    fun filterChargePoints(type2: Boolean, withinRange: Boolean, maxPrice: String, numConnectors: String){
        chargePointRepo.filterChargePoints(type2, withinRange, maxPrice, numConnectors)
    }

    //On getting nearby charge points, return the charge points to the activity via the view
    override fun returnChargePoint(chargePoints: Array<ChargePoint?>) {
        view?.returnChargePoints(chargePoints)
    }

    //View interface that will communicate with the main activity
    interface View {
        fun returnChargePoints(chargePoints: Array<ChargePoint?>)
    }


}