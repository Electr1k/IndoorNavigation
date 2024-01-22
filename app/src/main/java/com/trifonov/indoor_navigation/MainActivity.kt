package com.trifonov.indoor_navigation

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.trifonov.indoor_navigation.databinding.ActivityMainBinding
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
        Handler(Looper.getMainLooper()).postDelayed({initialAlertDialog()}, 7000)
    }

    /**
     * Инициализирует и запускает Alter Dialog с загрузкой локации
     * */
    private fun initialAlertDialog(){
        val builder = AlertDialog.Builder(this)
        val dialog = builder.setMessage("Вы находитесь в корпусе Г, хотите загрузить карту локации?")
            .setPositiveButton("Да", null)
            .setNegativeButton("Нет"){
                dialog, id ->  dialog.cancel()
            }
            .create()
        dialog.window?.setBackgroundDrawable(getDrawable(R.drawable.dialog_rounded_background))
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
        val positive_btn = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
        positive_btn.setTextColor(getColor(R.color.dark_blue))
        positive_btn.setOnClickListener{
            val alertDialogView = dialog.window!!.decorView
            val downloadView = layoutInflater.inflate(R.layout.download_view, null)
            val viewGroup = alertDialogView as ViewGroup
            viewGroup.addView(downloadView)
            Thread {
                if (mapConnector.initialMapView("Korpus_G", downloadView, dialog)) {
                    startNode++
                    this.runOnUiThread {
                        mapConnector.updatePath(136)
                        dialog.cancel()
                    }
                }
            }.start()
        }
        val negative_btn = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        negative_btn.setTextColor(getColor(R.color.dark_blue))
    }

}