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
import com.trifonov.indoor_navigation.common.getTitleStreamProvider
import com.trifonov.indoor_navigation.common.getTitleStreamProviderFromAssets
import com.trifonov.indoor_navigation.common.loadFromString
import com.trifonov.indoor_navigation.mapView.FileHelper.Companion.checkStorageLocation
import com.trifonov.indoor_navigation.mapView.MapConstants.dataPath
import java.io.File

class SplashScreen : Fragment() {

    private lateinit var baseActivity: MainActivity
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        baseActivity = requireActivity() as MainActivity
        Handler(Looper.getMainLooper()).postDelayed({
            val locationData = LocationData(requireContext())
            val currentLocationId = locationData.getCurrentLocation()
            val locName = locationData.getLocationById(currentLocationId)?.dataUrl
            if (currentLocationId != -1 && locName != null && checkStorageLocation(locName)) {
                baseActivity.mapData = loadFromString(
                    zoomLevelCount = (File("${dataPath}${locName}/tiles1").listFiles()?.size ?: 0) - 1,
                    json = File("${dataPath}${locName}/map.json").readText(),
                    applicationContext = requireContext(),
                    getTileStream = getTitleStreamProvider(locName, baseActivity.levelNumber, requireContext())
                )
                baseActivity.mapView.setMap(baseActivity.mapData)
            }
            else {
                // Если нет текущей локации, то ставим дефолтную карту
                locationData.setCurrentLocation(-1)
                baseActivity.streamFromAssets = true
                baseActivity.mapData = loadFromString(
                    zoomLevelCount = activity?.assets!!.list("tiles1")!!.size - 1,
                    json = activity?.assets!!.open("map.json").bufferedReader().use{ it.readText() },
                    applicationContext = requireContext(),
                    getTileStream = getTitleStreamProviderFromAssets(requireActivity() , baseActivity.levelNumber)
                )
                baseActivity.mapView.setMap(
                    baseActivity.mapData
                )
            }
            findNavController().navigate(R.id.action_splash_to_head)
        }, 1500)
        return inflater.inflate(R.layout.splash_screen_fragment, container, false)
    }
}