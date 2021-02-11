package android.cmcnall1.energiser

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_auto_details.*


class AutoDetailFragment: Fragment() {

    private var contentView: View? = null
    private var nameText: TextView? = null
    private var address1Text: TextView? = null
    private var distanceText: TextView? = null
    private var connectorsNumText: TextView? = null

    private var connectorIcon: ImageView? = null

    private var chargePoint: ChargePoint? = null
    private var chargePointListener: ChargePointListener? = null

    private var autoGoThereButton: Button? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        contentView = layoutInflater.inflate(R.layout.fragment_auto_details, container, false)

        nameText = contentView!!.findViewById(R.id.chargePointName) as TextView
        address1Text = contentView!!.findViewById(R.id.chargePointAddress1) as TextView
        distanceText = contentView!!.findViewById(R.id.chargePointDistance) as TextView
        connectorsNumText = contentView!!.findViewById(R.id.connectorsNumText) as TextView

        connectorIcon = contentView!!.findViewById(R.id.connectorIcon) as ImageView

        autoGoThereButton = contentView!!.findViewById(R.id.autoGoThereButton)

        val closeButton = contentView!!.findViewById(R.id.details_close_button) as ImageView
        closeButton.setOnClickListener {
            if (chargePointListener != null){
                chargePointListener!!.closeDetailsFragment()
            }

        }

        if (chargePoint != null) {
            updateContent()
        }

        return contentView
    }

    fun setChargePoint(myChargePoint: ChargePoint){
        chargePoint = myChargePoint

        if (contentView != null){
            updateContent()
        }
    }

    private fun updateContent() {
        //Do nothing if no charge point has been set or if the view has not been created
        if (chargePoint == null || contentView == null){
            return
        }

        val selectedChargePoint = chargePoint
        val distance = selectedChargePoint?.AddressInfo?.Distance

        nameText!!.text = selectedChargePoint?.AddressInfo?.Title
        address1Text!!.text = selectedChargePoint?.AddressInfo?.AddressLine1
        distanceText!!.text = "%.2f".format(distance) + " miles away"

        if(selectedChargePoint?.checkConnectorType("type 2")!!) {
            connectorIcon?.setImageResource(R.drawable.connector_green)
        } else {
            connectorIcon?.setImageResource(R.drawable.connector_red)
        }

        connectorsNumText?.text = selectedChargePoint?.Connections?.size.toString()

        //Creation of an Intent object that will pass the charge points latitude and longitude
        //out to the google maps application to allow turn-by-turn navigation to the charge point
        var gmIntentUri: Uri = Uri.parse("google.navigation:q=${chargePoint?.AddressInfo?.Latitude},${chargePoint?.AddressInfo?.Longitude}")
        var mapIntent = Intent(Intent.ACTION_VIEW, gmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")

        //Implementation of the button that will activate the turn-by-turn navigation through google maps
        autoGoThereButton?.setOnClickListener {
            startActivity(mapIntent)
        }
    }

    fun setChargePointListener(myChargePointListener: ChargePointListener){
        chargePointListener = myChargePointListener
    }

    interface ChargePointListener{
        fun navigateTo(chargePoint: ChargePoint)
        fun closeDetailsFragment()
    }
}