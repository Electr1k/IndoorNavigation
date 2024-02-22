package com.trifonov.indoor_navigation.data

import com.trifonov.indoor_navigation.data.dto.Location
import com.trifonov.indoor_navigation.data.dto.Locations
import retrofit2.http.GET

interface ServerApi {

    @GET("locations")
    suspend fun getLocations(): Locations
}