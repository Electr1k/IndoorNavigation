package com.trifonov.indoor_navigation.mapView

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.NumberPicker.OnValueChangeListener
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import com.trifonov.indoor_navigation.R
import ovh.plrapps.mapview.MapView
import ovh.plrapps.mapview.MapViewConfiguration
import ovh.plrapps.mapview.ReferentialData
import ovh.plrapps.mapview.ReferentialListener
import ovh.plrapps.mapview.api.addMarker
import ovh.plrapps.mapview.api.moveMarker
import ovh.plrapps.mapview.api.moveToMarker
import ovh.plrapps.mapview.api.removeMarker
import ovh.plrapps.mapview.paths.PathView
import ovh.plrapps.mapview.paths.addPathView
import ovh.plrapps.mapview.paths.removePathView
import kotlin.math.atan

class CustomMap(private val context: Context, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs),
    OnValueChangeListener, OnClickListener {

    private val numberPicker: NumberPicker
    private val plusButton: ImageView
    private val minusButton: ImageView
    private val positionButton: ImageView
    private var mapView: MapView
    private var listener: CustomViewListener? = null

    private val finishMarker = AppCompatImageView(context).apply {
        setImageResource(R.drawable.finish_marker)
    }
    private val startMarker = AppCompatImageView(context).apply {
        setImageResource(R.drawable.start_parh_icon)
    }
    private val myPositionMarker = AppCompatImageView(context).apply {
        setImageResource(R.drawable.position_marker)
    }
    private val centerMarker = AppCompatImageView(context).apply {
        setImageResource(R.drawable.position_marker)
    }
    private val openAudience = AppCompatImageView(context).apply {
        setImageResource(R.drawable.marker)
    }
    private var dotOpenAudience: Dot? = null

    private val strokePaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.path)
        strokeCap = Paint.Cap.ROUND
    }

    private var pathView: PathView
    private var newScale = 0f
    private var positionRotation = 0f
    private var markerList = ArrayList<MapMarker>()
    private var dotList = ArrayList<Dot>()
    private var levelNumber: Int = 1
    private var startNode = 0
    private var finishNode = 0
    private var myPosition: Dot? = null

    private lateinit var mapData: MapData
    private var minPathWidth = 0f
    private var maxScale = 0f
    private var minScale = 0f
    private var maxPathWidth = 0f
    private var lastPath = FloatArray(0)

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.map_layout, this, true)

        numberPicker = findViewById(R.id.picker)
        plusButton = findViewById(R.id.btn_zoomIn)
        minusButton = findViewById(R.id.btn_zoomOut)
        positionButton = findViewById(R.id.btn_position)
        mapView = findViewById(R.id.map)
        pathView = PathView(context)

        setChangeListeners()
    }

    private fun setLevelNumber(level: String) {
        numberPicker.value = mapData.levelArray.size + 1 - level.toInt()
        levelNumber = level.toInt()
    }

    internal fun setMap(mapData: MapData, needDestroy: Boolean = false, levelNumber: String = "1", addPath: Boolean = false) {
        if (needDestroy) destroyMapView()
        val config = MapViewConfiguration(
            levelCount = mapData.zoomLevelCount,
            fullWidth = mapData.fullWidth,
            fullHeight = mapData.fullHeight,
            tileSize = mapData.tileSize,
            tileStreamProvider = mapData.tileStreamProvider
        ).setMaxScale(mapData.setMaxScale).enableRotation()
        fillMapData(mapData)
        configureLevelPicker()
        setLevelNumber(levelNumber)
        mapView.configure(config)
        mapView.defineBounds(0.0, 0.0, mapData.fullWidth.toDouble(), mapData.fullHeight.toDouble())
        mapView.addReferentialListener(refOwner)
        addFinishMarker()
        addStartMarker()
        if (myPosition == null){
            myPosition = dotList.find { it.getId() == 33}!!.copy()
            myPosition!!.setId(33)
            myPosition!!.setName("Моё местоположение")
        }
        addMyPositionMarker()
        addCenterScreenMarker()
        addOpenAudienceMarker()
        updatePath(addPath)
        for (marker in markerList) {
            addMarker(marker)
        }
        setChangeListeners()
        changeVisibilityMyPositionMarker()
        changeVisibilityAudienceMarker()
    }
    private fun addMarker(marker: MapMarker){
        marker.apply {
            if (marker.name.isNotEmpty()) {
                marker.dotId
                setImageDrawable(BitmapDrawable(resources, drawText("$name ")))
                setOnClickListener {
                    listener?.onTap(it, marker.dotId)
                }
            }
        }
        marker.rotation = 0f
        if (levelNumber == marker.level && marker.name.isNotEmpty()) mapView.addMarker(marker, marker.x, marker.y)
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
     * Метод для перемещения камеры к точке
     * */
    internal fun moveCameraToDot(dot: Dot){
        mapView.moveMarker(centerMarker, dot.getX().toDouble(), dot.getY().toDouble())
        mapView.setScaleFromCenter(1.3f)
        mapView.moveToMarker(centerMarker,true)
    }


    private fun setStartPosition(start: Int) {
        startNode = start
    }

    fun getStartPosition(): Int {
        return startNode
    }

    private fun setFinishPosition(finish: Int, addPath: Boolean = false) {
        finishNode = finish
        updatePath(addPath = addPath)
    }

    fun getFinishPosition(): Int {
        return finishNode
    }

    fun getMyPosition(): Dot {
        return myPosition!!
    }

    fun setMyPosition(position: Dot) {
        myPosition = position
        mapView.moveMarker(myPositionMarker,position.getX().toDouble(), position.getY().toDouble())
    }

    private fun moveStartMarker() {
        for (marker in markerList) {
            if (marker.dotId == startNode) {
                startMarker.visibility = View.VISIBLE
                mapView.moveMarker(startMarker, marker.x, marker.y)
                break
            }
        }
    }

    internal fun moveStartMarker(id: Int) {
        startNode = id
        for (marker in markerList) {
            if (marker.dotId == id) {
                startMarker.visibility = View.VISIBLE
                mapView.moveMarker(startMarker, marker.x, marker.y)
                break
            }
        }
        if(finishNode != 0) updatePath()
    }

    private fun moveFinishMarker() {
        for (marker in markerList) {
            if (marker.dotId == finishNode) {
                finishMarker.visibility = View.VISIBLE
                mapView.moveMarker(finishMarker, marker.x, marker.y)
                break
            }
        }
    }

    internal fun removePath(needFullDrop: Boolean = false): Int {
        mapView.removePathView(pathView)
        mapView.removeView(pathView)
        val parent = mapView.parent as ViewGroup
        startMarker.visibility = INVISIBLE
        finishMarker.visibility = INVISIBLE
        if(needFullDrop){
            startNode = 0
            finishNode = 0
        }
        return parent.indexOfChild(mapView)
    }

    internal fun setListener(customViewListener: CustomViewListener) {
        this.listener = customViewListener
    }

    private fun destroyMapView() {
        val parent = mapView.parent as ViewGroup
        mapView.removeMarker(startMarker)
        mapView.removeMarker(finishMarker)
        parent.removeView(startMarker)
        parent.removeView(finishMarker)
        val index = removePath()
        for(marker in markerList) {if (marker.name.isNotEmpty()) mapView.removeMarker(marker)}
        mapView.removeMarker(centerMarker)
        mapView.removeMarker(myPositionMarker)
        mapView.removeMarker(openAudience)
        parent.removeView(mapView)
        mapView = MapView(context)
        parent.addView(mapView, index)
    }

    private fun setChangeListeners() {
        numberPicker.setOnValueChangedListener(this)
        plusButton.setOnClickListener(this)
        minusButton.setOnClickListener(this)
        positionButton.setOnClickListener(this)
    }

    private fun configureLevelPicker() {
        try {
            val levelArray = mapData.levelArray
            numberPicker.wrapSelectorWheel = false
            initPickerWithString(1, levelArray.size, numberPicker, levelArray.toTypedArray())
        } catch (_: Exception) {
        }
    }

    private fun initPickerWithString(min: Int, max: Int, p: NumberPicker, levels: Array<String>) {
        p.minValue = min
        p.maxValue = max
        p.value = max
        p.displayedValues = levels
    }


    override fun onValueChange(picker: NumberPicker?, oldVal: Int, newVal: Int) {
        levelNumber = mapData.levelArray[picker?.value!! - 1].toInt()
        listener?.onLevelChanged(levelNumber.toString())
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_zoomIn -> zoomIn()
            R.id.btn_zoomOut -> zoomOut()
            R.id.btn_position -> moveToMe()
        }
    }

    private fun zoomIn() {
        newScale += (maxScale - minScale) / mapData.zoomLevelCount
        if (newScale > maxScale) newScale = maxScale
        mapView.smoothScaleFromFocalPoint(mapView.width / 2, mapView.height / 2, newScale)
    }

    private fun zoomOut() {
        newScale -= (maxScale - minScale) / mapData.zoomLevelCount
        if (newScale < minScale) newScale = minScale
        mapView.smoothScaleFromFocalPoint(mapView.width / 2, mapView.height / 2, newScale)
    }

    private fun moveToMe() {
        mapView.scale = 1.3f
        newScale = 1.3f
        try {
            mapView.moveToMarker(myPositionMarker, true)
        } catch (_: Exception) {
        }
    }

    private fun fillMapData(mapData: MapData) {
        this.mapData = mapData
        markerList = mapData.markerList
        dotList = mapData.dotList
        minPathWidth = mapData.minPathWidth
        maxScale = mapData.maxScale
        minScale = mapData.minScale
        maxPathWidth = mapData.maxPathWidth
    }

    private val refOwner = object : ReferentialListener {
        var referentialData: ReferentialData? = null

        override fun onReferentialChanged(refData: ReferentialData) {
            setMarkerScale(refData.scale)
            setPositionMarkerRotation(refData.angle)
            referentialData = refData
            for (mapMarker in markerList) {
                if (mapMarker.name.isNotEmpty()) setMarkerScale(refData.scale, mapMarker)
            }
            newScale = refData.scale
            createDrawablePath(lastPath)
        }
    }

    private fun addCenterScreenMarker() {
        centerMarker.visibility = View.INVISIBLE
        mapView.addMarker(centerMarker, 0.0, 0.0, -0.5f, -0.5f)
    }

    private fun addOpenAudienceMarker(){
        openAudience.visibility = View.INVISIBLE
        mapView.addMarker(openAudience,
            (dotOpenAudience?.getX()  ?: 0.0).toDouble(), (dotOpenAudience?.getY()  ?: 0.0).toDouble(), -0.5f, -0.5f)
    }

    /***
     * Добавить маркер на крату при открытии аудитории
     */
    internal fun setOpenAudienceMarker(dot: Dot){
        openAudience.visibility = View.VISIBLE
        mapView.moveMarker(openAudience, dot.getX().toDouble(), dot.getY().toDouble())
        dotOpenAudience = dot
    }

    /***
     * Добавить убрать маркер с карты при открытии закрытии
     */
    internal fun removeOpenAudienceMarker(dot: Dot){
        if (dotOpenAudience?.getId() == dot.getId()){
            openAudience.visibility = View.INVISIBLE
            dotOpenAudience = null
        }

    }

    /***
     * Метод для изменения
     */
    private fun changeVisibilityAudienceMarker(){
        if (dotOpenAudience?.getLevel() == levelNumber){
            openAudience.visibility = View.VISIBLE
        }
        else{
            openAudience.visibility = View.INVISIBLE
        }
    }

    private fun changeVisibilityMyPositionMarker(){
        if (myPosition!!.getLevel() == levelNumber){
            myPositionMarker.visibility = View.VISIBLE
        }
        else{
            myPositionMarker.visibility = View.INVISIBLE
        }
    }

    private fun addFinishMarker() {
        finishMarker.visibility = View.INVISIBLE
        mapView.addMarker(finishMarker, 0.0, 0.0, -0.5f, -0.5f, tag = "finish")
    }

    private fun addStartMarker() {
        startMarker.visibility = View.INVISIBLE
        mapView.addMarker(startMarker, 0.0, 0.0, -0.5f, -0.5f, tag = "start")
    }

    private fun addMyPositionMarker() {
        myPositionMarker.visibility = View.INVISIBLE
        mapView.addMarker(myPositionMarker, myPosition!!.getX().toDouble(), myPosition!!.getY().toDouble(), -0.5f, -0.5f, tag = "myPosition")
    }

    private fun setMarkerScale(scale: Float) {
        startMarker.scaleX = scale + 1f
        startMarker.scaleY = scale + 1f
        finishMarker.scaleX = scale + 1f
        finishMarker.scaleY = scale + 1f
        openAudience.scaleX = scale + 1f
        openAudience.scaleY = scale + 1f
        myPositionMarker.scaleX = scale + 1f
        myPositionMarker.scaleY = scale + 1f
    }

    private fun setPositionMarkerRotation(angle: Float) {
        startMarker.rotation = angle + positionRotation
    }

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

    private fun updatePath(addPath: Boolean = false): FloatArray {
        if (finishNode == startNode) return FloatArray(0)

        moveStartMarker()
        moveFinishMarker()
        var path = calculatePath(startNode, finishNode)
        if(path.size < 2) path = calculatePath(startNode, finishNode)
        lastPath = path
        createDrawablePath(path, addPath)
        return path
    }

    private fun createDrawablePath(path: FloatArray, addPath: Boolean = false) {
        val drawablePath = object : PathView.DrawablePath {
            override val visible: Boolean = true
            override var path: FloatArray = path
            override var paint: Paint? = strokePaint
            override val width: Float = calculatePathWidth()
        }
        pathView.updatePaths(listOf(drawablePath))
        if (addPath) {
            mapView.addPathView(pathView)
        }
    }

    private fun calculateAngel(x1: Int, y1: Int, x2: Int, y2: Int): Float {
        var angel = 90.0f
        val tmp = (atan(((y2 - y1).toDouble() / (x2 - x1).toDouble())) * 180 / Math.PI).toFloat()
        angel += if (x2 - x1 >= 0) tmp
        else tmp + 180
        return angel
    }

    private fun calculatePath(start: Int, finish: Int): FloatArray {
        val navigation = Navigation(Map(mapData = mapData))
        val path = navigation.path(start, finish)
        var i = 0
        var size = 0
        for (pathModel in path) {
            if (pathModel.level == levelNumber) {
                size++
            }
        }
        var isFirstOnLevel = true
        val pathList = try {
            FloatArray((size-1) * 2)
        }catch (_ : Exception){ FloatArray(0) }

        for (pathModel in path) {
            if (pathModel.level == levelNumber) {
                if (!isFirstOnLevel) {
                    pathList[i] = pathModel.x
                    pathList[i + 1] = pathModel.y
                    i += 2
                }else{
                    isFirstOnLevel = false
                }

            }
        }
        changeMarkerVisibility()
        if (pathList.size > 3) {
            val x1 = pathList[0].toInt()
            val y1 = pathList[1].toInt()
            val x2 = pathList[2].toInt()
            val y2 = pathList[3].toInt()
            positionRotation = calculateAngel(x1, y1, x2, y2)
            setPositionMarkerRotation(refOwner.referentialData?.angle ?: 0f)
        }
        return pathList
    }

    private fun changeMarkerVisibility() {
        var myStartMarker = Dot(0f, 0f)
        var myFinishMarker = Dot(0f, 0f)
        for (marker in dotList) {
            if (marker.getId() == startNode) myStartMarker = marker
            if (marker.getId() == finishNode) myFinishMarker = marker
        }

        if (levelNumber == myStartMarker.getLevel()) startMarker.visibility = View.VISIBLE
        else startMarker.visibility = View.INVISIBLE
        if (levelNumber == myFinishMarker.getLevel()) finishMarker.visibility = View.VISIBLE
        else finishMarker.visibility = View.INVISIBLE
    }

    private fun calculatePathWidth(): Float {
        var temp = minPathWidth + (maxPathWidth - minPathWidth) * newScale / maxScale
        if (newScale == minScale) temp = minPathWidth
        else if (newScale == maxScale) temp = maxPathWidth
        return temp// * 1.5f
    }

    fun drawPath(startPosition: Int, finishPosition: Int, addPath: Boolean = false) {
        setStartPosition(startPosition)
        setFinishPosition(finishPosition, addPath)
    }
}