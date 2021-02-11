package android.cmcnall1.energiser


class FilterService {

    var filterListener: FilterListener? = null

    fun getFilteredChargePoints(
        chargePoints: Array<ChargePoint?>?,
        myCarData: Float, //This should be replaced with Wireless Car data set
        type2: Boolean,
        withinRange: Boolean,
        maxPrice: String,
        numConnectors: String
    ) {

        var type2Check = false
        var withinRangeCheck = false
        var maxPriceCheck = false
        var numConnectorsCheck = false

        if (type2) {
            type2Check = true
        }

        if (withinRange) {
            withinRangeCheck = true
        }

        if (numConnectors != "Any") {
            numConnectorsCheck = true
        }

        if (maxPrice != "1000") {
            maxPriceCheck = true
        }

        var filterChargePoints = chargePoints


        var filterChargePoints1 = filterChargePoints?.filter {
            if (type2Check)
                it?.let { chargePoint ->
                    chargePoint.checkConnectorType("type 2")
                } ?: false
            else {
                true
            }
        }


        var filterChargePoints2 = filterChargePoints1?.filter {
            if (numConnectorsCheck)
                it?.let { chargePoint ->
                    chargePoint.checkConnectors(numConnectors)
                } ?: false
            else {
                true
            }
        }

        var filterChargePoints3 = filterChargePoints2?.filter {
            if (maxPriceCheck)
                it?.let { chargePoint ->
                    chargePoint.checkMaxPrice(maxPrice)
                } ?: false
            else {
                true
            }
        }

        var filterChargePoints4 = filterChargePoints3?.filter {
            if (withinRange)
                it.let {chargePoint ->
                    chargePoint?.checkRange(myCarData)
                }?: false
            else {
                true
            }
        }

        var filteredChargePoints: Array<ChargePoint?> = filterChargePoints4!!.toTypedArray()

        filterListener?.onFilteredResponse(filteredChargePoints)
    }

    interface FilterListener {
        fun onFilteredResponse(response: Array<ChargePoint?>)
    }

}