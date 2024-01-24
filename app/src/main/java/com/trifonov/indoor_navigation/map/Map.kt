/**
 * Класс для базовой работы с картой
 * @Author Братусев Денис
 * @Since 01.06.2023
 * @Version 1.0
 * */
package com.trifonov.indoor_navigation.map

import com.trifonov.indoor_navigation.map.MapConstants.dotList
import com.trifonov.indoor_navigation.map.MapConstants.levelArray
import com.trifonov.indoor_navigation.map.MapConstants.mapHeight
import com.trifonov.indoor_navigation.map.MapConstants.mapWidth
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import kotlin.math.sqrt

/**
 * Класс Map, содержащий базовые методы для работы с картой
 *
 * @constructor Создаёт объект типа Map без параметров
 */
class Map {
    /**
     * Получает [dotList], [mapWidth] и [mapHeight] из json строки
     * @Param [json] - строка в формате json с графом
     */
    fun loadFromString(json: String) {
        println("loadFromString")
        val map = JSONTokener(json).nextValue() as JSONObject
        val jsonDots = map.getJSONArray("dots")
        val locationId = map.getInt("locationId")
        mapWidth = map.getInt("width")
        mapHeight = map.getInt("height")
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
            if (!levelArray.contains(dot.getLevel().toString())) {
                levelArray.add(dot.getLevel().toString())
            }
            dotList.add(dot)
        }
        levelArray.sort()
    }

    /**
     * Возвращает точку на графе по [id]
     * @See [Dot]
     * @Param [id]
     * @Return возвращает точку на графе
     */
    fun getDot(id: Int): Dot {
        return dotList[id]
    }

    /**
     * Считает расстояние от точки до точки на графе
     *
     * @Param [dot1] идентификатор первой точки
     * @Param [dot2] идентификатор второй точки
     * @Return расстояние между точками
     */
    fun dist(dot1: Int, dot2: Int): Float {
        if (dotList.isEmpty()) return -1f
        if (dot1 < 0 || dot2 < 0) return -1f
        if (dot1 > dotList.size || dot2 > dotList.size) return -1f
        val dX1 = mapWidth * dotList[dot1].getX()
        val dX2 = mapWidth * dotList[dot2].getX()
        val dY1 = mapHeight * dotList[dot1].getY()
        val dY2 = mapHeight * dotList[dot2].getY()

        return sqrt((dX1 - dX2) * (dX1 - dX2) + (dY1 - dY2) * (dY1 - dY2))
    }

    /**
     * Сбрасывает состояние точек к исходному из списка [dots]
     */
    fun clear() {
        for (dot in dotList) {
            dot.setVisited(false)
            dot.setFromId(-1)
            dot.setG(0f)
            dot.setH(0f)
        }
    }

    /**
     * Стурктура данных "Точка"
     *
     * @Param [x] X координата точки
     * @property [y] Y координата точки
     * @Constructor Создаёт точку с координатами [x], [y]
     */
    class Dot(private var x: Float, private var y: Float) {
        /**
         * @Param [id] идентификатор точки
         * @Param [g]  стоимость пути от начальной вершины
         * @Param [h]  эвристическое приближение
         * @Param [visited] флаг для проверки посещения точки
         * @Param [level] этаж на котором расположена точка на графе
         * @Param [fromId] идентификатор исходной точки маршрута
         * @Param [nei] массив идентификаторов соседних точек для данной
         * */
        private var id = 0
        private var mac = "00:00:00:00"
        private var name = " "
        private var description = " "
        private var type = " "
        private var g = 0f
        private var h = 0f
        private var visited = false
        private var level = 1
        private var fromId = -1
        private var nei = ArrayList<Int>()
        private var photoUrls = ArrayList<String>()

        /** Устанавливает [level] для текущей точки */
        fun setLevel(level: Int) {
            this.level = level
        }

        /** Возвращает значение [level] для текущей точки */
        fun getLevel(): Int {
            return level
        }

        fun setName(name: String){
            this.name = name
        }

        fun getName() : String{
            return name
        }

        fun setDescription(desc: String){
            this.description = desc
        }

        fun getDescription() : String{
            return description
        }

        fun setMac(mac: String){
            this.mac = mac
        }

        fun getMac() : String{
            return mac
        }

        fun setType(type: String){
            this.type = type
        }

        fun getType() : String{
            return type
        }

        /** Устанавливает [g] для текущей точки */
        fun setG(g: Float) {
            this.g = g
        }

        /** Возвращает значение [g] для текущей точки */
        fun getG(): Float {
            return g
        }

        /** Устанавливает [h] для текущей точки */
        fun setH(h: Float) {
            this.h = h
        }

        /** Устанавливает [fromId] для текущей точки */
        fun setFromId(id: Int) {
            fromId = id
        }

        /** Возвращает значение [fromId] для текущей точки */
        fun getFromId(): Int {
            return fromId
        }

        /** Устанавливает [visited] для текущей точки */
        fun setVisited(x: Boolean) {
            visited = x
        }

        /** Возвращает значение [visited] для текущей точки */
        fun isVisited(): Boolean {
            return visited
        }

        /** Устанавливает [id] для текущей точки */
        fun setId(id: Int) {
            this.id = id;
        }

        /** Возвращает значение [id] для текущей точки */
        fun getId(): Int {
            return id
        }

        /** Возвращает значение [x] для текущей точки */
        fun getX(): Float {
            return x
        }

        /** Возвращает значение [g] + [h] для текущей точки */
        fun getF(): Float {
            return g + h
        }

        /** Возвращает значение [y] для текущей точки */
        fun getY(): Float {
            return y
        }

        /** Устанавливает [nei] для текущей точки */
        fun setConnected(nei: JSONArray) {
            var i = -1
            while (++i < nei.length()) {
                this.nei.add(nei.getInt(i));
            }
        }

        /** Возвращает значение [nei] для текущей точки */
        fun getConnected(): ArrayList<Int> {
            return nei
        }

        fun setPhotos(photos: JSONArray) {
            var i = -1
            while (++i < photos.length()) {
                this.photoUrls.add(photos.getString(i));
            }
        }

        fun getPhotos() : ArrayList<String>{
            return photoUrls
        }
    }
}