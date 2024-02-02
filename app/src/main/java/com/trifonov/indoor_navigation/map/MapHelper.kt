/**
 * Вспомогательный класс для работы с MapView
 * @Author Братусев Денис
 * @Since 01.06.2023
 * @Version 1.0
 * */
package com.trifonov.indoor_navigation.map

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.trifonov.indoor_navigation.R
import com.trifonov.indoor_navigation.map.MapConstants.cameraMarkerX
import com.trifonov.indoor_navigation.map.MapConstants.cameraMarkerY
import com.trifonov.indoor_navigation.map.MapConstants.cameraRotation
import com.trifonov.indoor_navigation.map.MapConstants.cameraZoom
import com.trifonov.indoor_navigation.map.MapConstants.dotList
import com.trifonov.indoor_navigation.map.MapConstants.finishNode
import com.trifonov.indoor_navigation.map.MapConstants.levelNumber
import com.trifonov.indoor_navigation.map.MapConstants.mapHeight
import com.trifonov.indoor_navigation.map.MapConstants.mapWidth
import com.trifonov.indoor_navigation.map.MapConstants.markerList
import com.trifonov.indoor_navigation.map.MapConstants.maxPathWidth
import com.trifonov.indoor_navigation.map.MapConstants.maxScale
import com.trifonov.indoor_navigation.map.MapConstants.minPathWidth
import com.trifonov.indoor_navigation.map.MapConstants.minScale
import com.trifonov.indoor_navigation.map.MapConstants.startNode
import com.trifonov.indoor_navigation.map.MapConstants.unzipPath
import com.trifonov.indoor_navigation.map.MapConstants.zoomLevelCount
import ovh.plrapps.mapview.MapView
import ovh.plrapps.mapview.MapViewConfiguration
import ovh.plrapps.mapview.ReferentialData
import ovh.plrapps.mapview.ReferentialListener
import ovh.plrapps.mapview.api.addCallout
import ovh.plrapps.mapview.api.addMarker
import ovh.plrapps.mapview.api.moveMarker
import ovh.plrapps.mapview.api.moveToMarker
import ovh.plrapps.mapview.api.removeMarker
import ovh.plrapps.mapview.api.setAngle
import ovh.plrapps.mapview.api.setMarkerTapListener
import ovh.plrapps.mapview.core.TileStreamProvider
import ovh.plrapps.mapview.markers.MarkerTapListener
import ovh.plrapps.mapview.paths.PathView
import ovh.plrapps.mapview.paths.addPathView
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import kotlin.math.atan

/**
 * @Constructor Create empty Map helper
 * @Param [activity] контекст для работы с ресурсами
 * @Param [mapView] MapView для конфигурации карты
 * @Param [locationName] название локации
 * @Param [navigation] навигатор для поиска маршрута
 */
