package com.trifonov.indoor_navigation.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.annotation.MainThread
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.cardview.widget.CardView
import androidx.core.view.doOnLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.trifonov.indoor_navigation.MainActivity
import com.trifonov.indoor_navigation.R
import com.trifonov.indoor_navigation.adapter.LocationAdapter
import com.trifonov.indoor_navigation.common.LocationData
import com.trifonov.indoor_navigation.common.LocationEntity
import com.trifonov.indoor_navigation.map.FileHelper
import com.trifonov.indoor_navigation.map.MapConstants.mapConnector


class LocationFragment: CustomFragment() {
    private lateinit var currentLocation: LocationEntity
    private lateinit var selectedLocation: LocationEntity
    private lateinit var locationRV: RecyclerView
    private lateinit var acceptButton: CardView

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
        val locationData = LocationData(requireContext())

        currentLocation = locationData.getLocationById(locationData.getCurrentLocation())!!
        selectedLocation = currentLocation
        acceptButton.setOnClickListener{
            try {
                if (FileHelper.checkStorageLocation(selectedLocation.dataUrl)) {
                    Thread {
                        mapConnector.setLocation(selectedLocation)
                        requireActivity().runOnUiThread {
                            locationData.setCurrentLocation(selectedLocation.id)
                            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                        }
                    }.start()
                } else {
                    (requireActivity() as MainActivity).initialAlertDialog(
                        selectedLocation,
                        locationData
                    )
                    mBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                }
            }
            catch (e: Exception){
                Log.e("error", e.message.toString());
            }
        }
        locationRV.adapter = LocationAdapter(locationData.getAllLocations(), {selectedLocation = it}, currentLocation)
    }

    override fun onStart() {
        mBottomSheet.visibility = View.VISIBLE
        mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        val slideUpAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
        mBottomSheet.startAnimation(slideUpAnimation)
        super.onStart()
    }
}