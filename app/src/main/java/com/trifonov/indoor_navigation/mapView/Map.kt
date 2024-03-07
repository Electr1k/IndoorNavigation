/**
 * Класс для базовой работы с картой
 * @Author Братусев Денис
 * @Since 01.06.2023
 * @Version 1.0
 * */
package com.trifonov.indoor_navigation.mapView

import kotlin.math.sqrt

class Map(private val mapData: MapData) {

    fun getDot(id: Int): Dot {
        return mapData.dotList[id]
    }

    fun dist(dot1: Int, dot2: Int): Float {
        if (mapData.dotList.isEmpty()) return -1f
        if (dot1 < 0 || dot2 < 0) return -1f
        if (dot1 > mapData.dotList.size || dot2 > mapData.dotList.size) return -1f
        val dX1 = mapData.fullWidth * mapData.dotList[dot1].getX()
        val dX2 = mapData.fullWidth * mapData.dotList[dot2].getX()
        val dY1 = mapData.fullHeight * mapData.dotList[dot1].getY()
        val dY2 = mapData.fullHeight * mapData.dotList[dot2].getY()

        return sqrt((dX1 - dX2) * (dX1 - dX2) + (dY1 - dY2) * (dY1 - dY2))
    }

    fun clear() {
        for (dot in mapData.dotList) {
            dot.setVisited(false)
            dot.setFromId(-1)
            dot.setG(0f)
            dot.setH(0f)
        }
    }
}