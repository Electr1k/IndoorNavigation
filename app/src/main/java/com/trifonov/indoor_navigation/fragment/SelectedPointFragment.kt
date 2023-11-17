package com.trifonov.indoor_navigation.fragment

import android.annotation.SuppressLint
import android.graphics.Path
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.cardview.widget.CardView
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.trifonov.indoor_navigation.R
import com.trifonov.indoor_navigation.adapter.ImagePagerAdapter

class SelectedPointFragment: CustomFragment() {
    private var currentState: Int = 4

    @Nullable
    @MainThread
    @SuppressLint("KotlinNullnessAnnotation")
    override fun onCreateView(
        @NonNull inflater: LayoutInflater,
        @Nullable container: ViewGroup?,
        @Nullable savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.selected_point_fragment, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBottomSheetBehavior.skipCollapsed = false
        mBottomSheetBehavior.peekHeight = 200
        currentState = 4

        val viewPager = view.findViewById<ViewPager>(R.id.imagesPager)

        val imageList = listOf(
            R.drawable.pager_1,
            R.drawable.pager_2,
            R.drawable.pager_3,
            R.drawable.pager_4,
            R.drawable.pager_5,
            R.drawable.pager_6,
            R.drawable.pager_7,
            R.drawable.pager_8,
            R.drawable.pager_9,
            R.drawable.pager_10,
        )
        println("Start translationY ${viewPager.translationY}")
        super.mBottomSheetBehavior.addBottomSheetCallback(
            object : BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState !in listOf(1,2) ) currentState = newState
                    println("BottomSheetState: $currentState")
                }
                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    println("offset $slideOffset")
                    val k = if (currentState == 4) 1 else -1
                    view.findViewById<CardView>(R.id.cardForVP).translationY = -1 * 400 * (1 - slideOffset)
                }
            }
        )
        val viewPagerAdapter = ImagePagerAdapter(requireContext(), imageList)
        viewPager.adapter = viewPagerAdapter
    }

    override fun onStart() {
        super.onStart()
        mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }
}