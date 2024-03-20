package com.trifonov.indoor_navigation.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.trifonov.indoor_navigation.MainActivity
import com.trifonov.indoor_navigation.R
import com.trifonov.indoor_navigation.common.LocationData
import com.trifonov.indoor_navigation.data.dto.Location
import com.trifonov.indoor_navigation.mapView.FileHelper.Companion.checkStorageLocation
import com.trifonov.indoor_navigation.mapView.FileHelper.Companion.deleteLocation

class LocationAdapter(
    private val locationList: List<Location>,
    val click: (Location) -> Unit,
    private val currentLocation: Location?,
    private val activity: MainActivity,
    private val onDelete: (Location) -> Unit
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
        holder.deleteIcon.visibility = if (checkStorageLocation(locationList[position].dataUrl)) View.VISIBLE else View.GONE
        holder.deleteIcon.setOnClickListener {
            val ld = LocationData(activity)
            if (ld.getCurrentLocation() != locationList[position].id){
                onDelete(locationList[position])
            }
            else{
                Toast.makeText(activity, "Вы не можете удалить текущую локацию", Toast.LENGTH_SHORT).show()
            }
            notifyDataSetChanged()
        }
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
        val deleteIcon: ImageView
        init {
            title = itemView.findViewById(R.id.title)
            address = itemView.findViewById(R.id.address)
            btn = itemView.findViewById(R.id.btn)
            deleteIcon = itemView.findViewById(R.id.download_icon)
        }
    }
}