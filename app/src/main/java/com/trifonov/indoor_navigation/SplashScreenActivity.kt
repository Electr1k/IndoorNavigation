package com.trifonov.indoor_navigation


import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.trifonov.indoor_navigation.common.LocationData
import com.trifonov.indoor_navigation.map.MapConnector
import com.trifonov.indoor_navigation.map.MapConstants

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    private var splash: ImageView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen_fragment)
        val w: Window = window
        w.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        splash = findViewById(R.id.splash)
        splash?.alpha = 0f
        splash?.animate()?.setDuration(2000)?.alpha(1f)?.withEndAction {

            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            MapConstants.mapConnector = MapConnector(this, findNavController(R.id.nav_host_fragment_activity_bottom_navigation))
            val locationData = LocationData(this)
            val currentLocationId = locationData.getCurrentLocation()
            if (currentLocationId != -1){
                MapConstants.mapConnector.setLocation(locationData.getLocationById(currentLocationId)!!)
            }
            finish()
        }
    }
}