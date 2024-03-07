package com.trifonov.indoor_navigation.fragment

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.annotation.MainThread
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.trifonov.indoor_navigation.MainActivity
import com.trifonov.indoor_navigation.R

open class CustomFragment: Fragment() {
    protected lateinit var mBottomSheet: View
    protected lateinit var mBottomSheetBehavior: BottomSheetBehavior<View>
    protected lateinit var baseActivity: MainActivity
    private lateinit var translateYAnimator: ObjectAnimator
    private lateinit var fragment: View

    @MainThread
    @SuppressLint("KotlinNullnessAnnotation")
    override fun onViewCreated(@NonNull view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        baseActivity = requireActivity() as MainActivity
        fragment = view
        mBottomSheet = view.findViewById(R.id.bottom_sheet)
        mBottomSheetBehavior = BottomSheetBehavior.from(mBottomSheet)
        mBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        mBottomSheetBehavior.isHideable = true
        mBottomSheetBehavior.skipCollapsed = true
        val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN ) {
                    requireActivity().supportFragmentManager.popBackStack()
                }
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        }
        mBottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)
        view.findViewById<ImageView>(R.id.close_btn)?.setOnClickListener{
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            requireActivity().supportFragmentManager.popBackStack()
        }
    }
}