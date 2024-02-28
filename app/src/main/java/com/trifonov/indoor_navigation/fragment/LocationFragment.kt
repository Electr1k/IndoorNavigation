package com.trifonov.indoor_navigation.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import androidx.annotation.MainThread
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.cardview.widget.CardView
import androidx.core.view.doOnLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.trifonov.indoor_navigation.MainActivity
import com.trifonov.indoor_navigation.R
import com.trifonov.indoor_navigation.adapter.LocationAdapter
import com.trifonov.indoor_navigation.common.LocationData
import com.trifonov.indoor_navigation.data.dto.Location
import com.trifonov.indoor_navigation.di.ApiModule
import com.trifonov.indoor_navigation.map.FileHelper
import com.trifonov.indoor_navigation.map.MapConstants.mapConnector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class LocationFragment: CustomFragment() {
    private var currentLocation: Location? = null
    private var selectedLocation: Location? = null
    private lateinit var locationRV: RecyclerView
    private lateinit var acceptButton: CardView
    private lateinit var loadingContainer: LinearLayout
    private lateinit var locationData: LocationData

    @Nullable
    @MainThread
    @SuppressLint("KotlinNullnessAnnotation")
    override fun onCreateView(
        @NonNull inflater: LayoutInflater,
        @Nullable container: ViewGroup?,
        @Nullable savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.location_fragment, container, false)
        return view
    }

    @MainThread
    @SuppressLint("KotlinNullnessAnnotation")
    override fun onViewCreated(@NonNull view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        locationRV = view.findViewById(R.id.location_list)
        acceptButton = view.findViewById(R.id.accept_button)
        loadingContainer = view.findViewById(R.id.loading_container)
        mBottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                var width: Int = 0
                acceptButton.doOnLayout {
                    it.measuredWidth
                    width = it.measuredHeight
                }
                acceptButton.translationY = width * (1 - slideOffset)
            }
        })
        locationData = LocationData(requireContext())
        val currentLocationId = locationData.getCurrentLocation()
        currentLocation = if (currentLocationId != -1) locationData.getLocationById(currentLocationId) else null
        selectedLocation = currentLocation
        acceptButton.setOnClickListener{
            if (selectedLocation != null) {
                if (FileHelper.checkStorageLocation(selectedLocation!!.dataUrl)) {
                    Thread {
                        mapConnector.setLocation(selectedLocation!!)
                        requireActivity().runOnUiThread {
                            locationData.setCurrentLocation(selectedLocation!!.id)
                            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                        }
                    }.start()
                } else {
                    (requireActivity() as MainActivity).initialAlertDialog(
                        selectedLocation!!,
                        locationData
                    )
                    mBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                }
            }
        }
        lifecycleScope.launch {
            repeat(3) {
                loadingContainer.addView(
                    layoutInflater.inflate(
                        R.layout.loading_location_item,
                        null
                    )
                )
            }
            try {
                val locations: List<Location> = withContext(Dispatchers.IO) {
                    ApiModule.provideApi().getLocations().locations
                }
                locationData.setLocations(locations)
                currentLocation = if (currentLocationId != -1) locationData.getLocationById(currentLocationId) else null
                selectedLocation = currentLocation
                locationRV.adapter = LocationAdapter(locations, {selectedLocation = it}, currentLocation)

            } catch (e: Exception) {
                locationData.getAllLocations()
                locationRV.adapter = LocationAdapter(
                    locationData.getAllLocations(),
                    { selectedLocation = it },
                    currentLocation
                )
            } finally {
                loadingContainer.removeAllViews()
            }
        }
    }

    override fun onStart() {
        mBottomSheet.visibility = View.VISIBLE
        mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        val slideUpAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
        mBottomSheet.startAnimation(slideUpAnimation)
        super.onStart()
    }
}