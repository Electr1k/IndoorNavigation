package com.trifonov.indoor_navigation.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.trifonov.indoor_navigation.R
import com.trifonov.indoor_navigation.common.LocationData

class LocationAdapter(
    private val locationList: List<String>,
    val click: (String) -> Unit,
    private val currentLocation: String
): RecyclerView.Adapter<LocationAdapter.LocationViewHolder>() {
    private var selectedLocation: String? = null

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
        holder.title.text = locationList[position]
        holder.address.text = locationList[position]
        holder.itemView.setOnClickListener{
            selectedLocation = locationList[position]
            click(selectedLocation!!)
            println("Click position $position")
            holder.btn.isChecked = true
            notifyDataSetChanged()
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