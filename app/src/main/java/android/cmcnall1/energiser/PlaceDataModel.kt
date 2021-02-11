package android.cmcnall1.energiser

data class PlaceInfo(
    val html_attributions: List<html_attributions>?,
    val next_page_token: String?,
    val results: List<results>?,
    val status: String?
)

data class html_attributions(
    val html_attributions: String? //may cause issues as don't know the structure of this
)

data class results(
    val geometry: geometry?,
    val icon: String?,
    val name: String?,
    val opening_hours: opening_hours?,
    val photos: List<photos>?,
    val place_id: String?,
    val plus_code: plus_code?,
    val rating: Float?,
    val reference: String?,
    val scope: String?,
    val types: List<String>?,
    val user_ratings_total: Int?,
    val vicinity: String?
)

data class geometry(
    val location: location?,
    val viewport: viewport?
)

data class location(
    val lat: Float?,
    val lng: Float?
)

data class viewport(
    val northeast: northeast?,
    val southwest: southwest?
)

data class northeast(
    val lat: Float?,
    val lng: Float?
)

data class southwest(
    val lat: Float?,
    val lng: Float?
)

data class opening_hours(
    val open_now: Boolean?,
    val periods: List<periods>?,
    val weekday_text: List<String>?
)

data class periods(
    val open: open?,
    val close: close?
)

data class open(
    val day: Int?,
    val time: Int?
)

data class close(
    val day: Int?,
    val time: Int?
)

data class photos(
    val height: Int?,
    val html_attributions: List<String>?,
    val photo_reference: String?,
    val width: Int?
)

data class plus_code(
    val compound_code: String?,
    val global_code: String?
)