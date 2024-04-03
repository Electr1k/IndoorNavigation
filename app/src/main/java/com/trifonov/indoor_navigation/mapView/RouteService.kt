package com.trifonov.indoor_navigation.mapView

/**
 * Сервис для работы с маршрутами
 * */
class RouteService private constructor(private val map: CustomMap){
    /**
     * Основной маршрут - маршрут, который был построен после нажатия на кнопку "применить"
     * Черновик маршрута - маршрут, построения которого пользовтель не подтвердил
     * */

    /** Стартовая точка основного маршурта */
    var startDot: Int? = null
        get() = field
        private set(value) { field = value }

    /** Конечная точка основного маршурта */
    var endDot: Int? = null
        get() = field
        private set(value) { field = value }

    /** Стартовая точка черновика маршрута */
    var startDotTemp: Int? = null
        get() = field
        private set(value) { field = value }

    /** Конечная точка черновика маршрута */
    var endDotTemp: Int? = null
        get() = field
        private set(value) { field = value }

    /** Отображается ли сейчас основной маршрут*/
    var currentRouteIsMain = true
        get() = field
        private set(value) { field = value }


    /**Singleton*/
    companion object {

        @Volatile
        private var instance: RouteService? = null

        fun getInstance(mapView: CustomMap) =
            instance ?: synchronized(this) {
                instance ?: RouteService(mapView).also { instance = it }
            }
    }

    /** Сделать черновой маршрут основным*/
    fun saveTempRouteAsMain(){
        startDot = startDotTemp
        endDot = endDotTemp
        startDotTemp = null
        endDotTemp = null
    }

    /** Построить основной маршрут
     * @param start - id точки начала маршрута
     * @param end - id точки конца маршрута
     * */
    fun buildMainRoute(start: Int = startDot!!, end: Int = endDot!!){
        currentRouteIsMain = true
        startDot = start
        endDot = end
        map.drawPath(startDot!!, endDot!!)
    }

    /** Показать черновик маршрута
     * @param start - id точки начала маршрута
     * @param end - id точки конца маршрута
     * */
    fun buildTempRoute(start: Int = startDotTemp!!, end: Int = endDotTemp!!){
        currentRouteIsMain = false
        // Добавлять ли маршрут заново или перестраивать существующий
        val addPath = (startDotTemp == null || endDotTemp == null) && startDotTemp == null && endDot == null // Был ли построен хотя бы один маршрут
        startDotTemp = start
        endDotTemp = end
        map.drawPath(start, end, addPath)
    }

    /** Очистить начала и конец черновика маршрута */
    fun deleteTempDots(){
        startDotTemp = null
        endDotTemp = null
    }

}