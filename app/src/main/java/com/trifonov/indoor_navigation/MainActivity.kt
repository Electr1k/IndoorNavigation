package com.trifonov.indoor_navigation

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DownloadManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.trifonov.indoor_navigation.common.LocationData
import com.trifonov.indoor_navigation.data.dto.Location
import com.trifonov.indoor_navigation.data.dto.Locations
import com.trifonov.indoor_navigation.databinding.ActivityMainBinding
import com.trifonov.indoor_navigation.di.ApiModule
import com.trifonov.indoor_navigation.map.FileHelper.Companion.checkStorageLocation
import com.trifonov.indoor_navigation.map.MapConstants
import com.trifonov.indoor_navigation.map.MapConstants.dataPath
import com.trifonov.indoor_navigation.map.MapConstants.mapConnector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.lingala.zip4j.ZipFile
import java.io.File
import java.util.Date

class MainActivity : AppCompatActivity() {

    private val bluetoothAdapter: BluetoothAdapter? by lazy { BluetoothAdapter.getDefaultAdapter() }
    private val scanFilters: List<ScanFilter> = listOf(ScanFilter.Builder().setDeviceName("SFedU Beacon").build())
    private val scanSettings: ScanSettings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()


    private val permissions = arrayOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    private val requestCodePermissions = 42

    private var isLocationFound = false
    private var isDialogEnable = false

    private lateinit var mBinding: ActivityMainBinding
    private lateinit var mNavController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        val navView: BottomNavigationView = mBinding.bottomNavigationView

        CoroutineScope(Dispatchers.IO).launch {
            this@MainActivity.checkUpdateLocations()
        }
        mNavController = findNavController(R.id.nav_host_fragment_activity_bottom_navigation)
        navView.setupWithNavController(mNavController)
        mNavController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id != R.id.head) findViewById<CardView>(R.id.cardNav).visibility = View.INVISIBLE
            else findViewById<CardView>(R.id.cardNav).visibility = View.VISIBLE
        }

        if (!hasPermissions()) {
            requestPermissions()
        } else {
            startBluetoothScanning()
        }
    }

    private fun findCurrentLocation(macAddress: String) {
        Log.d("MyBluetoothScanner", macAddress)
        val locationData = LocationData(this)
        //TODO: Заменить костыль на сравнение с маками локации
        if("C5:BB:B0:62:5B:F9" == macAddress) initialAlertDialog(locationData.getLocationById(0)!!, locationData)
    }

    private fun hasPermissions(): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, permissions, requestCodePermissions)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == requestCodePermissions) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                startBluetoothScanning()
            } else {
                Log.e("BluetoothScanner", "Permission denied.")
            }
        }
    }

    private fun startBluetoothScanning() {
        try {
            bluetoothAdapter?.bluetoothLeScanner?.startScan(scanFilters, scanSettings, scanCallback)
        } catch (se: SecurityException) { }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device: BluetoothDevice = result.device
            val macAddress = device.address
            val rssi = result.rssi
            Log.d("MyBluetoothScanner", "Device: $macAddress, RSSI: $rssi")
            if(!isLocationFound) findCurrentLocation(macAddress)
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e("MyBluetoothScanner", "Scan failed with error code $errorCode")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
        } catch (se: SecurityException) { }
    }

    /**
     * Инициализирует и запускает Alert Dialog с загрузкой локации
     * */
    internal fun initialAlertDialog(location: Location, locationData: LocationData){
        if(!isDialogEnable){
            val builder = AlertDialog.Builder(this)
            val dialog = builder
                .create()
            dialog.window?.setBackgroundDrawable(getDrawable(R.drawable.dialog_rounded_background))
            dialog.setCanceledOnTouchOutside(false)
            val alertDialogView = dialog.window!!.decorView
            val confirmView = layoutInflater.inflate(R.layout.confirm_download, null)
            val viewGroup = alertDialogView as ViewGroup
            val isDownload = checkStorageLocation(location.dataUrl)
            confirmView.findViewById<TextView>(R.id.confirmText).text = if (isDownload) "Вы находитесь в ${location.name}, хотите переключиться на карту локации?" else "Карта локации ${location.name} не установлена на Ваше устройство, выполнить её загрузку?"
            confirmView.findViewById<Button>(R.id.positiveBtn).setOnClickListener{
                isLocationFound = true
                val downloadView = layoutInflater.inflate(R.layout.download_view, null)
                if (!isDownload){
                    viewGroup.removeView(confirmView)
                    viewGroup.addView(downloadView)
                }
                Thread {
                    isDialogEnable = false
                    if (mapConnector.setLocation(location, downloadView, dialog)) {
                        //startNode++
                        this.runOnUiThread {
                            dialog.cancel()
                            locationData.setCurrentLocation(location.id)
                        }
                    }
                }.start()
            }
            confirmView.findViewById<Button>(R.id.negativeBtn).setOnClickListener {
                dialog.cancel()
                isDialogEnable = false
            }
            viewGroup.addView(confirmView)
            dialog.show()
            isDialogEnable = true
        }
    }

    private suspend fun checkUpdateLocations(){
        val locations: Locations
        try {
            locations = ApiModule.provideApi().getLocations()
        }
        catch (e : Exception){
            Log.e("error", e.message.toString())
            return
        }
        val ld = LocationData(this@MainActivity)
        val currentLocation = ld.getCurrentLocation()
        for (location in locations.locations ){
            if (checkStorageLocation(location.dataUrl)){
                val jsonFile = File("${MapConstants.unzipPath}/${location.dataUrl}/map.json")
                val date = Date(jsonFile.lastModified())
                println("Date install $date")
                if (date < location.updateTime){
                    println("Reinstall location ${location.dataUrl}")
                    jsonFile.parentFile?.deleteRecursively()
                    if (silentInstall(location) && currentLocation == location.id){
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "Текущая локация была обновлена", Toast.LENGTH_SHORT).show()
                            mapConnector.setLocation(ld.getLocationById(currentLocation)!!)
                        }
                    }
                }
            }
        }

    }

    @SuppressLint("Range")
    private fun silentInstall(location: Location): Boolean{

        val url = Uri.parse("http://redmine.rdcenter.ru:1777/location/${location.dataUrl}")
        val request = DownloadManager.Request(url)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
            .setDestinationInExternalFilesDir(
                this,
                "locations/${location.dataUrl}",
                "${location.dataUrl}.zip"
            )
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
        val downloadManager =
            this.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)

        val query = DownloadManager.Query().setFilterById(downloadId)
        println("start download")
        var downloading = true;
        while (downloading) {
            val cursor = downloadManager.query(query)
            cursor.moveToFirst()
            try {
                if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                    val unzipFlag = try {
                        println("Unzip")
                        val zipFile = ZipFile("${dataPath+location.dataUrl}/${location.dataUrl}.zip")
                        zipFile.extractAll(dataPath+location.dataUrl)
                        File("${dataPath+location.dataUrl}/${location.dataUrl}.zip").delete()
                        downloading = false
                        println("success")
                        true
                    } catch (e: Exception) {
                        false
                    }

                    if (unzipFlag) {
                        cursor.close()
                        return true
                    }
                }
            }
            catch (e: Exception){
                downloadManager.remove(downloadId)
            }
            cursor.close()
        }
        return false
    }
}