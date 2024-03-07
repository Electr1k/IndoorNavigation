package com.trifonov.indoor_navigation.mapView

import org.json.JSONArray

data class Dot(private var x: Float, private var y: Float) {

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

    override fun toString(): String {
        return "$id $level $name"
    }

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