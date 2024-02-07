package com.trifonov.indoor_navigation

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.DialogInterface
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.trifonov.indoor_navigation.common.LocationData
import com.trifonov.indoor_navigation.common.LocationEntity
import com.trifonov.indoor_navigation.databinding.ActivityMainBinding
import com.trifonov.indoor_navigation.map.FileHelper.Companion.checkStorageLocation
import com.trifonov.indoor_navigation.map.MapConstants.mapConnector
import com.trifonov.indoor_navigation.map.MapConstants.startNode

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
        mNavController = findNavController(R.id.nav_host_fragment_activity_bottom_navigation)
        navView.setupWithNavController(mNavController)
        mNavController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id != R.id.head) findViewById<CardView>(R.id.cardNav).visibility = View.INVISIBLE
            else findViewById<CardView>(R.id.cardNav).visibility = View.VISIBLE
        }
        //val locationData = LocationData(this)

        if (!hasPermissions()) {
            requestPermissions()
        } else {
            startBluetoothScanning()
        }
        //Handler(Looper.getMainLooper()).postDelayed({initialAlertDialog(locationData.getLocationById(0)!!)}, 2000)
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
    internal fun initialAlertDialog(location: LocationEntity, locationData: LocationData){
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

}