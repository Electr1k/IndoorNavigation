package com.trifonov.indoor_navigation.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.MainThread
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.cardview.widget.CardView
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.trifonov.indoor_navigation.R
import com.trifonov.indoor_navigation.adapter.ImagePagerAdapter
import java.lang.Float.max
import kotlin.properties.Delegates

class SelectedPointFragment: CustomFragment() {
    private var currentState: Int = BottomSheetBehavior.STATE_COLLAPSED
    private val peekHeight = 420
    private lateinit var viewPager: ViewPager
    private lateinit var cardView: CardView

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
        val density = requireContext().resources.displayMetrics.density
        mBottomSheetBehavior.peekHeight = peekHeight
        cardView = view.findViewById(R.id.card)
        viewPager = view.findViewById(R.id.imagesPager)
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
        mBottomSheetBehavior.addBottomSheetCallback(
            object : BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState !in listOf(BottomSheetBehavior.STATE_DRAGGING, BottomSheetBehavior.STATE_SETTLING) ) currentState = newState
                    if (newState == BottomSheetBehavior.STATE_EXPANDED){
                        cardView.radius = 0F
                    }
                    else{
                        cardView.radius = 20f * density
                    }
                }
                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    view.findViewById<LinearLayout>(R.id.linearLayout).translationY = max(-1 * 260 * density * (1 - slideOffset), -1 * 260 * density)
                }
            }
        )
        val viewPagerAdapter = ImagePagerAdapter(requireContext(), imageList)
        viewPager.adapter = viewPagerAdapter
        viewPager.currentItem = 1
        var skipTo: Int? = null
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                skipTo = when (position) {
                    0 -> {
                        viewPagerAdapter.count - 2
                    }
                    viewPagerAdapter.count - 1 -> {
                        1
                    }
                    else -> null
                }
            }
            override fun onPageScrollStateChanged(state: Int) {
                if (state == 0 && skipTo != null){
                    viewPager.setCurrentItem(skipTo!!, false)
                    skipTo = null
                }
            }
        })
    }

    override fun onStart() {
        super.onStart()
        mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }
}