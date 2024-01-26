package com.trifonov.indoor_navigation

import android.app.AlertDialog
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
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
        val locationData = LocationData(this)
        Handler(Looper.getMainLooper()).postDelayed({initialAlertDialog(locationData.getLocationById(0)!!)}, 2000)
    }

    /**
     * Инициализирует и запускает Alert Dialog с загрузкой локации
     * */
    internal fun initialAlertDialog(location: LocationEntity){
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
            val downloadView = layoutInflater.inflate(R.layout.download_view, null)
            if (!isDownload){
                viewGroup.removeView(confirmView)
                viewGroup.addView(downloadView)
            }
            Thread {
                if (mapConnector.initialMapView(location.dataUrl, downloadView, dialog)) {
                    startNode++
                    this.runOnUiThread {
                        mapConnector.updatePath(136)
                        dialog.cancel()
                    }
                }
            }.start()
        }
        confirmView.findViewById<Button>(R.id.negativeBtn).setOnClickListener {
            dialog.cancel()
        }
        viewGroup.addView(confirmView)
        dialog.show()
    }

}