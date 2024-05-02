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
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import coil.ImageLoader
import coil.request.CachePolicy
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.trifonov.indoor_navigation.common.LocationData
import com.trifonov.indoor_navigation.common.getTitleStreamProvider
import com.trifonov.indoor_navigation.common.getTitleStreamProviderFromAssets
import com.trifonov.indoor_navigation.common.loadFromString
import com.trifonov.indoor_navigation.data.dto.Location
import com.trifonov.indoor_navigation.data.dto.Locations
import com.trifonov.indoor_navigation.databinding.ActivityMainBinding
import com.trifonov.indoor_navigation.di.ApiModule
import com.trifonov.indoor_navigation.mapView.FileHelper
import com.trifonov.indoor_navigation.mapView.FileHelper.Companion.checkStorageLocation
import com.trifonov.indoor_navigation.mapView.MapConstants
import com.trifonov.indoor_navigation.mapView.MapConstants.dataPath
import com.trifonov.indoor_navigation.mapView.CustomMap
import com.trifonov.indoor_navigation.mapView.CustomViewListener
import com.trifonov.indoor_navigation.mapView.MapData
import com.trifonov.indoor_navigation.mapView.RouteService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.lingala.zip4j.ZipFile
import java.io.File
import java.util.Date

class MainActivity : AppCompatActivity() {

    private val bluetoothAdapter: BluetoothAdapter? by lazy { BluetoothAdapter.getDefaultAdapter() }
    private val scanFilters: List<ScanFilter> = listOf(ScanFilter.Builder().setDeviceName("SFedU Beacon").build())
    private val scanSettings: ScanSettings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()

    lateinit var mapView: CustomMap
    lateinit var mapData: MapData
    var levelNumber = "1"
    var streamFromAssets = false

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


