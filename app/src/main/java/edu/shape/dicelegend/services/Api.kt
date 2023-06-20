package edu.shape.dicelegend.services

import android.util.Log
import android.widget.TextView
import com.google.gson.Gson
import edu.shape.dicelegend.MainActivity
import edu.shape.dicelegend.R
import edu.shape.dicelegend.dto.Dice
import edu.shape.dicelegend.dto.WeatherDto
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.CountDownLatch

class Api {
    companion object {
        private var client: OkHttpClient = OkHttpClient()
        enum class WeatherCode(val code: Int, val description: String){
            ClearSky(0, "Clear Sky"),
            MainlyClear(1, "Mainly clear"),
            PartlyCloudy(2, "Partly cloudy"),
            Overcast(3, "Overcast"),
            Fog(45, "Fog"),
            DepositingRimeFog(48, "Depositing rime fog"),
            DrizzleLight(51, "Drizzle: Light"),
            DrizzleModerate(53, "Drizzle: moderate"),
            DrizzleDenseIntensity(55, "Drizzle: dense intensity"),
            FreezingDrizzleLight(56, "Freezing Drizzle: Light"),
            FreezingDrizzleDenseIntensity(57, "Freezing Drizzle: dense intensity"),
            RainSlight(61, "Rain: Slight"),
            RainModerate(63, "Rain: moderate"),
            RainHeavyIntensity(65, "Rain: heavy intensity"),
            FreezingRainLight(66, "Freezing Rain: Light"),
            FreezingRainHeavyIntensity(67, "Freezing Rain: heavy intensity"),
            SnowFallSlight(71, "Snow fall: Slight"),
            SnowFallModerate(73, "Snow fall: moderate"),
            SnowFallHeavyIntensity(75, "Snow fall: heavy intensity"),
            SnowGrains(77, "Snow grains"),
            RainShowersSlight(80, "Rain showers: Slight"),
            RainShowersModerate(81, "Rain showers: moderate"),
            RainShowersViolent(82, "Rain showers: violent"),
            SnowShowersSlight(85, "Snow showers slight"),
            SnowShowersHeavy(86, "Snow showers heavy"),
            ThunderstormSlightOrModerate(95, "Thunderstorm: Slight or moderate"),
            ThunderstormWithSlight(96, "Thunderstorm with slight"),
            ThunderstormWithHeavyHail(99, "Thunderstorm with heavy hail"),
        }

        fun getDice(): Int {
            val request = Request.Builder().url("https://dice-api.genzouw.com/v1/dice").build()
            val latch = CountDownLatch(1)
            var dice = 0

            client?.newCall(request)?.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.d("HKT", e.toString())
                    latch.countDown()
                }

                override fun onResponse(call: Call, response: Response) {
                    var resStr = response.body?.string()
                    val result = Gson().fromJson(resStr, Dice.ApiResult::class.java)

                    dice = result.dice
                    Log.d("HKT", "response: $dice")
                    latch.countDown()
                }
            })

            latch.await()

            return dice
        }

        fun getWeather(): String {
            val request = Request.Builder().url("https://api.open-meteo.com/v1/forecast?latitude=52.52&longitude=13.41&current_weather=true&hourly=temperature_2m,relativehumidity_2m,windspeed_10m").build()
            val latch = CountDownLatch(1)
            var weatherResult: String = ""

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.d("Weather API", e.toString())
                    latch.countDown()
                }

                override fun onResponse(call: Call, response: Response) {
                    var resStr = response.body?.string()
                    val result = Gson().fromJson(resStr, WeatherDto.WeatherResult::class.java)
                    Log.d("Weather API", "response: ${result.current_weather}")
                    val weather = WeatherCode.values().firstOrNull{it.code == result.current_weather.weathercode}
                    Log.d("Weather API", "response: ${weather?.description}")
                    weatherResult = weather?.description.toString()
                    latch.countDown()
                }
            })

            latch.await()
            return weatherResult
        }
    }
}