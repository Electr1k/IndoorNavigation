package com.trifonov.indoor_navigation

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.fragment.findNavController

class SplashScreen : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        Handler(Looper.getMainLooper()).postDelayed({
            findNavController().navigate(R.id.action_splash_to_head, null)
        }, 1500)
        return inflater.inflate(R.layout.splash_screen_fragment, container, false)
    }
}