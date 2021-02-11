package android.cmcnall1.energiser

/*
    The Charge Point Repo handles any requests from the Presenter to fetch charge points
    and retrieves any responses from the Charge Point Service before passing the received
    charge points back to the presenter.
 */

class ChargePointRepo(private val chargePointService: ChargePointService,
                      private val filterService: FilterService): ChargePointService.DataListener,
    FilterService.FilterListener {

    //Repository listener that allows the Charge Point Repo to retrieve the received array of Charge Points
    var repository: Repository? = null

    var chargePoints: Array<ChargePoint?>? = null
    var filteredChargePoints: Array<ChargePoint?>? = null

    //Car Range in order to demonstrate the within range filter
    private val myCarRange = 2.4f

    //Initialise the charge point service listener in the charge point repo
    init {
        chargePointService.listener = this
        filterService.filterListener = this
    }

    //Function that is called from the Presenter that requests the Charge Point Service to
    //retrieve the charge points around the user's current location
    fun getCurrentLocation(long: String, lat: String) {
        chargePointService.fetchJson(long, lat)
    }

    //Function that is called from the Presenter that requests the Charge Point Service to
    //retrieve the charge points around the user's preferred location chose from their Place search
    fun usePlaceLocation(long: String, lat: String) {
        chargePointService.fetchJson(long, lat)
    }

    fun filterChargePoints(type2: Boolean, withinRange: Boolean, maxPrice: String, numConnectors: String){
        filterService.getFilteredChargePoints(chargePoints, myCarRange, type2, withinRange, maxPrice, numConnectors)
    }

    //Declaration of the repository listener that will communicate with the Presenter
    interface Repository {
        //Declaration of the function that sends the array of charge points to the Presenter
        fun returnChargePoint(chargePoints: Array<ChargePoint?>)
    }

    //Implementation of the response function from the Charge Point Service that passes on the
    //response to the Presenter
    override fun onHttpResponseReceived(response: Array<ChargePoint?>) {
        chargePoints = response
        repository?.returnChargePoint(response)
    }

    override fun onFilteredResponse(response: Array<ChargePoint?>) {
        filteredChargePoints = response
        repository?.returnChargePoint(response)
    }

}