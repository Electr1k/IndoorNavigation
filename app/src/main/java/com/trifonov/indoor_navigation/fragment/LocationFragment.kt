package com.trifonov.indoor_navigation.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.trifonov.indoor_navigation.R
import com.trifonov.indoor_navigation.adapter.AudienceRouteAdapter
import com.trifonov.indoor_navigation.adapter.AudienceTypeAdapter
import com.trifonov.indoor_navigation.adapter.LocationAdapter
import com.trifonov.indoor_navigation.common.LocationData

class LocationFragment: CustomFragment() {
    private lateinit var currentLocation: String
    private lateinit var selectedLocation: String
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
        val locationRV = view.findViewById<RecyclerView>(R.id.location_list)

        val list = listOf(
            "Туалет",
            "Аудитория",
            "Лекционный зал",
            "Кафе",
            "Зона отдыха"
        )
        val locationData = LocationData(requireContext())
        currentLocation = locationData.getCurrentLocation() ?: "Корпус Д"
        selectedLocation = currentLocation
        view.findViewById<CardView>(R.id.accept_button).setOnClickListener{
            locationData.setCurrentLocation(selectedLocation)
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
        locationRV.adapter = LocationAdapter(
            list, {
            selectedLocation = it
            },
            currentLocation)
    }
}