package com.trifonov.indoor_navigation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNav : BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bottomNav = findViewById(R.id.bottomNavigationView)


        bottomNav.setOnItemSelectedListener {
            it.isEnabled = true
            when (it.itemId) {
                R.id.head -> {
                    println("Главная")
                    true
                }
                R.id.search -> {
                    println("Поиск")
                    true
                }
                R.id.route -> {
                    println("Маршрут")
                    true
                }
                R.id.scan -> {
                    println("Сканер")
                    true
                }
                else -> {
                    println("Хз")
                    false
                }
            }
        }
    }
}