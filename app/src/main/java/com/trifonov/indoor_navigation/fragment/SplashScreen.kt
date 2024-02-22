package com.trifonov.indoor_navigation.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.trifonov.indoor_navigation.MainActivity
import com.trifonov.indoor_navigation.R
import com.trifonov.indoor_navigation.common.LocationData
import com.trifonov.indoor_navigation.map.FileHelper.Companion.checkStorageLocation
import com.trifonov.indoor_navigation.map.MapConnector
import com.trifonov.indoor_navigation.map.MapConstants
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class SplashScreen : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Handler(Looper.getMainLooper()).postDelayed({
            MapConstants.mapConnector = MapConnector(requireActivity(), findNavController())
            val locationData = LocationData(requireContext())
            val currentLocationId = locationData.getCurrentLocation()
            runBlocking {
                launch {
                    (requireActivity() as MainActivity).checkUpdateLocations()
                }
                if (currentLocationId != -1) {
                    if (checkStorageLocation(locationData.getLocationById(currentLocationId)!!.dataUrl)) {
                        MapConstants.mapConnector.setLocation(
                            locationData.getLocationById(
                                currentLocationId
                            )!!
                        )
                    } else {
                        locationData.setCurrentLocation(-1)
                    };
                }
                findNavController().navigate(R.id.action_splash_to_head)
            }
        }, 1500)
        return inflater.inflate(R.layout.splash_screen_fragment, container, false)
    }
}