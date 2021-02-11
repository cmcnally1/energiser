package android.cmcnall1.energiser

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.place_item_row.view.*

/*
    The Detail Adapter takes in nearby places and displays them in a recycler view for the user
    to view and interact with
 */

class DetailAdapter(private val nearbyItems: PlaceInfo?): RecyclerView.Adapter<DetailViewHolder>() {

    //Inflate the recycler view items within the recycler view
    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): DetailViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val cellForRow = layoutInflater.inflate(R.layout.place_item_row, parent, false)
        return DetailViewHolder(cellForRow)
    }

    //Count the number of items, if there is no items, count is 0
    override fun getItemCount(): Int {
        return if (nearbyItems?.results != null) {
            nearbyItems.results.count()
        } else
            0
    }

    //Extract the data to show and display it
    override fun onBindViewHolder(holder: DetailViewHolder, position: Int) {
        //Get the name of the nearby place
        val name = nearbyItems?.results?.get(position)?.name
        //Get the icon associated with the nearby place and display it in its image view
        Picasso.get().load(nearbyItems?.results?.get(position)?.icon.toString()).into(holder.view.item_image)
        //Display the nearby place name in its text view
        holder.view.item_name.text = name
    }
}

//View holder class for holding the view
class DetailViewHolder(val view: View): RecyclerView.ViewHolder(view) {}


