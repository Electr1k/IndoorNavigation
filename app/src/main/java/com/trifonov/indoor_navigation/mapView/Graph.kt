package com.trifonov.indoor_navigation.mapView

import java.util.PriorityQueue
import kotlin.math.pow
import kotlin.math.sqrt

class Graph(private val dots: ArrayList<Dot>) {

    data class Node(
        val id: Int,
        val x: Float,
        val y: Float,
        var g: Int = 0,
        var f: Int = 0,
        var h: Int = 0,
        val level: Int = 1,
        var visited: Boolean = false,
        val connected: ArrayList<Int>? = null,
        var parent: Node? = null
    )

    private val nodeArray = ArrayList<Node>()

    init {
        for (dot in dots) {
            nodeArray.add(Node(dot.getId(), dot.getX(), dot.getY(), level = dot.getLevel(), connected = dot.getConnected()))
        }
    }

   /* private fun getConnected(dots: ArrayList<Int>): ArrayList<Node> {
        val nodeNeighbors = ArrayList<Node>()
        for (dot in dots) {
            val tempDot = getDotById(dot)
            nodeNeighbors.add(Node(tempDot.getId(), tempDot.getX(), tempDot.getY(), connected = getConnected(tempDot.getConnected())))
        }
        return nodeNeighbors
    }*/

    private fun getNodeById(id: Int): Node{
        for (node in nodeArray) {
            if(node.id == id) return node
        }
        return Node(0, 0f, 0f)
    }

    private fun getDotById(id: Int): Dot {
        for (dot in dots) {
            if(dot.getId() == id) return dot
        }
        return Dot(0f, 0f)
    }

    fun aStar(start: Int, end: Int): List<Node>? {
        val startNode = getNodeById(start)
        val endNode = getNodeById(end)
        val openList = PriorityQueue<Node>(compareBy { it.f })
        openList.add(startNode)
        val closedSet = mutableSetOf<Node>()

        while (openList.isNotEmpty()) {
            val current = openList.poll()
            if (current == endNode) {
                val path = mutableListOf<Node>()
                var currentNode = current
                while (currentNode != null) {
                    path.add(currentNode)
                    currentNode = currentNode.parent
                }
                return path.asReversed()
            }
            if (current != null) {
                closedSet.add(current)
            }
            val neighbors = parseToNode(current?.connected ?: ArrayList())
            if (neighbors != null) {
                for (neighbor in neighbors) {
                    if (neighbor in closedSet) continue
                    val newG = current.g + 1
                    if (openList.any { it == neighbor }) {
                        val nfo = openList.first { it == neighbor }
                        if (newG < nfo.g) {
                            nfo.g = newG
                            nfo.h = sqrt(((endNode.x - nfo.x).toDouble().pow(2) + (endNode.y - nfo.y).toDouble().pow(2))).toInt()
                            nfo.f = nfo.g + nfo.h
                            nfo.parent = current
                            openList.remove(nfo)
                            openList.add(nfo)
                        }
                    } else {
                        neighbor.g = newG
                        neighbor.h = sqrt(((endNode.x - neighbor.x).toDouble().pow(2) + (endNode.y - neighbor.y).toDouble().pow(2))).toInt()
                        neighbor.f = neighbor.g + neighbor.h
                        neighbor.parent = current
                        openList.add(neighbor)
                    }
                }
            }
        }
        return null
    }

    private fun parseToNode(connected: ArrayList<Int>): ArrayList<Node> {
        val array = ArrayList<Node>()
        connected.forEach { elem ->
            array.add(getNodeById(elem))
        }
        return array
    }

}