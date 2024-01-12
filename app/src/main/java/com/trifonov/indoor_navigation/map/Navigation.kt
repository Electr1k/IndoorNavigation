/**
 * Класс для вычисления путей на графе
 * @Author Братусев Денис
 * @Since 01.06.2023
 * @Version 1.0
 * */
package com.trifonov.indoor_navigation.map

import kotlin.math.sqrt

/**
 * Класс для вычисления кратчайшего пути на графе
 * @Constructor Создаёт пустой объект класса Navigation
 */
class Navigation {
    /**
     * @See [Map]
     * */
    private var map: Map = Map()
    /**
     * @See [Navigation.PathCash]
     * */
    private var pathCash: PathCash?

    init {
        pathCash = null
    }

    /**
     * Возвращает точку графа
     * @See [Map.Dot]
     * @Param index идентификатор точки
     * @Return точка графа по идентификатору
     */
    fun getDot(index: Int): Map.Dot {
        return map.getDot(index)
    }

    /**
     * Вычисляет длинну маршрута
     * @Param start идентификатор точки начала пути
     * @Param finish идентификатор точки конца маршрута
     * @Return длинна маршрута
     */
    fun dist(start: Int, finish: Int): Float {
        val nodeArray = path(start, finish)
        var res = 0f
        var px = nodeArray!![0]
        var py = nodeArray[1]
        for (i in 2 until nodeArray.size step 2) {
            res += sqrt((nodeArray[i] - px) * (nodeArray[i] - px) + (nodeArray[i + 1] - py) * (nodeArray[i + 1] - py))
            px = nodeArray[i]
            py = nodeArray[i + 1]
        }
        return res
    }

    /**
     * Вычисляет кратчайший маршрут
     * @Param start идентификатор точки начала пути
     * @Param finish идентификатор точки конца маршрута
     * @Return массив координат точек маршрута
     */
    fun path(start: Int, finish: Int, level: Int = 1): FloatArray? {
        if (pathCash?.from == start && pathCash?.to == finish) return pathCash?.path
        val que = ArrayList<Map.Dot>()
        map.getDot(start).setG(0f)
        map.getDot(start).setH(map.dist(start, finish))

        que.add(map.getDot(start))

        while (que.isNotEmpty()) {
            que.sortBy { it.getF() }
            val x: Map.Dot = que[0]
            que.removeAt(0)
            if (x.getId() == finish) {
                return reconstructPath(x.getId(), level)
            }

                x.setVisited(true)
                for (y in x.getConnected()) {
                    if (map.getDot(y).isVisited()) continue
                    var isTentativeBetter = false
                    val tentativeGScore = x.getG() + map.dist(x.getId(), y)
                    if (!que.contains(map.getDot(y))) isTentativeBetter = true
                    else if (tentativeGScore < map.getDot(y).getG()) isTentativeBetter = true

                    if (isTentativeBetter) {
                        map.getDot(y).setG(tentativeGScore)
                        map.getDot(y).setH(map.dist(y, finish))
                        map.getDot(y).setFromId(x.getId())
                        que.add(map.getDot(y))
                    }
                }
        }

        map.clear()
        return null
    }

    /**
     * Вычисляет кратчайший маршрут от текущей точки
     * @Param finish идентификатор точки конца маршрута
     * @Return массив координат точек маршрута
     */
    private fun reconstructPath(finish: Int, level: Int = 1): FloatArray {
        try {
            val path = ArrayList<Int>()
            path.add(finish)
            var from = map.getDot(finish).getFromId()
            while (from != -1) {
                if(level == map.getDot(from).getLevel()) {
                    path.add(from)
                }

                from = map.getDot(from).getFromId()
            }
            if(level != map.getDot(finish).getLevel()) path.removeAt(0)
            path.reverse()
            var init = true
            val size = path.size * 4 - 4
            var i = 0
            var dot = 0
            val lines = FloatArray(size)
            while (i < size) {
                if (init) {
                    lines[i] = map.getDot(path[dot]).getX()
                    lines[i + 1] = map.getDot(path[dot]).getY()
                    init = false
                    i += 2
                    dot++
                } else {
                    lines[i] = map.getDot(path[dot]).getX()
                    lines[i + 1] = map.getDot(path[dot]).getY()
                    if (i + 2 >= size) break
                    lines[i + 2] = lines[i]
                    lines[i + 3] = lines[i + 1]
                    i += 4
                    dot++
                }
            }

            pathCash = object : PathCash() {
                override var path = lines
                override var from = path[0]
                override var to = path[path.size - 1]
            }
            map.clear()
            return lines
        } catch (e: Exception){}
        return FloatArray(0)
    }

    /**
     * Получает [dots], [width] и [height] из json строки
     * @Param [json] - строка в формате json с графом
     */
    fun loadMapFromJson(json: String) {
        map.loadFromString(json)
    }

    /**
     * Path cash
     *
     * @Constructor Create empty Path cash
     */
    abstract class PathCash {
        abstract var path: FloatArray
        abstract var from: Int
        abstract var to: Int
    }
}