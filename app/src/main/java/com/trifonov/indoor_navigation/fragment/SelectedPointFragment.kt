package com.trifonov.indoor_navigation.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.trifonov.indoor_navigation.R

class SelectedPointFragment: CustomFragment() {

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
}