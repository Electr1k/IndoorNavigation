/**
 * Класс для работы с файловой системой
 * @Author Братусев Денис
 * @Since 01.06.2023
 * @Version 1.0
 * */
package com.trifonov.indoor_navigation.mapView

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.gson.Gson
import com.trifonov.indoor_navigation.R
import com.trifonov.indoor_navigation.data.dto.Location
import com.trifonov.indoor_navigation.data.dto.Locations
import com.trifonov.indoor_navigation.mapView.MapConstants.baseUrl
import com.trifonov.indoor_navigation.mapView.MapConstants.dataPath
import com.trifonov.indoor_navigation.mapView.MapConstants.unzipPath
import net.lingala.zip4j.ZipFile
import java.io.File
import java.lang.Float.max
import java.security.MessageDigest
import kotlin.math.roundToInt


/**
 * Класс для работы с файловой системой
 * @Param [contextActivity] для работы с сервисами
 * @Param [location] название локации для подгрузки
 * @Constructor Создаёт FileHelper для работы с системой
 */
class FileHelper(
    private val activity: Activity,
    val location: Location,
    private val downloadView: View? = null,
    private val dialog: AlertDialog? = null,
) {

    /**
     * Скачивает архив с тайлами и графом навигации
     * @Param [uRl] идентификатор документа для скачивания
     * @See [FileHelper.convertUrl] метод для дополнения ссылки
     * @return Boolean - успешная/безуспешная загрузка
     */

    private var dataPathTmp = dataPath
    private var unzipPathTmp = unzipPath
    private var downloading = false
    private var checkConnectionFlag = true

    @SuppressLint("Range")
    internal fun fileDownload(location: Location): Boolean {
        var returning = false
        dataPathTmp = dataPath+"${location.dataUrl}/"
        unzipPathTmp = unzipPath+"${location.dataUrl}/"
        startThreadConnection()
        val url = Uri.parse(convertUrl(location.id))
        val request = DownloadManager.Request(url)
            .setTitle("${location.dataUrl}.zip")
            .setDescription("Downloading")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setDestinationInExternalFilesDir(
                activity,
                "locations/${location.dataUrl}",
                "${location.dataUrl}.zip"
            )
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
        val downloadManager =
            activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloading = true
        val downloadId = downloadManager.enqueue(request)
        val query = DownloadManager.Query().setFilterById(downloadId)
        activity.runOnUiThread {
            downloadView?.findViewById<Button>(R.id.cancel_button)?.setOnClickListener {
                checkConnectionFlag = false
                downloading = false
                downloadManager.remove(downloadId)
                dialog?.cancel()
            }
        }
        while (downloading) {
            val cursor = downloadManager.query(query)
            cursor.moveToFirst()
            var status: Int = 0
            var bytes_downloaded: Int = 0
            var bytes_total: Int = 0
            try {
                status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                bytes_downloaded = cursor.getInt(
                    cursor
                        .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                )
                bytes_total =
                    cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                activity.runOnUiThread {
                    downloadView?.findViewById<TextView>(R.id.progress)?.text =
                        (max(bytes_downloaded.toFloat() / bytes_total.toFloat() * 100 - 1, 0f)).roundToInt().toString() + "%"
                    downloadView?.findViewById<LinearProgressIndicator>(R.id.progressBar)?.setProgressCompat(
                        (max(bytes_downloaded.toFloat() / bytes_total.toFloat() * 100 - 1, 0f)).roundToInt(), true)
                }
            }
            catch (e: Exception){
                activity.runOnUiThread {
                    checkConnectionFlag = false
                    downloading = false
                    downloadManager.remove(downloadId)
                    dialog?.cancel()
                }
            }
            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                activity.runOnUiThread {
                    downloadView?.findViewById<TextView>(R.id.partLoading)?.text = activity.resources.getString(R.string.unzip)
                }
                checkConnectionFlag = false
                println("Hash file ${calculateSHA256("$dataPathTmp${location.dataUrl}.zip")}")
                println("Required hash ${location.hashSum}")
                if (calculateSHA256("$dataPathTmp${location.dataUrl}.zip") == location.hashSum) {
                    if (unzip(location.dataUrl) == true) {
                        // Если нет map.json возвращаем false
                        if (!File("$unzipPathTmp/map.json").exists()) returning = true
                        else {
                            val locationsFile = File("$dataPath/locations.json")
                            val gson = Gson()
                            if (locationsFile.exists()) {
                                val locations =
                                    gson.fromJson(locationsFile.readText(), Locations::class.java)
                                locations.locations.add(location)
                                locationsFile.writeText(gson.toJson(locations))
                            } else {
                                locationsFile.writeText(gson.toJson(Locations(mutableListOf(location))))
                            }
                            activity.runOnUiThread {
                                downloadView?.findViewById<TextView>(R.id.progress)?.text = "100%"
                                downloadView?.findViewById<LinearProgressIndicator>(R.id.progressBar)?.progress =
                                    100
                                activity.findViewById<TextView>(R.id.current_location).text =
                                    location.name
                                Toast.makeText(activity, "Установка успешна", Toast.LENGTH_SHORT)
                                    .show()
                            }
                            returning = true
                        }
                    }
                }
                else{
                    activity.runOnUiThread {
                        Toast.makeText(activity, "Перезагрука локации", Toast.LENGTH_SHORT).show()
                    }
                    cursor.close()
                    File(dataPathTmp).deleteRecursively()
                    returning = fileDownload(location)
                }
            }
            cursor.close()
        }
        return returning
    }


    /**
    *   Создает новый поток, в котором проверяет наличие Интернет соединения, если нет, то уведомляет об этом пользователя
    * */
    private fun startThreadConnection(){
        Thread{
            // Проверяем есть ли интернет
            while (checkConnectionFlag) {
                val cm =
                    activity.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
                val netInfo = cm.activeNetworkInfo
                if (netInfo != null && netInfo.isConnectedOrConnecting) {
                    activity.runOnUiThread {
                        downloadView?.findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.connectionError)!!.visibility =
                            INVISIBLE
                        downloadView.findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.download_progress)!!.visibility =
                            VISIBLE
                    }
                } else {
                    activity.runOnUiThread {
                        downloadView?.findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.download_progress)!!.visibility =
                            INVISIBLE
                        downloadView?.findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.connectionError)!!.visibility =
                            VISIBLE
                    }
                }
            }
        }.start()
    }

    /**
     * @Param [id] идентификатор документа на google Drive
     * @See [FileHelper.fileDownload]
     * */
    private fun convertUrl(id: Int): String {
        return "${baseUrl}locations/${id}/tiles"
    }

    /**
     * Получает строковое представление карты из файла
     * @Param [name] название локации для отображения
     * @Return карта в строковом представлении
     */
    internal fun getJsonMap(location: Location): String {
        if (checkStorageLocation(location.dataUrl)) {
            return try {
                return File("$dataPath${location.dataUrl}/map.json").readText()
            } catch (e: Exception) {
                println(e.message)
                "empty location"
            }
        } else {
            return if (fileDownload(location)) File("$dataPath${location.dataUrl}/map.json").readText()
            else "empty location"
        }
        return "empty location"
    }

    /**
     * Метод для разархивации карты и тайлов
     * @Param [fileName] название локации для разархивации
     * @Return успех / провал
     * */
    private fun unzip(fileName: String): Boolean? {
        return try {
            println("Unzip")
            val zipFile = ZipFile("$dataPathTmp$fileName.zip")
            zipFile.extractAll(unzipPathTmp)
            File("$dataPathTmp$fileName.zip").delete()
            if (!downloading){
                File(dataPathTmp).deleteRecursively()
                return false
            }
            downloading = false
            true
        } catch (e: Exception) {
            println(e.message)
            false
        }
    }


    fun calculateSHA256(filePath: String): String {
        val bytes = File(filePath).readBytes()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }


    /**
     * Статический метод для проверки наличия локации в файлах устройства
     * @Param [locationName] название локации для карты
     * @Return true при наличии файла в памяти
     * */
    companion object {
        internal fun checkStorageLocation(locationName: String): Boolean {
            try {
                for (file in File("$unzipPath/$locationName/").listFiles()!!) {
                    if (file.name == "map.json") {
                        println("$locationName is found")
                        return true
                    }
                }
            } catch (e: Exception) {
                println("$locationName not found")
            }
            return false
        }

        internal fun deleteLocation(locationName: String){
            if (checkStorageLocation(locationName)){
                File("$dataPath$locationName").deleteRecursively()
            }
        }
    }
}