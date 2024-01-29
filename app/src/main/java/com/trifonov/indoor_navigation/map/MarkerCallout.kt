/**
 * Класс для настройки всплывающих над маркером окон
 * @Author Братусев Денис
 * @Since 01.06.2023
 * @Version 1.0
 * */
package com.trifonov.indoor_navigation.map

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.AnimationUtils
import android.view.animation.OvershootInterpolator
import android.view.animation.ScaleAnimation
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.trifonov.indoor_navigation.R

/**
 * Класс для настройки всплывающих над маркером окон
 * @Constructor создаёт шаблон окна над маркером
 * @Param activity контекст для работы с ресурсами
 */
class MarkerCallout(
    private val activity: Activity,
    private val dot: Map.Dot?,
    private val navController: NavController
) : RelativeLayout(activity) {
    private val mTitle: TextView
    private val mSubTitle: TextView
    private val marker: RelativeLayout

    init {
        View.inflate(activity, R.layout.marker_callout, this)
        marker = findViewById(R.id.marker)
        marker.visibility = INVISIBLE
        mTitle = findViewById(R.id.callout_title)
        mSubTitle = findViewById(R.id.callout_subtitle)
    }

    /**
     * Метод для анимирования появления окна
     */
    fun transitionIn() {
        val scaleAnimation = ScaleAnimation(
            0f, 1f, 0f, 1f, Animation.RELATIVE_TO_SELF,
            0.5f, Animation.RELATIVE_TO_SELF, 1f)
        scaleAnimation.interpolator = OvershootInterpolator(1.2f)
        scaleAnimation.duration = 250
        val alphaAnimation = AlphaAnimation(0f, 1f)
        alphaAnimation.duration = 200
        val animationSet = AnimationSet(false)
        animationSet.addAnimation(scaleAnimation)
        animationSet.addAnimation(alphaAnimation)
        startAnimation(animationSet)
    }

    /**
     * Set title
     *
     * @Param title
     */
    fun setTitle(title: String) {
        mTitle.text = title
    }

    /**
     * Set sub title
     *
     * @Param subtitle
     */
    fun setSubTitle(subtitle: String) {
        mSubTitle.text = subtitle
    }

    /**
     * Переопределяем метод прикрепления к окну, для добавления нижнего листа с выбранной точкой
     */
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val bundle = Bundle()
        bundle.putInt("id", dot?.getId() ?: -1)
        while (navController.currentDestination!!.id != R.id.head){
            navController.popBackStack()
        }
        navController.navigate(R.id.action_head_to_scan, bundle)
    }

    /**
     * Переопределяем метод открепления к окну, для закрытия нижнего листа с выбранной точкой
     * лист сворачивается и удаляется при вызове popBackStack (в фрагменте сработает onDestroy)
     */
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        val navigation = activity.findNavController(R.id.nav_host_fragment_activity_bottom_navigation)
        println("Current destination ${navigation.currentDestination}")
        if (navigation.currentDestination?.id == R.id.scan){
            //: TODO: ИЗМЕНИТЬ SCAN НА selectedFragment, когда сканер будет готов
            BottomSheetBehavior.from(activity.findViewById<View>(R.id.bottom_sheet)).state = BottomSheetBehavior.STATE_HIDDEN
            navigation.popBackStack()
        }
    }
}