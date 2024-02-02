package com.trifonov.indoor_navigation.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.annotation.MainThread
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.trifonov.indoor_navigation.R
import com.trifonov.indoor_navigation.adapter.AudienceSearchAdapter
import com.trifonov.indoor_navigation.map.MapConstants
import com.trifonov.indoor_navigation.map.MapConstants.dotList

class SearchFragment: CustomFragment() {
    @Nullable
    @MainThread
    @SuppressLint("KotlinNullnessAnnotation")
    override fun onCreateView(
        @NonNull inflater: LayoutInflater,
        @Nullable container: ViewGroup?,
        @Nullable savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.search_fragment, container, false)
        return view
    }
    @MainThread
    @SuppressLint("KotlinNullnessAnnotation")
    override fun onViewCreated(@NonNull view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = view.findViewById<RecyclerView>(R.id.list_item)
        val list = dotList.filter { it.getName().isNotEmpty() }
        recyclerView.adapter = AudienceSearchAdapter(list) { dot ->
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            MapConstants.mapConnector.moveCameraToDot(dot)
            val bundle = Bundle()
            bundle.putInt("id", dot.getId())
            findNavController().navigate(R.id.action_search_to_scan, bundle)
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