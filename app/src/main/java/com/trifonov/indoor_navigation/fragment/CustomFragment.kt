package com.trifonov.indoor_navigation.fragment

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.annotation.MainThread
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.trifonov.indoor_navigation.R

open class CustomFragment: Fragment() {
    private lateinit var mBottomSheet: View
    protected lateinit var mBottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var translateYAnimator: ObjectAnimator

    @MainThread
    @SuppressLint("KotlinNullnessAnnotation")
    override fun onViewCreated(@NonNull view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBottomSheet = view.findViewById(R.id.bottom_sheet)
        mBottomSheet.animation = onCreateAnimation(500, true, 10)
        mBottomSheetBehavior = BottomSheetBehavior.from(mBottomSheet)
        mBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        mBottomSheetBehavior.isHideable = true
        mBottomSheetBehavior.skipCollapsed = true
//// Получаем высоту экрана в пикселях
//        val screenHeight = resources.displayMetrics.heightPixels
//
//// Создаем кастомный аниматор для трансляции Bottom Sheet вверх снизу вверх
//        translateYAnimator = ObjectAnimator.ofFloat(mBottomSheet, "translationY", screenHeight.toFloat())
//        translateYAnimator.duration = 500 // Длительность анимации (в миллисекундах) - установите нужное вам значение


        val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    activity?.supportFragmentManager?.popBackStack()
                }
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        }
        mBottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)
        view.findViewById<ImageView>(R.id.close_btn).setOnClickListener{
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    override fun onStart() {
        super.onStart()

//        translateYAnimator.start() // Запускаем анимацию трансляции
        mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }
}