package edu.shape.dicelegend

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.gson.Gson
import edu.shape.dicelegend.databinding.ActivityMainBinding
import edu.shape.dicelegend.ui.home.HomeFragment
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.time.LocalDateTime
import java.util.Date
import java.util.Objects
import java.util.Random
import java.util.concurrent.CountDownLatch

class MainActivity : AppCompatActivity() {
    enum class GameStatus {
        PendingStart,
        NewGame,
        ShakeDice,
        OpenUp,
    }

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
    data class currentWeather (
        val temperature: Float,
        val weathercode: Int,
        val windspeed: Float,
        val winddirection: Int,
    )
    data class WeatherResult(val current_weather: currentWeather)
    private lateinit var binding: ActivityMainBinding

    private var playerKey: String? = null
    lateinit var client: OkHttpClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
                R.id.navigation_home, R.id.navigation_dice_history,))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val sharedPrefs = getPreferences(MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        playerKey = sharedPrefs.getString("player_key", null)
        if(playerKey.isNullOrBlank()){
            val key = bindPlayer()
            editor.putString("player_key", key)
            editor.apply()
            playerKey = sharedPrefs.getString("player_key", null)
        }

        val userCodeText: TextView = findViewById(R.id.txtUserCode)
        userCodeText.text = "User Code: $playerKey"
        Toast.makeText(applicationContext, "Your key is $playerKey!", Toast.LENGTH_SHORT).show()

        client = OkHttpClient()
        getWeather()
    }

    fun bindPlayer(): String{
        val random = Random()
        val sb = StringBuilder(6)
        for (i in 0 until 6) {
            val randomInt = random.nextInt(62)
            val char = when {
                randomInt < 10 -> (randomInt + 48).toChar() // 0-9
                randomInt < 36 -> (randomInt + 55).toChar() // A-Z
                else -> (randomInt + 61).toChar() // a-z
            }
            sb.append(char)
        }
        return sb.toString()
    }

    fun getWeather() {
        val request = Request.Builder().url("https://api.open-meteo.com/v1/forecast?latitude=52.52&longitude=13.41&current_weather=true&hourly=temperature_2m,relativehumidity_2m,windspeed_10m").build()
        val latch = CountDownLatch(1)

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("Weather API", e.toString())
                latch.countDown()
            }

            override fun onResponse(call: Call, response: Response) {
                var resStr = response.body?.string()
                val result = Gson().fromJson(resStr, WeatherResult::class.java)
                Log.d("Weather API", "response: ${result.current_weather}")
                val weather = WeatherCode.values().firstOrNull{it.code == result.current_weather.weathercode}
                Log.d("Weather API", "response: ${weather?.description}")
                val weatherText: TextView = findViewById(R.id.weather)
                weatherText.text = "Weather: ${weather?.description}"
                latch.countDown()
            }
        })

        latch.await()
    }
}