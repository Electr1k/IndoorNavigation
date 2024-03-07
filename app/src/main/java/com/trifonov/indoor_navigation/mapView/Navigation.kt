/**
 * Класс для вычисления путей на графе
 * @Author Братусев Денис
 * @Since 01.06.2023
 * @Version 1.0
 * */
package com.trifonov.indoor_navigation.mapView

import kotlin.math.sqrt

class Navigation(private val map: Map) {

    private var pathCash: PathCash?

    init {
        pathCash = null
    }

    fun dist(start: Int, finish: Int): Float {
        val nodeArray = pathToArray(path(start, finish))
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

    private fun pathToArray(path : ArrayList<PathModel>) : FloatArray{
        val floatPath = FloatArray(path.size)
        var i = 0
        for (pathModel in path) {
            floatPath[i] = pathModel.x
            floatPath[i+1] = pathModel.y
            i+=2
        }

        return floatPath
    }

    fun path(start: Int, finish: Int): ArrayList<PathModel> {
        if (pathCash?.from == start && pathCash?.to == finish) return ArrayList()
        val que = ArrayList<Dot>()
        map.getDot(start).setG(0f)
        map.getDot(start).setH(map.dist(start, finish))

        que.add(map.getDot(start))

        while (que.isNotEmpty()) {
            que.sortBy { it.getF() }
            val x: Dot = que[0]
            que.removeAt(0)
            if (x.getId() == finish) {
                return reconstructPath(x.getId())
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
        return ArrayList()
    }

    private fun reconstructPath(finish: Int): ArrayList<PathModel> {
        val lines = ArrayList<PathModel>()
        try {
            val path = ArrayList<Int>()
            path.add(finish)
            var from = map.getDot(finish).getFromId()
            while (from != -1) {
                path.add(from)
                from = map.getDot(from).getFromId()
            }
            path.reverse()
            for (i in path) {
                lines.add(PathModel(map.getDot(i).getX(), map.getDot(i).getY(), map.getDot(i).getLevel(), map.getDot(i).getId()))
                lines.add(PathModel(map.getDot(i).getX(), map.getDot(i).getY(), map.getDot(i).getLevel(), map.getDot(i).getId()))
            }
            pathCash = object : PathCash() {
                override var path = pathToArray(lines)
                override var from = path[0]
                override var to = path[path.size - 1]
            }
            map.clear()
            return lines
        } catch (_: Exception) {
            return lines
        }
    }

    abstract class PathCash {
        abstract var path: FloatArray
        abstract var from: Int
        abstract var to: Int
    }
}