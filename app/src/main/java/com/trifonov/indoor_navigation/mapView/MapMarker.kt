/**
 * Структура данных маркера для карты
 * @Author Братусев Денис
 * @Since 01.06.2023
 * @Version 1.0
 * */
package com.trifonov.indoor_navigation.mapView

import android.content.Context
import androidx.appcompat.widget.AppCompatImageView

class MapMarker(context: Context, val x: Double, val y: Double, val name: String) :
    AppCompatImageView(context) {

    override fun toString(): String {
        return "$x $y $name"
    }
}


