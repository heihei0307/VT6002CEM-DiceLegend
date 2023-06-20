package edu.shape.dicelegend.dto

class WeatherDto {
    data class CurrentWeather (
        val temperature: Float,
        val weathercode: Int,
        val windspeed: Float,
        val winddirection: Int,
    )
    data class WeatherResult(val current_weather: CurrentWeather)
}