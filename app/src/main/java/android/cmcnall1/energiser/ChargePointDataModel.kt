package android.cmcnall1.energiser

import java.util.regex.Pattern

/*
    Data model for the charge points
 */

data class ChargePoint(
    val ID: Int,
    val UUID: String,
    val ParentChargePointId: Int,
    val DataProviderId: Int,
    val DataProvider: DataProvider?,
    val DataProvidersReference: String,
    val OperatorID: Int,
    val OperatorInfo: OperatorInfo?,
    val OperatorsReference: String,
    val UsageTypeID: Int,
    val UsageType: UsageType?,
    val UsageCost: String,
    val AddressInfo: AddressInfo?,
    val NumberOfPoints: Int,
    val GeneralComments: String,
    val DatePlanned: String,
    val DataLastConfirmed: String,
    val StatusTypeID: Int,
    val StatusType: StatusType?,
    val DateLastStatusUpdate: String,
    val DataQualityLevel: Int,
    val DateCreated: String,
    val SubmissionStatusTypeID: Int,
    val SubmissionStatus: SubmissionStatus?,
    val UserComments: List<UserComments>?,
    val PercentageSimilarity: Float,
    val Connections: List<Connections>?,
    val MediaItems: List<MediaItems>?,
    val MetadataValues: List<MetadataValues>?,
    val IsRecentlyVerified: Boolean,
    val DateLastVerified: String
){

    fun checkConnectorType(type: String): Boolean = Connections?.let { connections ->
        var hasConnectorType = false
        connections.forEach { connection ->
            if (connection.ConnectionType.Title.contains(type, true))
                hasConnectorType = true
        }
        return hasConnectorType
    } ?: false

    fun checkConnectors(numOfConnectors: String): Boolean = Connections?.let { connections ->
        return connections.count() == numOfConnectors.toInt()
    } ?: false

    fun checkMaxPrice(maxPrice: String): Boolean = UsageCost?.let { usageCost ->

        val regex = "([0-9]+[.][0-9]+)"

        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(usageCost)

        var prices: Array<String> = emptyArray()

        while (matcher.find()){
            prices += matcher.group()
        }

        val maxPriceFloat = maxPrice.toFloat()/100

        if (prices.isNotEmpty()) {
            val priceFloat = prices[0].toFloat()
            if (priceFloat < maxPriceFloat) {
                return true
            }
        }
        return false
    } ?: false

    fun checkRange(myCarRange: Float): Boolean = AddressInfo?.let { addressInfo ->
        return addressInfo.Distance < myCarRange
    } ?: false
}

data class DataProvider(val WebsiteURL: String,
                        val Comments: String,
                        val DataProviderStatusType: DataProviderStatusType,
                        val IsRestrictedEdit: Boolean,
                        val IsOpenDataLicensed: Boolean,
                        val IsApprovedImport: Boolean,
                        val License: String,
                        val DateLastImported: String,
                        val ID: Int,
                        val Title: String)

data class DataProviderStatusType(val IsProviderEnabled: Boolean,
                                  val ID: Int,
                                  val Title: String)

data class OperatorInfo(val WebsiteURL: String,
                        val Comments: String,
                        val PhonePrimaryContact: String,
                        val PhoneSecondaryContact: String,
                        val IsPrivateIndividual: Boolean,
                        val AddressInfo: String,
                        val BookingURL: String,
                        val ContactEmail: String,
                        val FaultReportEmail: String,
                        val IsRestrictedEdit: Boolean,
                        val ID: Int,
                        val Title: String)

data class UsageType(val IsPayAtLocation: Boolean,
                     val IsMembershipRequired: Boolean,
                     val IsAccessKeyRequired: Boolean,
                     val ID: Int,
                     val Title: String)

data class AddressInfo(val ID: Int,
                       val Title: String,
                       val AddressLine1: String,
                       val AddressLine2: String,
                       val Town: String,
                       val StateOrProvince: String,
                       val Postcode: String,
                       val CountryCode: Int,
                       val Country: Country,
                       val Latitude: Float,
                       val Longitude: Float,
                       val ContactTelephone1: String,
                       val ContactTelephone2: String,
                       val ContactEmail: String,
                       val AccessComments: String,
                       val RelatedURL: String,
                       val Distance: Float,
                       val DistanceUnit: Int)

data class Country(val ISOCode: String,
                   val ContinentCode: String,
                   val ID: Int,
                   val Title: String)

data class StatusType(val IsOperational: Boolean,
                      val IsUserSelectable: Boolean,
                      val ID: Int,
                      val Title: String)

data class SubmissionStatus(val IsLive: Boolean,
                            val ID: Int,
                            val Title: String)

//data class Connections(val Connection: Connection)

data class Connections(val ID: Int,
                       val ConnectionTypeID: Int,
                       val ConnectionType: ConnectionType,
                       val Reference: String,
                       val StatusTypeID: Int,
                       val StatusType: StatusType,
                       val LevelID: Int,
                       val Level: Level,
                       val Amps: Int,
                       val Voltage: Int,
                       val PowerKW: Float,
                       val CurrentTypeID: Int,
                       val CurrentType: CurrentType,
                       val Quantity: Int,
                       val Comments: String)

data class ConnectionType(val FormalName: String,
                          val IsDisconnected: Boolean,
                          val IsObsolete: Boolean,
                          val ID: Int,
                          val Title: String)

data class UserComments(val ID: Int,
                        val ChargePointID: Int,
                        val CommentTypeID: Int,
                        val CommentType: CommentType,
                        val UserName: String,
                        val Comment: String,
                        val Rating: Int,
                        val RelatedURL: String,
                        val DateCreated: String,
                        val User: User,
                        val CheckinStatusTypeID: Int,
                        val CheckinStatusType: CheckinStatusType)

data class CommentType(val ID: Int,
                       val Title: String)

data class User(val ID: Int,
                val IdentityProvider: String,
                val Identifier: String,
                val CurrentSessionToken: String,
                val Username: String,
                val Profile: String,
                val Location: String,
                val WebsiteURL: String,
                val ReputationPoints: Int,
                val Permissions: String,//Possible Error Point
                val PermissionsRequested: String,//
                val DateCreated: String,
                val DateLastLogin: String,
                val IsProfilePublic: Boolean,
                val IsEmergencyChargingProvider: Boolean,
                val Latitude: Float,
                val Longitude: Float,
                val EmailAddress: String,
                val EmailHash: String,
                val ProfileImageURL: String,
                val IsCurrentSessionTokenValid: Boolean,
                val APIKey: String,
                val SyncedSettings: String)

data class CheckinStatusType(val IsPositive: Boolean,
                             val IsAutomatedCheckin: Boolean,
                             val ID: Int,
                             val Title: String)

data class MediaItems(val ID: Int,
                      val ChargePointID: Int,
                      val ItemURL: String,
                      val ItemThumbnailURL: String,
                      val Comment: String,
                      val IsEnabled: Boolean,
                      val IsVideo: Boolean,
                      val IsFeaturedItem: Boolean,
                      val IsExternalResource: Boolean,
                      val MetadataValue: String,
                      val User: User,
                      val DateCreated: String)

data class MetadataValues(val ID: Int,
                          val MetadataFieldID: Int,
                          val ItemValue: String,
                          val MetadataFieldOption: String,
                          val MetadataFieldOptionID: Int)
data class Level(val Comments: String,
                 val IsFastChargeCapable: Boolean,
                 val ID: Int,
                 val Title: String)

data class CurrentType(val Description: String,
                       val ID: Int,
                       val Title: String)