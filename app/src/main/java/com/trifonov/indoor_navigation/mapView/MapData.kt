package com.trifonov.indoor_navigation.mapView

import android.util.Log
import ovh.plrapps.mapview.core.TileStreamProvider

data class MapData(
    val levelArray: ArrayList<String>,
    val fullWidth: Int,
    val fullHeight: Int,
    val tileSize: Int,
    val setMaxScale: Float,
    val markerList: ArrayList<MapMarker>,
    val dotList: ArrayList<Dot>,
    val minPathWidth: Float,
    val maxScale: Float,
    val minScale: Float,
    val maxPathWidth: Float,
    var tileStreamProvider: TileStreamProvider,
    val zoomLevelCount: Int
){
    override fun toString(): String {
        return logLevel()
    }

    private fun logLevel() : String{
        for (s in levelArray) {
            Log.d("MyMapDataLog", "levelArray $s")
        }
        for (mapMarker in markerList) {
            Log.d("MyMapDataLog", "markerList $mapMarker ${markerList.size}")
        }
        for (dot in dotList) {
            Log.d("MyMapDataLog", "dotList $dot ${dotList.size}")
        }
        return "Logged"
    }
}
