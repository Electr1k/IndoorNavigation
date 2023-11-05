package com.trifonov.indoor_navigation.common

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager


class LocationData(cntx: Context?) {
    var sp: SharedPreferences? = PreferenceManager.getDefaultSharedPreferences(cntx)

    fun getCurrentLocation(): String? {
        return sp!!.getString("location", null)
    }
    fun setCurrentLocation(location: String){
        sp!!.edit().putString("location", location).apply()
    }
}