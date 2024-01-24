package com.trifonov.indoor_navigation.common

data class LocationEntity(
    val id: Int,
    val name: String,
    val description: String,
    val address: String,
    val dataUrl: String,
    val updateTime: String,
    val isVisible: Boolean,
    val hashSum: Int
)