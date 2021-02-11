package android.cmcnall1.energiser

import com.google.android.apps.auto.sdk.MenuAdapter
import com.google.android.apps.auto.sdk.MenuItem

class AutoListMenuAdapter(private val menuCallbacks: MenuCallbacks,
                          private val chargePoints: Array<ChargePoint?>): MenuAdapter(){

    private val menuItems: ArrayList<MenuItem> = arrayListOf()
    private val currentChargePoints: ArrayList<ChargePoint> = arrayListOf()


    fun updateChargePointList() {
        menuItems.clear()
        currentChargePoints.clear()

        for(chargePoint in chargePoints){
            currentChargePoints.add(chargePoint!!)
        }

        for(currentChargePoint in currentChargePoints){
            val subtitle: String
            val type2Icon: Int
            if(currentChargePoint.AddressInfo?.Distance != null){
                subtitle = "%.2f".format(currentChargePoint.AddressInfo?.Distance) + " miles away"

            } else {
                subtitle = "Distance Unknown"
            }

            if (currentChargePoint.checkConnectorType("type 2")){
                type2Icon = R.drawable.connector_green
            } else {
                type2Icon = R.drawable.connector_red
            }

            menuItems.add(MenuItem.Builder()
                .setTitle(currentChargePoint.AddressInfo?.Title.toString())
                .setSubtitle(subtitle)
                .setIconResId(type2Icon)
                .build())
        }

        notifyDataSetChanged()
    }

    override fun getMenuItem(position: Int): MenuItem {
        return menuItems[position]
    }

    override fun getMenuItemCount(): Int {
        return menuItems.size
    }

    override fun onMenuItemClicked(position: Int) {
        val selectedChargePoint = currentChargePoints[position]
        menuCallbacks.onChargePointSelected(selectedChargePoint)
    }

    interface MenuCallbacks{

        //Called when a user selects a charge point from the list
        fun onChargePointSelected(chargePoint: ChargePoint)

    }


}