class MapHelper(
    private val activity: Activity,
    private val mapView: MapView,
    private val locationName: String,
    private val navigation: Navigation,
    private val navController: NavController,
    private val isFromAssets: Boolean = false
) : TileStreamProvider {

    /** @Param [finishMarker] – маркер конца маршрута на карте */
    private val finishMarker = AppCompatImageView(activity).apply {
        setImageResource(R.drawable.finish_marker)
    }

    /** @Param [positionMarker] – маркер текущего положения пользователя на карте */
    private val positionMarker = AppCompatImageView(activity).apply {
        setImageResource(R.drawable.position_marker)
    }

    /** @Param [positionMarker] – маркер текущего положения пользователя на карте */
    private val centerMarker = AppCompatImageView(activity).apply {
        setImageResource(R.drawable.position_marker)
    }

    /** @Param [pathView] – конструктор маршрута для пользователя */
    private lateinit var pathView: PathView

    /** @Param [newScale] – текущее приблежение карты */
    private var newScale = 0f

    private var isPathSet = false

    private var positionRotation = 0f

    /**
     * Метод для первичной настройки MapView
     * @See [MapHelper.generateConfig]
     * */
    init {
        markerList.clear()
        mapView.configure(generateConfig(zoomLevelCount, mapWidth, mapHeight))
        mapView.defineBounds(0.0, 0.0, mapWidth.toDouble(), mapHeight.toDouble())
    }

    /**
     * Функция установки метки конца маршрута
     * @Param [id] - уникальный идентификатор точки на графе
     * */
    internal fun addFinishMarker(id: String) {
        var x = 0.0
        var y = 0.0
        for (marker in markerList) {
            if (marker.name == id) {
                x = marker.x
                y = marker.y
                break
            }
        }
        try {
            mapView.removeMarker(finishMarker)
        } catch (e: Exception) {
        }
        if (!isPathSet) finishMarker.visibility = View.INVISIBLE
        mapView.addMarker(finishMarker, x, y, -0.5f, -0.5f)
    }

    /**
     * Функция установки метки текущего положения
     * @Param [id] - уникальный идентификатор точки на графе
     * */
    internal fun addPositionMarker(id: String, angel: Float = 0f) {
        var x = 0.0
        var y = 0.0
        for (marker in markerList) {
            if (marker.name == id) {
                x = marker.x
                y = marker.y
                break
            }
        }
        try {
            mapView.removeMarker(positionMarker)
        } catch (e: Exception) {
        }
        positionMarker.rotation = angel
        positionMarker.visibility = View.INVISIBLE
        mapView.addMarker(positionMarker, x, y, -0.5f, -0.5f)
    }


    /**
     * Функция установки метки текущего положения
     * @Param [id] - уникальный идентификатор точки на графе
     * */
    internal fun addCenterScreenMarker() {
        centerMarker.visibility = View.INVISIBLE
        mapView.addMarker(centerMarker, 0.0, 0.0, -0.5f, -0.5f)
        moveCamera(cameraMarkerX, cameraMarkerY, cameraZoom.toFloat())
    }

    /**
     * Функция установки стандартой метки на карту
     * @Param [x] - координата по горизонтали
     * @Param [y] - координата по вертикали
     * @Param [level] - номер отображаемого этажа
     * @Param [name] - описание маркера. В данной реализации его идентификатор
     * @Param [angel] - угол поворота маркера
     * */
    private fun addDefaultMarker(
        x: Double,
        y: Double,
        level: Int,
        id: String,
        name: String,
        angel: Float = 0f
    ) {
        val marker = MapMarker(activity, x, y, id).apply {
            setImageDrawable(BitmapDrawable(resources, drawText("$name ")))
        }
        marker.rotation = angel
        markerList.add(marker)
        if (levelNumber == level) mapView.addMarker(marker, x, y)
    }

    /**
     * Функция отрисовки текста на карте
     * @Param [text] - отображаемый текст
     * @Param [textColor] - цвет отображаемого текста
     * @Param [textSize] - размер шрифта текста
     * @Param [typeface] - семейство шрифтов
     * @Param [style] - стиль шрифта
     * @Param [isUnderline] - наличие подчеркивания
     * @Return возвращает карту с отрисованным на ней текстом
     * */
    private fun drawText(
        text: String = "Г-320",
        textColor: Int = Color.WHITE,
        textSize: Float = 18F * 2,
        typeface: Typeface = Typeface.SERIF,
        style: Int = Typeface.BOLD,
        isUnderline: Boolean = false,
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(
            (textSize.toInt() * text.length / 1.6).toInt(),
            textSize.toInt(),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap).apply {}

        val paint = Paint().apply {
            isAntiAlias = true
            color = textColor
            this.textSize = textSize
            this.typeface = typeface
            setTypeface(Typeface.create(typeface, style))
            if (isUnderline) {
                flags = Paint.UNDERLINE_TEXT_FLAG
            }
        }
        canvas.drawText(text, 10F, canvas.height.toFloat(), paint)
        return bitmap
    }

    /**
     * Функция генерации базовой конфигурации MapView
     * @Param [levelCount] - количество уровней приближения
     * @Param [fullWidth] - фактическая ширина карты
     * @Param [fullHeight] - фактическая высота карты
     * */
    private fun generateConfig(
        levelCount: Int,
        fullWidth: Int,
        fullHeight: Int
    ): MapViewConfiguration {
        pathView = PathView(activity)
        return MapViewConfiguration(levelCount, fullWidth, fullHeight, 256, this).setMaxScale(
            maxScale
        )
            .enableRotation().setStartScale(minScale)
    }

    /**
     * Метод для подгрузки тайлов карты к MapView
     * */
    @SuppressLint("SdCardPath", "SuspiciousIndentation")
    override fun getTileStream(row: Int, col: Int, zoomLvl: Int): InputStream? {
        return try {
            if (isFromAssets) {
                try {
                    activity.assets.open("tiles$levelNumber/$zoomLvl/$row/$col.jpg")
                }
                catch (e: Exception){
                    activity.assets.open("tiles$levelNumber/blank.png")
                }
            }
            else
            FileInputStream(
                File(
                    "$unzipPath/$locationName/",
                    "tiles$levelNumber/$zoomLvl/$row/$col.jpg"
                )
            )
        } catch (e: Exception) {
            FileInputStream(File("$unzipPath/$locationName/", "tiles$levelNumber/blank.png"))
        }
    }

    /**
     * @Param [refOwner] – обработчик вращения и изменения размера карты
     * */
    private val refOwner = object : ReferentialListener {
        var referentialData: ReferentialData? = null

        override fun onReferentialChanged(refData: ReferentialData) {
            setMarkerScale(refData.scale)
            setPositionMarkerRotation(refData.angle)
            referentialData = refData
            for (mapMarker in markerList) {
                setMarkerScale(refData.scale, mapMarker)
            }
            newScale = refData.scale
            updatePath()
        }
    }

    internal fun updateCameraData(){
        cameraZoom = refOwner.referentialData!!.scale.toDouble()
        cameraRotation = refOwner.referentialData!!.angle
        cameraMarkerX = refOwner.referentialData!!.centerX * mapWidth
        cameraMarkerY = refOwner.referentialData!!.centerY * mapHeight
    }

    /**
     * Метод перемещения камеры на указанные координаты
     * */
    internal fun moveCamera(x: Double, y: Double, scale: Float, shouldAnimate: Boolean = false) {
        mapView.moveMarker(centerMarker, x, y)
        mapView.setScaleFromCenter(scale)
        mapView.moveToMarker(centerMarker, shouldAnimate)
        Log.d("MyLog", "$cameraRotation")
    }

    /**
     * Метод устанавливает приближение маркера метоположения
     * @Param [scale] уровень приближения
     * */
    private fun setMarkerScale(scale: Float) {
        positionMarker.scaleX = scale + 1f
        positionMarker.scaleY = scale + 1f
        finishMarker.scaleX = scale + 1f
        finishMarker.scaleY = scale + 1f
    }

    private fun setPositionMarkerRotation(angle: Float) {
        positionMarker.rotation = angle + positionRotation
    }

    /**
     * Метод устанавливает приближение маркера местоположения
     * @Param [scale] уровень приближения маркера
     * @Param [mapMarker] настраиваемый маркер
     * */
    private fun setMarkerScale(scale: Float, mapMarker: MapMarker) {
        val mapMax = 2.0
        val mapMin = 0.3
        val markerMin = 0
        val markerMax = 3
        var tmp =
            (((scale - mapMin) / (mapMax - mapMin)) * (markerMax - markerMin) + markerMin).toFloat()
        tmp = if (tmp > 0) tmp else 0.0F
        mapMarker.scaleX = tmp
        mapMarker.scaleY = tmp
    }

    /**
     * Метод для размещения слушателя [refOwner]
     * @See [MapHelper.refOwner]
     */
    internal fun addReferentialListener() {
        mapView.addReferentialListener(refOwner)
    }

    /**
     * Метод для добавления маршрута [pathView] на [mapView]
     * */
    private fun addPathView() {
        isPathSet = true
        mapView.addPathView(pathView)
    }

    /**
     * Метод для установки обработчика нажатий на маркер
     * @See [MarkerCallout]
     */
    internal fun addMarkerClickListener() {
        mapView.setMarkerTapListener(object : MarkerTapListener {
            override fun onMarkerTap(view: View, x: Int, y: Int) {
                if (view is MapMarker) {
                    val dot = dotList.find { view.name.toInt() == it.getId() }
                    if (dot?.getName() != "") {
                        val callout = MarkerCallout(activity, dot, navController)
                        callout.setTitle(dot?.getName() ?: view.name)
                        callout.setSubTitle("position: ${view.x} , ${view.y}")
                        mapView.addCallout(callout, view.x, view.y, -0.5f, -1.2f, 0f, 0f)
                        callout.transitionIn()
                    }
                }
            }
        })
    }

    /**
     * Устанавливает уровень увеличения
     * @Param [scale] значение уровня приближения
     */
    internal fun setScale(scale: Float) {
        mapView.scale = scale
    }

    /**
     * Возвращает уровень текущего приближения карты
     * @Return текущий уровень приближения
     */
    internal fun getScale(): Float {
        return mapView.scale
    }

    /**
     *  @Param [strokePaint] настройка кисти для рисования маршрута на карте
     * */
    private val strokePaint = Paint().apply {
        color = ContextCompat.getColor(activity, R.color.brand)
        strokeCap = Paint.Cap.ROUND
    }

    internal fun movePosition(id: String) {
        var x = 0.0
        var y = 0.0
        for (marker in markerList) {
            if (marker.name == id) {
                x = marker.x
                y = marker.y
                break
            }
        }
        mapView.moveMarker(positionMarker, x, y)
        setPositionMarkerRotation(0f)
    }

    /**
     * Метод для построения маршрута на карте
     * @See [Navigation]
     */
    internal fun updatePath() {
        var myMarker = Map.Dot(0f, 0f)
        var myFinishMarker = Map.Dot(0f, 0f)
        for (marker in dotList) {
            if (marker.getId() == startNode) myMarker = marker
            if (marker.getId() == finishNode) myFinishMarker = marker
        }
        changeMarkerVisibility(myMarker, myFinishMarker)

        val drawablePath = object : PathView.DrawablePath {
            override val visible: Boolean = true
            override var path: FloatArray = calculatePath()
            override var paint: Paint? = strokePaint
            override val width: Float = calculatePathWidth()
        }

        pathView.updatePaths(listOf(drawablePath))
        if (!isPathSet) addPathView()
    }

    private fun changeMarkerVisibility(myMarker: Map.Dot, myFinishMarker: Map.Dot) {
        if (levelNumber == myMarker.getLevel()) positionMarker.visibility = View.VISIBLE
        if (levelNumber == myFinishMarker.getLevel()) {
            addFinishMarker(finishNode.toString())
            finishMarker.visibility = View.VISIBLE
        } else finishMarker.visibility = View.INVISIBLE
    }

    /**
     * Метод для подсчёта угла поворота маркера положения пользователя
     * @Param [x1] X координата пользователя
     * @Param [y1] Y координата пользователя
     * @Param [x2] X координата следующей точки по маршруту
     * @Param [y2] Y координата следующей точки по маршруту
     * */
    private fun calculateAngel(x1: Int, y1: Int, x2: Int, y2: Int): Float {
        var angel = 90.0f
        val tmp = (atan(((y2 - y1).toDouble() / (x2 - x1).toDouble())) * 180 / Math.PI).toFloat()
        angel += if (x2 - x1 >= 0) tmp
        else tmp + 180
        return angel
    }

    private fun calculatePath(): FloatArray {
        val finish = if(finishMarker.visibility == View.INVISIBLE) dotList[dotList.size-3].getId() else finishNode
        val start = if(positionMarker.visibility == View.INVISIBLE) dotList[dotList.size-2].getId() else startNode
        val myPath = navigation.path(start, finish, levelNumber)
        if (myPath?.size!! > 3) {
            val x1 = myPath[0].toInt()
            val y1 = myPath[1].toInt()
            val x2 = myPath[2].toInt()
            val y2 = myPath[3].toInt()
            positionRotation = calculateAngel(x1, y1, x2, y2)
        }
        return myPath
    }

    private fun calculatePathWidth() : Float{
        var temp = minPathWidth + (maxPathWidth - minPathWidth) * newScale / maxScale
        if (newScale == minScale) temp = minPathWidth
        else if (newScale == maxScale) temp = maxPathWidth
        return temp
    }

    /**
     * Метод для добавления всех маркеров на карту
     * @Param [dotList] массив маркеров для карты
     * @See [Map.Dot]
     */
    internal fun addAllMarkers(dotList: java.util.ArrayList<Map.Dot>) {
        try {
            for (dot in dotList) {
                addDefaultMarker(
                    dot.getX().toDouble(),
                    dot.getY().toDouble(),
                    dot.getLevel(),
                    dot.getId().toString(),
                    dot.getName()
                )
            }
        } catch (e: Exception) {
        }
    }

    /**
     * Метод для увеличения карты кнопкой
     */
    internal fun zoomIn() {
        newScale += (maxScale - minScale) / zoomLevelCount
        if (newScale > maxScale) newScale = maxScale
        mapView.smoothScaleFromFocalPoint(mapView.width / 2, mapView.height / 2, newScale)
    }

    /**
     * Метод для уменьшения карты кнопкой
     */
    internal fun zoomOut() {
        newScale -= (maxScale - minScale) / zoomLevelCount
        if (newScale < minScale) newScale = minScale
        mapView.smoothScaleFromFocalPoint(mapView.width / 2, mapView.height / 2, newScale)
    }

    /**
     * Метод для перемещения камеры на положение пользователя
     */
    internal fun moveToMe() {
        mapView.scale = 1.3f
        newScale = 1.3f
        Log.d("PositionMarker", "${positionMarker.x} ${positionMarker.y}")
        try {
            mapView.moveToMarker(positionMarker, true)
        } catch (e: Exception) {
        }
    }
}