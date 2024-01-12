/**
 * Класс для настройки всплывающих над маркером окон
 * @Author Братусев Денис
 * @Since 01.06.2023
 * @Version 1.0
 * */
package com.trifonov.indoor_navigation.map

import android.content.Context
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.OvershootInterpolator
import android.view.animation.ScaleAnimation
import android.widget.RelativeLayout
import android.widget.TextView
import com.trifonov.indoor_navigation.R

/**
 * Класс для настройки всплывающих над маркером окон
 * @Constructor создаёт шаблон окна над маркером
 * @Param context контекст для работы с ресурсами
 */
class MarkerCallout(context: Context) : RelativeLayout(context) {
    private val mTitle: TextView
    private val mSubTitle: TextView

    init {
        View.inflate(context, R.layout.marker_callout, this)
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
}