    lateinit var imageLoader: ImageLoader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        mapView = findViewById(R.id.mapView)
        val navView: BottomNavigationView = mBinding.bottomNavigationView
        imageLoader = ImageLoader.Builder(this)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.DISABLED)
            .build()
        CoroutineScope(Dispatchers.IO).launch {
            this@MainActivity.checkUpdateLocations()
        }
        mNavController = findNavController(R.id.nav_host_fragment_activity_bottom_navigation)
        navView.setupWithNavController(mNavController)
        mNavController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id != R.id.head){
                findViewById<CardView>(R.id.cardNav).visibility = View.INVISIBLE
            }
            else{
                findViewById<CardView>(R.id.cardNav).visibility = View.VISIBLE
            }
        }

        mapView.setListener(object: CustomViewListener{
            override fun onLevelChanged(newValue: String) {
                levelNumber = newValue
                configureMap()
            }

            override fun onTap(view: View, id: Int) {
                val dot = mapData.dotList.find { id == it.getId() }
                if (dot?.getName() != "") {
                    val bundle = Bundle()
                    bundle.putInt("id", dot?.getId() ?: -1)
                    while (mNavController.currentDestination!!.id != R.id.head){
                        mNavController.popBackStack()
                    }
                    mNavController.navigate(R.id.action_head_to_audience, bundle)
                }
            }
        })

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
        imageLoader.shutdown()
        System.gc() // Запросить сборку мусора
        try {
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
        } catch (se: SecurityException) { }
        super.onDestroy()
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
            val confirmView = layoutInflater.inflate(R.layout.confirm_view, null)
            val viewGroup = alertDialogView as ViewGroup
            val isDownload = checkStorageLocation(location.dataUrl)
            confirmView.findViewById<TextView>(R.id.confirmText).text = if (isDownload) "Вы находитесь в ${location.name}, хотите переключиться на карту локации?" else "Карта локации ${location.name} не установлена на Ваше устройство, выполнить её загрузку?"
            confirmView.findViewById<Button>(R.id.positiveBtn).setOnClickListener{
                startDownload(location, dialog, viewGroup, confirmView, isDownload)
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

    private fun startDownload(
        location: Location,
        dialog: AlertDialog,
        viewGroup: ViewGroup,
        confirmView: View? = null,
        isDownload: Boolean = false,
        hasCancel: Boolean = true
    ){
        isLocationFound = true
        val downloadView = layoutInflater.inflate(R.layout.download_view, null)
        if (!isDownload){
            viewGroup.removeView(confirmView)
            if (!hasCancel){
                val cancelBTN = downloadView.findViewById<Button>(R.id.cancel_button)
                cancelBTN.visibility = View.INVISIBLE
                cancelBTN.isClickable = false
            }
            viewGroup.addView(downloadView)
        }
        Thread {
            isDialogEnable = false
            val fh = FileHelper(this, location, downloadView, dialog)
            val json = fh.getJsonMap(location)
            if (json != "empty location") {
                mapData = loadFromString(
                    zoomLevelCount = (File("${dataPath}${location.dataUrl}/tiles1").listFiles()?.size ?: 0) - 1,
                    json = File("${dataPath}${location.dataUrl}/map.json").readText(),
                    applicationContext = applicationContext,
                    getTileStream = getTitleStreamProvider(location.dataUrl, levelNumber)
                )
                runOnUiThread {
                    mapView.setMap(mapData, needDestroy = true)
                    dialog.cancel()
                    val ld = LocationData(this)
                    ld.setCurrentLocation(location.id)
                }
            }
        }.start()
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
                val directory = File("${MapConstants.unzipPath}/${location.dataUrl}")
                val date = Date(directory.lastModified())
                println("Date install $date")
                println("Date update ${location.updateTime}")
                if (date < location.updateTime){
                    println("Reinstall location ${location.dataUrl}")
                    if (currentLocation == location.id){
                        runOnUiThread {
                            val builder = AlertDialog.Builder(this)
                            val dialog = builder
                                .create()
                            dialog.window?.setBackgroundDrawable(
                                AppCompatResources.getDrawable(
                                    this,
                                    R.drawable.dialog_rounded_background
                                )
                            )
                            dialog.setCanceledOnTouchOutside(false)
                            val alertDialogView = dialog.window!!.decorView
                            val confirmView = layoutInflater.inflate(R.layout.confirm_view, null)
                            val viewGroup = alertDialogView as ViewGroup
                            confirmView.findViewById<TextView>(R.id.confirmText).text =
                                "Найдено обновление для текущей локации. Обновить сейчас?"
                            confirmView.findViewById<Button>(R.id.positiveBtn).setOnClickListener {
                                directory.deleteRecursively()
                                startDownload(location, dialog, viewGroup, confirmView, hasCancel = false)
                            }
                            confirmView.findViewById<Button>(R.id.negativeBtn).setOnClickListener {
                                dialog.cancel()
                            }
                            viewGroup.addView(confirmView)
                            dialog.show()
                        }
                    }
                    else{
                        directory.deleteRecursively()
                        silentInstall(location)
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

    fun configureMap() {
        mapData.tileStreamProvider = if (streamFromAssets) getTitleStreamProviderFromAssets(this, levelNumber) else {
            val locationData = LocationData(this)
            getTitleStreamProvider(locationData.getLocationById(locationData.getCurrentLocation())!!.dataUrl, levelNumber)
        }
        mapView.setMap(mapData = mapData, true, levelNumber = levelNumber, addPath = true)
    }


    fun openRouteBar(){
        val routeBar = findViewById<CardView>(R.id.routeBar)
        val closeIcon = findViewById<ImageView>(R.id.close_path)
        closeIcon.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val dialog = builder.create()
            dialog.window?.setBackgroundDrawable(
                AppCompatResources.getDrawable(
                    this,
                    R.drawable.dialog_rounded_background
                )
            )
            dialog.setCanceledOnTouchOutside(false)
            val alertDialogView = dialog.window!!.decorView
            val confirmView = layoutInflater.inflate(R.layout.confirm_view, null)
            val viewGroup = alertDialogView as ViewGroup
            confirmView.findViewById<TextView>(R.id.confirmText).text =
                "Вы уверены, что хотите завершить маршрут?"
            confirmView.findViewById<Button>(R.id.positiveBtn).setOnClickListener {
                dialog.cancel()
                val roteService = RouteService.getInstance(mapView)
                roteService.removePath()
                closeRouteBar()
            }
            confirmView.findViewById<Button>(R.id.negativeBtn).setOnClickListener {
                dialog.cancel()
            }
            viewGroup.addView(confirmView)
            dialog.show()
        }
        routeBar.visibility = View.VISIBLE
    }

    fun closeRouteBar(){
        val routeBar = findViewById<CardView>(R.id.routeBar)
        routeBar.visibility = View.GONE
    }
}