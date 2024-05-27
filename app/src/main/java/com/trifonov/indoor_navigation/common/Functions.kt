package com.trifonov.indoor_navigation.common

import android.app.Activity
import android.content.Context
import android.widget.Toast
import com.trifonov.indoor_navigation.mapView.MapConstants.unzipPath
import com.trifonov.indoor_navigation.mapView.Dot
import com.trifonov.indoor_navigation.mapView.MapData
import com.trifonov.indoor_navigation.mapView.MapMarker
import org.json.JSONObject
import org.json.JSONTokener
import ovh.plrapps.mapview.core.TileStreamProvider
import java.io.File
import java.io.FileInputStream


fun loadFromString(zoomLevelCount: Int, json: String, applicationContext: Context, getTileStream: TileStreamProvider): MapData {
    val map = JSONTokener(json).nextValue() as JSONObject
    val jsonDots = map.getJSONArray("dots")
    val locationId = map.getInt("locationId")
    val mapWidth = map.getInt("width")
    val mapHeight = map.getInt("height")
    val dotList = mutableListOf<Dot>()
    val levelArray = mutableListOf<String>()
    var i = -1
    while (++i < jsonDots.length()) {
        val jsonDot = jsonDots.getJSONObject(i)
        val dot = Dot(jsonDot.getDouble("x").toFloat(), jsonDot.getDouble("y").toFloat())
        dot.setLevel(jsonDot.getInt("floor"))
        dot.setMac(jsonDot.getString("mac"))
        dot.setName(jsonDot.getString("name"))
        dot.setDescription(jsonDot.getString("description"))
        dot.setType(jsonDot.getString("type"))
        dot.setPhotos(jsonDot.getJSONArray("photoUrls"))
        dot.setId(jsonDot.getInt("id"))
        dot.setConnected(jsonDot.getJSONArray("connected"))
        try {
            dot.setWorkingHours(jsonDot.getJSONArray("working"))
        }catch (_: Exception){}
        if (!levelArray.contains(dot.getLevel().toString())) {
            levelArray.add(dot.getLevel().toString())
        }
        dotList.add(dot)
    }
    levelArray.reverse()
    return  MapData(
        tileStreamProvider = getTileStream,
        levelArray = levelArray as ArrayList<String>,
        fullWidth = mapWidth,
        fullHeight = mapHeight,
        tileSize = 256,
        setMaxScale = 4f,
        markerList = parseFromDot(dotList as ArrayList<Dot>, applicationContext),
        dotList = dotList,
        minPathWidth = 10f,
        maxPathWidth = 50f,
        minScale = 0f,
        maxScale = 2f,
        zoomLevelCount = zoomLevelCount
    )
}

fun parseFromDot(list: ArrayList<Dot>, applicationContext: Context): ArrayList<MapMarker> {
    val mapMarkerList = ArrayList<MapMarker>()

    for (dot in list) {
        mapMarkerList.add(
            MapMarker(
                context = applicationContext, x = dot.getX().toDouble(),
                y = dot.getY().toDouble(), name = dot.getName(), level = dot.getLevel(), dotId = dot.getId()
            )
        )
    }
    return mapMarkerList;
}


fun getTitleStreamProvider(locationName:String, levelNumber: String, context: Context):TileStreamProvider{
    if (!File("${unzipPath}$locationName/tiles${levelNumber}").exists()){
        Toast.makeText(context, "Ошибка при работе с картой", Toast.LENGTH_SHORT).show()
        return TileStreamProvider{ _, _, _ ->
            context.assets.open("tiles1/blank.png")
        }
    }
    return TileStreamProvider{ row, col, zoomLvl ->
        try {
            FileInputStream(
                File(
                    "${unzipPath}/$locationName/",
                    "tiles${levelNumber}/$zoomLvl/$row/$col.jpg"
                )
            )
        }
        catch (e: Exception){
            FileInputStream(File("${unzipPath}/$locationName/", "tiles${levelNumber}/blank.png"))
        }
    }
}


fun getTitleStreamProviderFromAssets(activity: Activity, levelNumber: String):TileStreamProvider{
    return TileStreamProvider{ row, col, zoomLvl ->
        try {
            activity.assets.open("tiles${levelNumber}/$zoomLvl/$row/$col.jpg")
        }
        catch (e: Exception){
            activity.assets.open("tiles${levelNumber}/blank.png")
        }
    }
}