package com.trifonov.indoor_navigation.common

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import org.json.JSONObject
import org.json.JSONTokener


class LocationData(
    private val cntx: Context?
) {
    var sp: SharedPreferences? = PreferenceManager.getDefaultSharedPreferences(cntx)
    private val locationsList = mutableListOf<LocationEntity>()
    init {
        val locationsAsString: String =
            cntx!!.assets.open("locations.json").bufferedReader().use { it.readText() }
        val locationsAsJson = JSONTokener(locationsAsString).nextValue() as JSONObject
        val locationsArray = locationsAsJson.getJSONArray("locations")
        for (i in 0 until locationsArray.length()) {
            val location = locationsArray.getJSONObject(i)
            locationsList.add(
                LocationEntity(
                    id = location.getInt("id"),
                    name = location.getString("name"),
                    description = location.getString("description"),
                    address = location.getString("address"),
                    dataUrl = location.getString("dataUrl"),
                    updateTime = location.getString("updateTime"),
                    isVisible = location.getBoolean("isVisible"),
                    hashSum = location.getInt("hashSum")
                )
            )
        }
    }
    fun getCurrentLocation(): Int {
        return sp!!.getInt("locationId", -1)
    }
    fun setCurrentLocation(locationId: Int){
        sp!!.edit().putInt("locationId", locationId).apply()
    }

    fun getAllLocations(): List<LocationEntity>{
        return locationsList
    }

    fun getLocationById(locationId: Int): LocationEntity?{
        return locationsList.find { locationId == it.id }
    }
}