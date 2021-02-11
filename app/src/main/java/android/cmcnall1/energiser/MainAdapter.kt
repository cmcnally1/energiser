package android.cmcnall1.energiser

import android.annotation.SuppressLint
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.charge_point_row.view.*

/*
    The Main Adapter takes in an array of charge points and displays them in a recycler view for the user
    to view and interact with.
 */

class MainAdapter(private val chargePoints: Array<ChargePoint?>): RecyclerView.Adapter<CustomViewHolder>() {

    //Count number of items in the charge point array
    override fun getItemCount(): Int {
        return chargePoints.count()
    }

    //Inflate the recycler view rows within the recycler view
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): CustomViewHolder {
        val layoutInflater = LayoutInflater.from(p0.context)
        val cellForRow = layoutInflater.inflate(R.layout.charge_point_row, p0, false)
        return CustomViewHolder(cellForRow)
    }

    //Extract the data to display it
    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: CustomViewHolder, p1: Int) {

        val title = chargePoints[p1]?.AddressInfo?.Title
        val distance = chargePoints[p1]?.AddressInfo?.Distance

        //Displays the title of the charge point
        holder.view.text_title.text = title

        //Converts the distance float into a string with 2 decimal points.
        val distanceText = "%.2f".format(distance) + " Miles away"
        //Displays the distance
        holder.view.text_distance.text = distanceText

        if (chargePoints[p1]?.Connections != null) {
            if (chargePoints[p1]?.checkConnectorType("type 2")!!) {
                holder.view.connectorImageView.setImageResource(R.drawable.connector_green)
            } else {
                holder.view.connectorImageView.setImageResource(R.drawable.connector_red)
            }

            //Displays the number of connections at each charge point
            holder.view.connectorsNumText.text = chargePoints[p1]?.Connections?.size.toString()

        }
        //Assign this charge point to the Custom View Holder so that it can be used should the user
        //select it in the list
        holder.chargePoint = chargePoints[p1]
    }
}

//Custom View Holder
class CustomViewHolder(val view: View, var chargePoint: ChargePoint? = null): RecyclerView.ViewHolder(view){
    //Keys for the data being sent to the detail activity
    companion object{
        const val TITLE_KEY = "TITLE"
        const val ADDRESS_LINE_1_KEY = "ADDRESS_LINE_1"
        const val POSTCODE_KEY = "POSTCODE"
        const val COUNTRY_KEY = "COUNTRY"
        const val DISTANCE_KEY = "DISTANCE"
        const val LATITUDE_KEY = "LATITUDE"
        const val LONGITUDE_KEY = "LONGITUDE"
    }

    init {
        //On click listener to listen for the user to click a charge point in the list
        view.setOnClickListener {

            //Intent object to start the Detail Activity
            val intent = Intent(view.context, DetailActivity::class.java)

            //Add the data to the intent object
            intent.putExtra(TITLE_KEY, chargePoint?.AddressInfo?.Title)
            intent.putExtra(ADDRESS_LINE_1_KEY, chargePoint?.AddressInfo?.AddressLine1)
            intent.putExtra(POSTCODE_KEY, chargePoint?.AddressInfo?.Postcode)
            intent.putExtra(COUNTRY_KEY, chargePoint?.AddressInfo?.Country?.Title)
            intent.putExtra(DISTANCE_KEY, chargePoint?.AddressInfo?.Distance)
            intent.putExtra(LATITUDE_KEY, chargePoint?.AddressInfo?.Latitude)
            intent.putExtra(LONGITUDE_KEY, chargePoint?.AddressInfo?.Longitude)

            //Start the activity with the intent
            view.context.startActivity(intent)
        }
    }
}