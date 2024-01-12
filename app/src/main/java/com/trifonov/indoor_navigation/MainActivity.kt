package com.trifonov.indoor_navigation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.cardview.widget.CardView
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.trifonov.indoor_navigation.databinding.ActivityMainBinding
import com.trifonov.indoor_navigation.map.MapConstants.startNode
import ovh.plrapps.mapview.MapView
import ovh.plrapps.mapview.MapViewConfiguration
import ovh.plrapps.mapview.core.TileStreamProvider

import java.lang.Exception

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
        println("Activity created")
    }

}