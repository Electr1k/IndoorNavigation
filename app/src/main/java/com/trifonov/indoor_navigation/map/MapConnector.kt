package com.trifonov.indoor_navigation.map

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.NumberPicker
import androidx.core.view.size
import androidx.navigation.findNavController
import com.trifonov.indoor_navigation.R
import com.trifonov.indoor_navigation.map.MapConstants.dotList
import com.trifonov.indoor_navigation.map.MapConstants.finishNode
import com.trifonov.indoor_navigation.map.MapConstants.levelArray
import com.trifonov.indoor_navigation.map.MapConstants.levelNumber
import com.trifonov.indoor_navigation.map.MapConstants.startNode
import com.trifonov.indoor_navigation.map.MapConstants.zoomLevelCount
import ovh.plrapps.mapview.MapView

class MapConnector(
    private val activity: Activity,
    private val fragmentView: View,
    private val locationName: String
):
    NumberPicker.OnValueChangeListener,
    View.OnClickListener
{

    private lateinit var mapView: MapView
    private lateinit var mapHelper: MapHelper
    private lateinit var levelPicker: NumberPicker
    private lateinit var zoomIn: ImageButton
    private lateinit var zoomOut: ImageButton
    private lateinit var position: ImageButton
    private var navigation: Navigation = Navigation()
    private val parentView: ViewGroup = activity.findViewById<FrameLayout>(R.id.viewGroup) as ViewGroup

    /**
     * @Param [fileHelper] класс для работы с файловой системой
     * @See [FileHelper]
     * */
    private var fileHelper = FileHelper(
        activity,
        fragmentView,
        locationName
    )

    init {
        fileHelper = FileHelper(activity, fragmentView, locationName)
        val json = fileHelper.getJsonMap(locationName)
        println(json.substring(0, 20))
        if (json != "empty location") {
            loadFromString(json)
            activity.runOnUiThread {
                configureViews(parentView)
                fragmentView.findNavController().navigate(R.id.action_download_to_head)
            }
        }
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
            val delta =
                (levelPicker.minValue + levelPicker.value - levelArray.size) % levelPicker.size
            levelPicker.scrollBy(0, -(delta * levelPicker.getChildAt(0).height))
            levelPicker.setOnValueChangedListener(this)
        } catch (e: Exception) {
        }
    }

    /**
     * Метод для настройки диапазона значений, отображаемых в [levelPicker]
     * @Param [min] минимальный номер этажа
     * @Param [max] максимальный номер этажа
     * @Param [p] view для выбора этажа которую мы настраиваем
     * @Param [levels] массив номеров этажей
     *
     * @See [MapFragment.configureLevelPicker]
     * */
    private fun initPickerWithString(min: Int, max: Int, p: NumberPicker, levels: Array<String>) {
        p.minValue = min
        p.maxValue = max
        p.displayedValues = levels
    }

    /**
     * Метод для поиска и настройки работы с view элементами
     * @Param [fragmentView] родительское view
     * @Param [confMap] флаг для проверки сконфигурированность [mapView]
     * */
    private fun configureViews(view: View, confMap: Boolean = true) {
        println("Настройка конфигурации")
        zoomIn = view.findViewById(R.id.btn_zoomIn)
        zoomOut = view.findViewById(R.id.btn_zoomOut)
        position = view.findViewById(R.id.btn_position)
        zoomIn.setOnClickListener(this)
        zoomOut.setOnClickListener(this)
        position.setOnClickListener(this)
        levelPicker = view.findViewById(R.id.picker)
        mapView = view.findViewById(R.id.mapView) ?: return
        configureLevelPicker(levelPicker)
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
        scale: Float = 0f
    ) {
        mapHelper =
            MapHelper(activity, mapView, locationName, navigation)
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
                configureViews(parentView, false)
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
        configureViews(parentView)
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