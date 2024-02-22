package com.trifonov.indoor_navigation.data.dto

import java.util.Date

data class Location(
    val id: Int,
    val name: String,
    val description: String,
    val address: String,
    val dataUrl: String,
    val updateTime: Date,
    val isVisible: Boolean,
    val hashSum: Int
)
