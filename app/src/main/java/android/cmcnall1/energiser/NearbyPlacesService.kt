package android.cmcnall1.energiser

import com.google.gson.GsonBuilder
import okhttp3.*
import java.io.IOException

class NearbyPlacesService {
    var listener: DataListener? = null

    private val apiKey = "AIzaSyA2W69fcSdTDZEptQSLI5Q7iKn3E7rljvY"

    fun fetchJson(long: String, lat: String, placeType:String){

        val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=$lat,$long&type=$placeType&rankby=distance&key=$apiKey"

        val request = Request.Builder().url(url).build()

        val client = OkHttpClient()

        client.newCall(request).enqueue(object: Callback {

            override fun onResponse(call: Call, response: Response) {

                val body = response.body()?.string()

                val gson = GsonBuilder().create()

                val result = gson.fromJson(body, PlaceInfo::class.java)

                listener?.onHttpResponseReceived(result)
            }

            override fun onFailure(call: Call, e: IOException) {
                //Print message if an error occurs while fetching information
                println("Failed to execute request")
            }
        })
    }

    interface DataListener {
        fun onHttpResponseReceived(response: PlaceInfo?)
    }
}