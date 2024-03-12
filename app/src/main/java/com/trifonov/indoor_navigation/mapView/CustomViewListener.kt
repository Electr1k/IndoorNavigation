package com.trifonov.indoor_navigation.mapView

import android.view.View

interface CustomViewListener {

    fun onLevelChanged(newValue: String)

    fun onTap(view: View, x: Int, y: Int)
}