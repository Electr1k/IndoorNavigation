/**
 * Класс для работы с файловой системой
 * @Author Братусев Денис
 * @Since 01.06.2023
 * @Version 1.0
 * */
package com.trifonov.indoor_navigation.map

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.Context

import android.net.Uri
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.trifonov.indoor_navigation.R
import com.trifonov.indoor_navigation.map.MapConstants.dataPath
import com.trifonov.indoor_navigation.map.MapConstants.unzipPath
import net.lingala.zip4j.ZipFile
import java.io.File
import java.lang.Float.max
import kotlin.math.roundToInt


/**
 * Класс для работы с файловой системой
 * @Param [contextActivity] для работы с сервисами
 * @Param [locationName] название локации для подгрузки
 * @Constructor Создаёт FileHelper для работы с системой
 */
class FileHelper(
    private val activity: Activity,
    private val view: View,
    val locationName: String
) {

    /**
     * Скачивает архив с тайлами и графом навигации
     * @Param [uRl] идентификатор документа для скачивания
     * @See [FileHelper.convertUrl] метод для дополнения ссылки
     * @See [FileHelper.onComplete] ресивер для обработки конца загрузки
     */

    private var dataPathTmp = dataPath
    private var unzipPathTmp = unzipPath
    private var downloading = false

    @SuppressLint("Range")
    internal fun fileDownload(uRl: String) {
        dataPathTmp += "$locationName/"
        unzipPathTmp += "$locationName/"
        val request = DownloadManager.Request(Uri.parse(convertUrl(uRl)))
            .setTitle("$locationName.zip")
            .setDescription("Downloading")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(
                activity,
                "locations/$locationName",
                "$locationName.zip"
            )
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
        val downloadManager =
            activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloading = true
        val downloadId = downloadManager.enqueue(request)
        val query = DownloadManager.Query().setFilterById(downloadId)
        println("Process is run")
        while (downloading) {
            val cursor = downloadManager.query(query)
            cursor.moveToFirst()
            val status: Int =
                cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
            val bytes_downloaded = cursor.getInt(
                cursor
                    .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
            )
            val bytes_total =
                cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
            activity.runOnUiThread {
                view.findViewById<TextView>(R.id.downloadText)?.text =
                    (max(bytes_downloaded.toFloat() / bytes_total.toFloat() * 100 - 1, 0f)).roundToInt().toString()
            }
            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                if (unzip(locationName) == true){
                    activity.runOnUiThread {
                        view.findViewById<TextView>(R.id.downloadText)?.text =
                        100.toString()
                        Toast.makeText(activity, "Установка успешна", Toast.LENGTH_SHORT).show()
                    }
                }
                println("download is Success")
            }
            cursor.close()
        }
    }


    /**
     * @Param [id] идентификатор документа на google Drive
     * @See [FileHelper.fileDownload]
     * */
    private fun convertUrl(id: String): String {
        return "https://drive.google.com/uc?export=download&confirm=no_antivirus&id=$id"
    }

    /**
     * Получает строковое представление карты из файла
     * @Param [name] название локации для отображения
     * @Return карта в строковом представлении
     */
    internal fun getJsonMap(name: String): String {
        if (checkStorageLocation()) {
            return try {
                return File("$dataPath$name/map.json").readText()
            } catch (e: Exception) {
                println()
                e.message.toString()
            }
        } else {
            println("location not found")
            fileDownload("1rq4aFmBEvLCAhXTQ3YPbtaHkoc2_8B8v")
            return File("$dataPath$name/map.json").readText()
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
            downloading = false
            true
        } catch (e: Exception) {
            println(e.message)
            false
        }
    }

    /**
     * Получение количество уровней приближения карты по количеству папок в директории
     * @Param [fileName] название локации для карты
     * @Return количество уровней приближения
     */
    internal fun getLevelCount(fileName: String): Int {
        val directory = File("$unzipPath$locationName/$fileName")
        val files = directory.listFiles()
        return files?.size ?: 0
    }

    /**
     * Метод для проверки наличия локации в файлах устройства
     * @Return true при наличии файла в памяти
     * */
    internal fun checkStorageLocation(): Boolean {
        try {
            println(locationName)
            println(File(unzipPath).listFiles())
            for (file in File(unzipPath).listFiles()!!) {
                println(file.name)
                if (file.name == locationName){
                    return true
                }
            }
            Toast.makeText(activity, "Локация не найдена", Toast.LENGTH_SHORT).show()
        }catch (e: Exception){
            println("Location not found")
        }
        return false
    }
}