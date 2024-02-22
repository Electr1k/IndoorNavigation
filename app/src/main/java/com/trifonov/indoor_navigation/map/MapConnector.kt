package com.trifonov.indoor_navigation.map

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.NumberPicker
import androidx.appcompat.widget.AppCompatImageView
import androidx.navigation.NavController
import com.trifonov.indoor_navigation.R
import com.trifonov.indoor_navigation.common.LocationEntity
import com.trifonov.indoor_navigation.data.dto.Location
import com.trifonov.indoor_navigation.map.MapConstants.dotList
import com.trifonov.indoor_navigation.map.MapConstants.finishNode
import com.trifonov.indoor_navigation.map.MapConstants.levelArray
import com.trifonov.indoor_navigation.map.MapConstants.levelNumber
import com.trifonov.indoor_navigation.map.MapConstants.startNode
import com.trifonov.indoor_navigation.map.MapConstants.zoomLevelCount
import org.json.JSONObject
import org.json.JSONTokener
import ovh.plrapps.mapview.MapView
import ovh.plrapps.mapview.api.addMarker
import ovh.plrapps.mapview.api.removeMarker

class MapConnector(
    private val activity: Activity,
    private val navController: NavController
):
    NumberPicker.OnValueChangeListener,
    View.OnClickListener
{

    private lateinit var locationName: String
    private lateinit var mapView: MapView
    private lateinit var mapHelper: MapHelper
    private lateinit var levelPicker: NumberPicker
    private lateinit var zoomIn: ImageButton
    private lateinit var zoomOut: ImageButton
    private lateinit var position: ImageButton
    private var navigation: Navigation = Navigation()
    private val parentView: ViewGroup = activity.findViewById<FrameLayout>(R.id.viewGroup) as ViewGroup
    private var isFromAssets = true
    /**
     * @Param [fileHelper] класс для работы с файловой системой
     * @See [FileHelper]
     * */
    private lateinit var fileHelper: FileHelper

    init {
        activity.runOnUiThread {
            configureViews(parentView, false)
            initStartMap()
        }
    }

    /**
     * Метод для установки начальной карты
     * */
    private fun initStartMap(){
        val json = activity.assets.open("map.json").bufferedReader().use{
            it.readText()
        }
        locationName = ""
        isFromAssets = true
        zoomLevelCount = activity.assets.list("tiles1")!!.size - 1

        val map = JSONTokener(json).nextValue() as JSONObject
        val jsonDots = map.getJSONArray("dots")
        val locationId = map.getInt("locationId")
        MapConstants.mapWidth = map.getInt("width")
        MapConstants.mapHeight = map.getInt("height")
        dotList.clear()
        var i = -1
        while (++i < jsonDots.length()) {
            val jsonDot = jsonDots.getJSONObject(i)
            val dot = Map.Dot(jsonDot.getDouble("x").toFloat(), jsonDot.getDouble("y").toFloat())
            dot.setLevel(jsonDot.getInt("floor"))
            dot.setMac(jsonDot.getString("mac"))
            dot.setName(jsonDot.getString("name"))
            dot.setDescription(jsonDot.getString("description"))
            dot.setType(jsonDot.getString("type"))
            dot.setPhotos(jsonDot.getJSONArray("photoUrls"))
            dot.setId(jsonDot.getInt("id"))
            dot.setConnected(jsonDot.getJSONArray("connected"))
            if (!levelArray.contains(dot.getLevel().toString())) {
                levelArray.add(dot.getLevel().toString())
            }
            dotList.add(dot)
        }
        levelArray.sort()

        parentView.removeAllViewsInLayout()
        mapView = MapView(activity)
        parentView.addView(mapView)
        parentView.addView(zoomIn)
        parentView.addView(zoomOut)
        parentView.addView(position)
        parentView.addView(levelPicker)
        configureViews(parentView)
        configureMapView(mapView,1f)
    }

    /**
     * Метод для установки локации по названию локации
     * @Param [location] - локация
     * @Param [downloadView] - view с прогресс баром (R.layout.download_view)
     * @Param [dialog] - dialog, для закрытия диалога после загрузки
     * @return Boolean - успешная/безуспешная инициализация
     * */
    internal fun setLocation(location: Location, downloadView: View? = null, dialog: AlertDialog? = null): Boolean {
        locationName = location.dataUrl
        isFromAssets = false
        fileHelper = FileHelper(activity, downloadView, location, dialog)
        val json = fileHelper.getJsonMap(location)
        return if (json != "empty location") {
            loadFromString(json)
            activity.runOnUiThread {
                parentView.removeAllViewsInLayout()
                mapView = MapView(activity)
                parentView.addView(mapView)
                parentView.addView(zoomIn)
                parentView.addView(zoomOut)
                parentView.addView(position)
                parentView.addView(levelPicker)
                configureViews(parentView)
                configureMapView(mapView,1f)
            }
            true
        } else false
    }


    /**
     * Метод для настройки view выбора этажа
     * @Param [levelPicker] view для выбора номера этажа
     * @See [MapFragment.initPickerWithString]
     * */
    private fun configureLevelPicker(levelPicker: NumberPicker) {
        try {
            levelArray.reverse()
            levelPicker.wrapSelectorWheel = false
            initPickerWithString(1, levelArray.size, levelPicker, levelArray.toTypedArray())
            levelPicker.setOnValueChangedListener(this)
        } catch (e: Exception) {
        }
    }

    /**
     * Метод для перемещения камеры к точке
     * */
    internal fun moveCameraToDot(dot: Map.Dot){
        mapHelper.moveCamera(dot.getX().toDouble(), dot.getY().toDouble(), 1f, true)
    }

    /**
     * Метод для добавления маркера
     * @Param [marker] макер
     * */
    internal fun setMarker(marker: AppCompatImageView, x: Double, y: Double){
        mapHelper.addDynamicMarker(marker)
        mapView.addMarker(marker, x, y, -0.5f, -0.5f)

    }

    /**
     * Метод для удаления маркера
     * @Param [marker] маркер
     * */
    internal fun removeMarker(marker: AppCompatImageView){
        mapHelper.removeDynamicMarker(marker)
        mapView.removeMarker(marker)
    }


    /**
     * Метод для настройки диапазона значений, отображаемых в [levelPicker]
     * @Param [min] минимальный номер этажа
     * @Param [max] максимальный номер этажа
     * @Param [p] view для выбора этажа которую мы настраиваем
     * @Param [levels] массив номеров этажей
     * @See [MapFragment.configureLevelPicker]
     * */
    private fun initPickerWithString(min: Int, max: Int, p: NumberPicker, levels: Array<String>) {
        p.minValue = min
        p.maxValue = max
        p.value = max
        p.displayedValues = levels
    }

    /**
     * Метод для поиска и настройки работы с view элементами
     * @Param [fragmentView] родительское view
     * @Param [confMap] флаг для проверки сконфигурированность [mapView]
     * */
    private fun configureViews(view: View, confMap: Boolean = true) {
        zoomIn = view.findViewById(R.id.btn_zoomIn)
        zoomOut = view.findViewById(R.id.btn_zoomOut)
        position = view.findViewById(R.id.btn_position)
        if (confMap) zoomIn.setOnClickListener(this)
        if (confMap) zoomOut.setOnClickListener(this)
        if (confMap) position.setOnClickListener(this)
        levelPicker = view.findViewById(R.id.picker)
        if (confMap) configureLevelPicker(levelPicker)
        mapView = view.findViewById(R.id.mapView) ?: return
        if (confMap) configureMapView(mapView)
    }

    /**
     * Метод для настройки [mapHelper]
     * @See [MapHelper]
     * @Param [mapView] view для настройки
     * @Param [scale] уровень приближения карты
     * */
    private fun configureMapView(
        mapView: MapView,
        scale: Float = 0f,
    ) {
        mapHelper =
            MapHelper(activity, mapView, locationName, navigation, navController, isFromAssets)
        mapHelper.setScale(scale)
        mapHelper.addAllMarkers(dotList)
        mapHelper.addReferentialListener()
        mapHelper.addMarkerClickListener()
        mapHelper.addPositionMarker(startNode.toString(), 0f)
        mapHelper.addCenterScreenMarker()
        mapHelper.addFinishMarker(finishNode.toString())
    }

    /**
     * Получает [dotList], [mapWidth], [mapHeight] и [zoomLevelCount] из json строки
     * @Param [json] - строка в формате json с графом
     * @See [Map]
     */
    private fun loadFromString(json: String) {
        zoomLevelCount = fileHelper.getLevelCount("tiles1") - 1
        navigation.loadMapFromJson(json)
    }

    /**
     * Метод для обработки обновления значения [levelPicker]
     * @See [MapFragment.configureViews]
     * @See [MapFragment.configureMapView]
     * */
    @SuppressLint("ResourceAsColor", "SoonBlockedPrivateApi")
    override fun onValueChange(picker: NumberPicker?, oldVal: Int, newVal: Int) {
        try {
            levelNumber = levelArray[picker?.value!! - 1].toInt()
            if (oldVal != newVal) {
                updateViews()
                configureMapView(mapView, mapHelper.getScale())
                mapHelper.updatePath()
            }
        } catch (e: Exception) {
        }
    }

    /**
     * @Param [id] идентификатор метки положения
     * Метод для установки этажа на положение
     * пользователя
     * */
    internal fun setLevelById(id: Int) {
        if(id == 136) onValueChange(levelPicker, 1, 2)
    }

    /**
     * Метод для пересоздания всех view на экране
     * */
    private fun updateViews() {
        mapHelper.updateCameraData()
        parentView.removeAllViewsInLayout()
        mapView = MapView(activity)
        parentView.addView(mapView)
        parentView.addView(levelPicker)
        parentView.addView(zoomIn)
        parentView.addView(zoomOut)
        parentView.addView(position)
        configureViews(parentView, false)
    }

    /**
     * Метод для построения маршрута
     * @Param [finish] идентификатор точки конца маршрута
     * @Param [start] идентификатор точки начала маршрута
     * */
    internal fun updatePath(finish: Int, start: Int = startNode) {
        finishNode = finish
        startNode = start
        mapHelper.updatePath()
        mapHelper.movePosition(startNode.toString())
    }

    /**
     * Обработчки нажатий на кнопки приближения и отдаления карты
     * */
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_zoomIn -> mapHelper.zoomIn()
            R.id.btn_zoomOut -> mapHelper.zoomOut()
            R.id.btn_position -> mapHelper.moveToMe()
        }
    }
}