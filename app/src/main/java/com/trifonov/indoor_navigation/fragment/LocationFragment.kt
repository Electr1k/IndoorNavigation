package com.trifonov.indoor_navigation.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.MainThread
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.cardview.widget.CardView
import androidx.core.view.doOnLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.trifonov.indoor_navigation.R
import com.trifonov.indoor_navigation.adapter.LocationAdapter
import com.trifonov.indoor_navigation.common.LocationData
import com.trifonov.indoor_navigation.common.getTitleStreamProvider
import com.trifonov.indoor_navigation.common.loadFromString
import com.trifonov.indoor_navigation.data.dto.Location
import com.trifonov.indoor_navigation.di.ApiModule
import com.trifonov.indoor_navigation.mapView.FileHelper
import com.trifonov.indoor_navigation.mapView.MapConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


class LocationFragment: CustomFragment() {
    private var currentLocation: Location? = null
    private var selectedLocation: Location? = null
    private lateinit var locationRV: RecyclerView
    private lateinit var acceptButton: CardView
    private lateinit var loadingContainer: LinearLayout
    private lateinit var locationData: LocationData
    private lateinit var emptyListText: TextView

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
    @SuppressLint("KotlinNullnessAnnotation", "MissingInflatedId")
    override fun onViewCreated(@NonNull view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        locationRV = view.findViewById(R.id.location_list)
        acceptButton = view.findViewById(R.id.accept_button)
        loadingContainer = view.findViewById(R.id.loading_container)
        emptyListText = view.findViewById(R.id.empty_list_text)
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
                        baseActivity.mapData = loadFromString(
                            zoomLevelCount = (File("${MapConstants.dataPath}${selectedLocation!!.dataUrl}/tiles1").listFiles()?.size ?: 0) - 1,
                            json = File("${MapConstants.dataPath}${selectedLocation!!.dataUrl}/map.json").readText(),
                            applicationContext = requireContext(),
                            getTileStream = getTitleStreamProvider(selectedLocation!!.dataUrl, baseActivity.levelNumber)
                        )
                        requireActivity().runOnUiThread {
                            baseActivity.mapView.setMap(baseActivity.mapData, needDestroy = true)
                            locationData.setCurrentLocation(selectedLocation!!.id)
                            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                        }
                    }.start()
                } else {
                    baseActivity.initialAlertDialog(
                        selectedLocation!!,
                        locationData
                    )
                    mBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                }
            }
        }
        lifecycleScope.launch {
            repeat(3) {
                val item = layoutInflater.inflate(
                    R.layout.loading_location_item,
                    null
                )
                item.findViewById<ShimmerFrameLayout>(R.id.shimmer).startShimmer()
                loadingContainer.addView(
                    item
                )
            }
            var locations: List<Location>? = null
            try {
                locations = withContext(Dispatchers.IO) {
                    ApiModule.provideApi().getLocations().locations
                }
                locationData.setLocations(locations)
                currentLocation = if (currentLocationId != -1) locationData.getLocationById(currentLocationId) else null
                selectedLocation = currentLocation
                locationRV.adapter = LocationAdapter(locations, {selectedLocation = it}, currentLocation, baseActivity)

            } catch (e: Exception) {
                locations = locationData.getAllLocations()
                locationRV.adapter = LocationAdapter(
                    locations,
                    { selectedLocation = it },
                    currentLocation,
                    baseActivity
                )
            } finally {
                loadingContainer.removeAllViews()
                if (locations!!.isEmpty()){
                    emptyListText.visibility = View.VISIBLE
                }
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