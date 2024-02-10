package com.trifonov.indoor_navigation.fragment

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.trifonov.indoor_navigation.R
import com.trifonov.indoor_navigation.common.LocationData

class HeadFragment: Fragment() {

    private var isEnable = true
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.head_fragment, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isEnable = true
        val locationData = LocationData(requireContext())
        var currentLocationId = locationData.getCurrentLocation()
//        if (currentLocationId == -1){
//            currentLocationId = 0
//            locationData.setCurrentLocation(currentLocationId)
//        }
        val btn = view.findViewById<TextView>(R.id.current_location)
        btn.text = if (currentLocationId != -1)locationData.getLocationById(currentLocationId)!!.name else "Демо локация"
        val cardView =  view.findViewById<CardView>(R.id.card_location)
        cardView.setOnClickListener {
            val scaleUpX = ObjectAnimator.ofFloat(cardView, "scaleX", 1.1f)
            val scaleUpY = ObjectAnimator.ofFloat(cardView, "scaleY", 1.1f)
            scaleUpX.duration = 150
            scaleUpY.duration = 150

            // Анимация уменьшения масштаба при отпускании
            val scaleDownX = ObjectAnimator.ofFloat(cardView, "scaleX", 1f)
            val scaleDownY = ObjectAnimator.ofFloat(cardView, "scaleY", 1f)
            scaleDownX.duration = 150
            scaleDownY.duration = 150

            // Создаем композицию анимаций
            val scaleUp = AnimatorSet().apply {
                play(scaleUpX).with(scaleUpY)
            }

            val scaleDown = AnimatorSet().apply {
                play(scaleDownX).with(scaleDownY)
            }

            // Запускаем анимации при нажатии и отпускании
            scaleUp.start()
            scaleUp.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    scaleDown.start()
                }
            })
            scaleDown.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    if (isEnable) {
                        Navigation.findNavController(view).navigate(R.id.location)
                        isEnable = false
                    }
                }
            })
        }
    }
}