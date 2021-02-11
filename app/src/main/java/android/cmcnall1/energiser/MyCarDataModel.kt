package android.cmcnall1.energiser

import android.graphics.drawable.Drawable

data class MyCarInfo(
    var carModel: String?,
    var carImage: Drawable?,
    var isEV: Boolean?,
    var chargeRemaining: Float?,
    var chargeRemainingPercentage: Float?,
    var milesRemaining: Float?,
    var kmRemaining: Float?,
    var kWhTotalConsumption: Float?,
    var kWhAverageConsumption: Float?,
    var mileage: Float?,
    var dateLastCharge: String?,
    var hoursLastCharge: String?
)