package android.cmcnall1.energiser

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class MyCarFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_my_car, container, false)
        val recyclerViewMyCar: RecyclerView = view.findViewById(R.id.recyclerView_myCar)

        val myRR = MyCarInfo(
            "Car 1",
            resources.getDrawable(R.drawable.rr),//possible depreciation issue
            true,
            13.34f,
            90.0f,
            56.75f,
            100.0f,
            2345.0f,
            14.46f,
            3.43f,
            "04/3/19",
            "10 minutes ago"
        )

        val myRRV = MyCarInfo(
            "Car 2",
            resources.getDrawable(R.drawable.rrv),//possible depreciation issue
            true,
            17.56f,
            88.5f,
            64.43f,
            132.34f,
            2567.90f,
            15.48f,
            4.56f,
            "05/2/19",
            "5 hours ago"
        )

        val myCars = arrayListOf(myRR,myRRV)

        //Text Views to display info
        val myCarTitle: TextView = view.findViewById(R.id.myCarTitle)
        val chargeRemainTV: TextView = view.findViewById(R.id.chargeRemainTextView)
        val rangeTV: TextView = view.findViewById(R.id.mRangeTextView)
        val kwhTotalConsTV: TextView = view.findViewById(R.id.consumptionText)
        val avConsTV: TextView = view.findViewById(R.id.avConsTextView)
        val mileageTV: TextView = view.findViewById(R.id.myMileageTextView)
        val dateTV: TextView = view.findViewById(R.id.myDateTextView)
        val timeTV: TextView = view.findViewById(R.id.myTimeTextView)

        //Set the data so that the info boxes show the info of the first Car in the Array
        myCarTitle.text = myCars[0].carModel.toString()
        chargeRemainTV.text = myCars[0].chargeRemaining.toString()
        rangeTV.text = myCars[0].milesRemaining.toString()
        kwhTotalConsTV.text = myCars[0].kWhTotalConsumption.toString()
        avConsTV.text = myCars[0].kWhAverageConsumption.toString()
        mileageTV.text = myCars[0].mileage.toString()
        dateTV.text = myCars[0].dateLastCharge.toString()
        timeTV.text = myCars[0].hoursLastCharge.toString()

        //Set the info boxes to the car selected from the slider
        recyclerViewMyCar.layoutManager = SliderLayoutManager(activity).apply {
            callback = object : SliderLayoutManager.OnItemSelectedListener {
                override fun onItemSelected(layoutPosition: Int) {
                    myCarTitle.text = myCars[layoutPosition].carModel.toString()
                    chargeRemainTV.text = myCars[layoutPosition].chargeRemaining.toString()
                    rangeTV.text = myCars[layoutPosition].milesRemaining.toString()
                    kwhTotalConsTV.text = myCars[layoutPosition].kWhTotalConsumption.toString()
                    avConsTV.text = myCars[layoutPosition].kWhAverageConsumption.toString()
                    mileageTV.text = myCars[layoutPosition].mileage.toString()
                    dateTV.text = myCars[layoutPosition].dateLastCharge.toString()
                    timeTV.text = myCars[layoutPosition].hoursLastCharge.toString()
                }
            }
        }

        recyclerViewMyCar.adapter = MyCarAdapter().apply {
            setData(myCars)
            callback = object: MyCarAdapter.Callback {
                override fun onItemClicked(view: View) {
                    //Reset the My Car scroller to the first car
                    recyclerViewMyCar.scrollToPosition(0)
                }
            }
        }
        return view
    }

    companion object {
        fun newInstance(): MyCarFragment = MyCarFragment()
    }
}