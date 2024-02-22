package com.trifonov.indoor_navigation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.trifonov.indoor_navigation.R
import com.trifonov.indoor_navigation.common.LocationEntity
import com.trifonov.indoor_navigation.data.dto.Location

class LocationAdapter(
    private val locationList: List<Location>,
    val click: (Location) -> Unit,
    private val currentLocation: Location?
): RecyclerView.Adapter<LocationAdapter.LocationViewHolder>() {
    private var selectedLocation: Location? = currentLocation

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LocationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.location_item, parent,false)
        return LocationViewHolder(view)
    }
    override fun getItemCount(): Int {
        return locationList.size
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.title.text = locationList[position].name
        holder.address.text = locationList[position].address
        holder.btn.setOnClickListener{
            selectedLocation = locationList[position]
            click(selectedLocation!!)
            holder.btn.isChecked = true
            notifyDataSetChanged()
        }
        holder.itemView.setOnClickListener{
            holder.btn.callOnClick()
        }
        holder.btn.isChecked = locationList[position] == selectedLocation
    }
    class LocationViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val title: TextView
        val address: TextView
        val btn: RadioButton
        init {
            title = itemView.findViewById(R.id.title)
            address = itemView.findViewById(R.id.address)
            btn = itemView.findViewById(R.id.btn)
        }
    }
}