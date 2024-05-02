package com.trifonov.indoor_navigation.mapView

import android.os.Environment


object MapConstants {
    /**
     * @Param [SDPath] путь к файлам проекта
     * @Param [dataPath] путь для скачивания архива
     * @Param [unzipPath] путь для разархивации
     * */
    private val SDPath =
        Environment.getExternalStorageDirectory().absolutePath + "/Android/data/com.trifonov.indoor_navigation"
    internal val dataPath = "$SDPath/files/locations/"
    internal val unzipPath = "$SDPath/files/locations/"
    internal val baseUrl = "http://indoor.skbkit.ru/api/"

    internal var startDistance: Float = 0f
    internal var factDistance: Float = 0f
}