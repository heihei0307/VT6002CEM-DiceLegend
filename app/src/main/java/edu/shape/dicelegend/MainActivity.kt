package edu.shape.dicelegend

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import edu.shape.dicelegend.databinding.ActivityMainBinding
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
    private var sensorManager: SensorManager? = null
    private var acceleration = 0f
    private var currentAcceleration = 0f
    private var lastAcceleration = 0f
    private var status: GameStatus = GameStatus.NewGame
    private var currentAiDice = 0
    private var currentPlayerDice = 0
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

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        Objects.requireNonNull(sensorManager)!!
            .registerListener(sensorListener, sensorManager!!
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)

        acceleration = 10f
        currentAcceleration = SensorManager.GRAVITY_EARTH
        lastAcceleration = SensorManager.GRAVITY_EARTH

        Objects.requireNonNull(sensorManager)!!
            .registerListener(lightSensorListener, sensorManager!!
                .getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_NORMAL)
    }

    private val sensorListener: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            lastAcceleration = currentAcceleration
            currentAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            val delta: Float = currentAcceleration - lastAcceleration
            acceleration = acceleration * 0.9f + delta

            if (acceleration > 10) {
                var alert = findViewById<TextView>(R.id.alert_message)
                var playerView = findViewById<TextView>(R.id.player_dice_number)
                var aiView = findViewById<TextView>(R.id.ai_dice_number)
                if(status == GameStatus.NewGame){
                    alert.visibility = TextView.INVISIBLE
                    currentAiDice = randomDice()
                    currentPlayerDice = randomDice()

                    playerView.visibility = TextView.VISIBLE
                    aiView.visibility = TextView.VISIBLE
                    status = GameStatus.ShakeDice
                    Toast.makeText(applicationContext, "Shake Dice Result", Toast.LENGTH_SHORT).show()
                }
                else if(status == GameStatus.ShakeDice){
                    playerView.text = ""+currentPlayerDice
                    aiView.text = ""+currentAiDice
                    var winner = ""
                    if(currentPlayerDice == currentAiDice)
                        winner = "draw"
                    else if(currentPlayerDice > currentAiDice)
                        winner = "player win"
                    else if(currentPlayerDice < currentAiDice)
                        winner = "ai win"

                    status = GameStatus.OpenUp
                    Toast.makeText(applicationContext, "This round is $winner", Toast.LENGTH_SHORT).show()
                }
                else if(status == GameStatus.OpenUp){
                    currentAiDice = 0
                    currentPlayerDice = 0

                    playerView.visibility = TextView.INVISIBLE
                    aiView.visibility = TextView.INVISIBLE
                    playerView.text = "Hidden"
                    aiView.text = "Hidden"
                    alert.visibility = TextView.VISIBLE
                    status = GameStatus.NewGame

                    Toast.makeText(applicationContext, "Game Reset!", Toast.LENGTH_SHORT).show()
                }
            }
        }
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    private val lightSensorListener: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val lightValue = event.values[0]
            var playerView = findViewById<TextView>(R.id.player_dice_number)
            if(status == GameStatus.ShakeDice){
                if(lightValue < 200){
                    playerView.text = ""+currentPlayerDice
                }else{
                    playerView.text = "Hidden"
                }
            }

        }
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    fun randomDice(): Int {
        val randomGenerator = Random()
        return randomGenerator.nextInt(6) + 1
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

    override fun onResume() {
        sensorManager?.registerListener(sensorListener, sensorManager!!.getDefaultSensor(
            Sensor .TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL
        )
        super.onResume()
    }

    override fun onPause() {
        sensorManager!!.unregisterListener(sensorListener)
        super.onPause()
    }
}