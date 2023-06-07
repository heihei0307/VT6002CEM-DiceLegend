package edu.shape.dicelegend

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import edu.shape.dicelegend.databinding.ActivityMainBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Objects
import java.util.Random
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {
    enum class GameStatus {
        NewGame,
        ShakeDice,
        OpenUp,
    }
    private lateinit var binding: ActivityMainBinding

    private var playerKey: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications))
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
        Toast.makeText(applicationContext, "Your key is $playerKey!", Toast.LENGTH_SHORT).show()
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
}