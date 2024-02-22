package com.trifonov.indoor_navigation.common

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.preference.PreferenceManager
import androidx.annotation.RequiresApi
import com.trifonov.indoor_navigation.data.dto.Location
import org.json.JSONObject
import org.json.JSONTokener
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date


@RequiresApi(Build.VERSION_CODES.O)
class LocationData(
    private val cntx: Context?
) {
    var sp: SharedPreferences? = PreferenceManager.getDefaultSharedPreferences(cntx)
    private val locationsList = mutableListOf<Location>()
    init {
        val locationsAsString: String =
            cntx!!.assets.open("locations.json").bufferedReader().use { it.readText() }
        val locationsAsJson = JSONTokener(locationsAsString).nextValue() as JSONObject
        val locationsArray = locationsAsJson.getJSONArray("locations")
        for (i in 0 until locationsArray.length()) {
            val location = locationsArray.getJSONObject(i)
            locationsList.add(
                Location(
                    id = location.getInt("id"),
                    name = location.getString("name"),
                    description = location.getString("description"),
                    address = location.getString("address"),
                    dataUrl = location.getString("dataUrl"),
                    updateTime = java.util.Date
                        .from(LocalDateTime.parse(location.getString("updateTime")).atZone(ZoneId.systemDefault())
                            .toInstant()),
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

    fun getAllLocations(): List<Location>{
        return locationsList
    }

    fun getLocationById(locationId: Int): Location?{
        return locationsList.find { locationId == it.id }
    }
}