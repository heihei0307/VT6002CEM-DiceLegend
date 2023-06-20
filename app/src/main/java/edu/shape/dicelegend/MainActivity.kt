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
import edu.shape.dicelegend.services.Api.Companion.getWeather
import edu.shape.dicelegend.ui.home.HomeFragment
import edu.shape.dicelegend.utils.CommonUtil.Companion.bindPlayer
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
        val weather = getWeather()
        val weatherText: TextView = findViewById(R.id.weather)
        weatherText.text = "Weather: $weather"
    }


}