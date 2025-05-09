package com.example.uc14front

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("api/local/{latitude}/{longitude}")
    fun getLocationData(
        @Path("latitude") latitude: Double,
        @Path("longitude") longitude: Double
    ): Call<WeatherResponse>
}

// Classes para a resposta (pode ser no mesmo arquivo ou em um novo)
data class WeatherResponse(
    val localInfo: LocalInfo,
    val previsao: Previsao
)

data class LocalInfo(
    val display_name: String?,
    val address: Address?
)

data class Address(
    val city: String?,
    val state: String?,
    val country: String?
)

data class Previsao(
    val current: CurrentWeather
)

data class CurrentWeather(
    val temperature: Double,
    val weather_code: Int,
    val wind_speed_10m: Double
)