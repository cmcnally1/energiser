package android.cmcnall1.energiser

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.ToggleButton

class AutoFiltersFragment: Fragment() {

    private var contentView: View? = null

    private var chargePoints: Array<ChargePoint?> = arrayOf()
    private var filterListener: FilterListener? = null

    private lateinit var closeButton: Button
    private lateinit var resetButton: Button

    private lateinit var showAllToggleButton: ToggleButton
    private lateinit var type2ToggleButton: ToggleButton
    private lateinit var rangeToggleButton: ToggleButton

    private lateinit var priceSeekBar: SeekBar
    private lateinit var connectorsSeekBar: SeekBar

    private lateinit var priceTextView: TextView
    private lateinit var connectorTextView: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        contentView = layoutInflater.inflate(R.layout.fragment_auto_filters, container, false)

        closeButton = contentView!!.findViewById(R.id.cancelFilterButton)
        resetButton = contentView!!.findViewById(R.id.resetButton)

        showAllToggleButton = contentView!!.findViewById(R.id.showAllToggle)
        type2ToggleButton = contentView!!.findViewById(R.id.type2Toggle)
        rangeToggleButton = contentView!!.findViewById(R.id.withinRangeToggle)

        priceSeekBar = contentView!!.findViewById(R.id.priceSeekbar)
        connectorsSeekBar = contentView!!.findViewById(R.id.connectorsSeekbar)

        priceTextView = contentView!!.findViewById(R.id.showPrice)
        connectorTextView = contentView!!.findViewById(R.id.showConnectors)

        setUpFilter()


        closeButton.setOnClickListener {
            if (filterListener != null) {
                filterListener!!.closeFiltersFragment()
            }

        }

        return contentView

    }

    fun setFilterListener(myFilterListener: FilterListener){
        filterListener = myFilterListener
    }

    private fun setUpFilter() {
        //Max price SeekBar implementation
        priceSeekBar.incrementProgressBy(10)
        priceSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            var progressChangedValue: Float = 0.00F

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                progressChangedValue = progress.toFloat() / 100

                when (progressChangedValue) {
                    0.00F -> priceTextView.setText("Free")
                    10.00F -> priceTextView.setText("Any")
                    else -> priceTextView.setText("£" + "%.2f".format(progressChangedValue))
                }
                filterListener?.returnFilters(
                    type2ToggleButton.isChecked,
                    rangeToggleButton.isChecked,
                    priceSeekBar.progress.toString(),
                    connectorTextView.text.toString()
                )

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        //Number of connectors SeekBar implementation
        connectorsSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            var progressChangedValue = 0

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                progressChangedValue = progress

                if (progressChangedValue == 0) {
                    connectorTextView.setText("Any")
                } else {
                    connectorTextView.setText(progressChangedValue.toString())
                }
                filterListener?.returnFilters(
                    type2ToggleButton.isChecked,
                    rangeToggleButton.isChecked,
                    priceSeekBar.progress.toString(),
                    connectorTextView.text.toString()
                )
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        //Toggle Button Listeners
        showAllToggleButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                rangeToggleButton.setChecked(false)
                type2ToggleButton.setChecked(false)
            }
            filterListener?.returnFilters(
                    type2ToggleButton.isChecked,
                    rangeToggleButton.isChecked,
                    priceSeekBar.progress.toString(),
                    connectorTextView.text.toString()
                )
        }

        type2ToggleButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showAllToggleButton.setChecked(false)
            }
            filterListener?.returnFilters(
                    type2ToggleButton.isChecked,
                    rangeToggleButton.isChecked,
                    priceSeekBar.progress.toString(),
                    connectorTextView.text.toString()
                )
        }

        rangeToggleButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showAllToggleButton.setChecked(false)
            }
            filterListener?.returnFilters(
                    type2ToggleButton.isChecked,
                    rangeToggleButton.isChecked,
                    priceSeekBar.progress.toString(),
                    connectorTextView.text.toString()
                )
        }

        resetButton.setOnClickListener {
            connectorsSeekBar.progress = 0
            priceSeekBar.progress = 1000
            showAllToggleButton.isChecked = true

            filterListener?.returnFilters(
                    type2ToggleButton.isChecked,
                    rangeToggleButton.isChecked,
                    priceSeekBar.progress.toString(),
                    connectorTextView.text.toString()
                )
        }

    }

    interface FilterListener {
        fun closeFiltersFragment()
        fun returnFilters(type2: Boolean, withinRange: Boolean, maxPrice: String, numConnectors: String)
    }
}