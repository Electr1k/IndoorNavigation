package com.trifonov.indoor_navigation.common

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.google.gson.Gson
import com.trifonov.indoor_navigation.data.dto.Location
import com.trifonov.indoor_navigation.data.dto.Locations
import com.trifonov.indoor_navigation.map.MapConstants
import java.io.File



class LocationData(
    private val cntx: Context?
) {
    var sp: SharedPreferences? = PreferenceManager.getDefaultSharedPreferences(cntx)
    private var locationsList = mutableListOf<Location>()
    init {
        val fileLocations = File("${MapConstants.dataPath}/locations.json")
        val locations = if (fileLocations.exists()){
            Gson().fromJson(fileLocations.readText(), Locations::class.java)
        }
        else{
            Locations(mutableListOf())
        }
        for (location in locations.locations) {
            locationsList.add(location)
        }
    }
    fun getCurrentLocation(): Int {
        return sp!!.getInt("locationId", -1)
    }
    fun setCurrentLocation(locationId: Int){
        sp!!.edit().putInt("locationId", locationId).apply()
    }

    fun setLocations(locations: List<Location>){
        locationsList = locations.toMutableList()
    }

    fun getAllLocations(): List<Location>{
        return locationsList
    }

    fun getLocationById(locationId: Int): Location?{
        return locationsList.find { locationId == it.id }
    }
}