package android.cmcnall1.energiser


import com.google.gson.GsonBuilder
import okhttp3.*
import java.io.IOException

/*
    The Charge Point Service handles the requests to query the Open Charge Map API.
    On response from the API, it will parse the received JSON into an array of Charge Point
    Data objects (ChargePoint) that is sent back to the Charge Point Repo via the data listener.
 */

class ChargePointService {

    //Listener that allows the Charge Point Repo to get the received array of Charge Points
    var listener: DataListener? = null

    //Function to fetch and parse the JSON from the api
    fun fetchJson(long: String, lat: String){

        //URL used to query the API, with the requested location used and a max of 30 results returned
        val url = "https://api.openchargemap.io/v2/poi/?output=json&longitude=$long&latitude=$lat&maxresults=60"

        //Creation of an OKHTTP request using the above API URL
        val request = Request.Builder().url(url).build()

        //Creation of an OKHTTP Client
        val client = OkHttpClient()

        //Send out the request via the HTTP client
        client.newCall(request).enqueue(object: Callback {

            //Called if a response is received successfully
            override fun onResponse(call: Call, response: Response) {

                //The raw JSON string received
                val body = response.body()?.string()

                //Creation of a GSON builder to parse the JSON
                val gson = GsonBuilder().create()

                //This result stores the parsed JSON, which is now an array of type ChargePoint
                val result = gson.fromJson(body, Array<ChargePoint?>::class.java)

                //Send the resulting array of Charge Points to the Charge Point Repo via the listener
                listener?.onHttpResponseReceived(result)

            }

            //Called if there is a failure in getting a response from the API
            override fun onFailure(call: Call, e: IOException) {
                //Print message if an error occurs while fetching information
                println("Failed to execute request")
            }
        })
    }

    //Declaration of the listener that will communicate with the Charge Point Repo
    interface DataListener {
        //Declaration of the function that sends a response to the Charge Point Repo
        fun onHttpResponseReceived(response: Array<ChargePoint?>)
    }
}