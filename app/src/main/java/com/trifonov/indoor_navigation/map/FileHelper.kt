/**
 * Класс для работы с файловой системой
 * @Author Братусев Денис
 * @Since 01.06.2023
 * @Version 1.0
 * */
package com.trifonov.indoor_navigation.map

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.widget.Toast
import androidx.core.content.ContextCompat.registerReceiver
import androidx.navigation.fragment.findNavController
import com.trifonov.indoor_navigation.R
import com.trifonov.indoor_navigation.SplashScreen
import com.trifonov.indoor_navigation.map.MapConstants.dataPath
import com.trifonov.indoor_navigation.map.MapConstants.unzipPath
import net.lingala.zip4j.ZipFile
import java.io.File
import java.util.concurrent.CountDownLatch


/**
 * Класс для работы с файловой системой
 * @Param [context] для работы с сервисами
 * @Param [locationName] название локации для подгрузки
 * @Constructor Создаёт FileHelper для работы с системой
 */
class FileHelper(private val context: Context, private val fragment: SplashScreen, val locationName: String) {

    /**
     * Скачивает архив с тайлами и графом навигации
     * @Param [uRl] идентификатор документа для скачивания
     * @See [FileHelper.convertUrl] метод для дополнения ссылки
     * @See [FileHelper.onComplete] ресивер для обработки конца загрузки
     */

    private var dataPathTmp = dataPath
    private var unzipPathTmp = unzipPath

    internal fun fileDownload(uRl: String) {
        dataPathTmp += "$locationName/"
        unzipPathTmp += "$locationName/"
        val latch = CountDownLatch(1) // Создаем CountDownLatch
        if (!checkStorageLocation()) {
            println("Загрузка файла: ${convertUrl(uRl)}")
            val request = DownloadManager.Request(Uri.parse(convertUrl(uRl)))
                .setTitle("$locationName.zip")
                .setDescription("Downloading")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalFilesDir(
                    context,
                    "locations/$locationName",
                    "$locationName.zip"
                )
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = downloadManager.enqueue(request)

            val intentFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            val onComplete = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    println("Загружено")
                    val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    if (id == downloadId) { // Проверяем, что это наша загрузка
                        if (unzip(locationName) == true) {
                            // Здесь можно обработать завершение загрузки
                            latch.countDown()
                        }
                        context?.unregisterReceiver(this) // Отменяем регистрацию ресивера
                    }
                }
            }
            context.registerReceiver(onComplete, intentFilter)
            latch.await() // Блокируем поток до вызова onComplete
        } else {
            fragment.findNavController().navigate(R.id.action_splash_to_head)
        }
    }

    /**
     * @Param [onComplete] ресивер для обработки конца загрузки
     * @See [FileHelper.fileDownload]
     * */
//    private val onComplete = object : BroadcastReceiver() {
//        override fun onReceive(context: Context?, intent: Intent?) {
//            if(unzip(locationName) == true){
//
//            }
//        }
//    }

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
                e.message.toString()
            }
        } else {
            //TODO: Добавить обработку отсутствия карты
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
            val zipFile = ZipFile("$dataPathTmp$fileName.zip")
            zipFile.extractAll(unzipPathTmp)
            File("$dataPathTmp$fileName.zip").delete()
            fragment.findNavController().navigate(R.id.action_splash_to_head)
            Toast.makeText(context, "Установка успешна", Toast.LENGTH_SHORT).show()
            true
        } catch (e: Exception) {
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
    private fun checkStorageLocation(): Boolean {
        try {
            for (file in File(unzipPath).listFiles()!!) {
                if (file.name === locationName){
                    println("${file.name} $locationName")
                    return true
                }
            }
            Toast.makeText(context, "Локация не найдена", Toast.LENGTH_SHORT).show()
        }catch (e: Exception){
        }
        return false
    }
}