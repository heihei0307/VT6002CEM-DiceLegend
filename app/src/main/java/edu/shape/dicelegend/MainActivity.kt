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

    private lateinit var binding: ActivityMainBinding
    private var sensorManager: SensorManager? = null
    private var acceleration = 0f
    private var currentAcceleration = 0f
    private var lastAcceleration = 0f
    private var resume = false

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
                if(!resume){
                    var playerNumber = findViewById<TextView>(R.id.player_dice_number)
                    var aiNumber = findViewById<TextView>(R.id.ai_dice_number)
                    var alert = findViewById<TextView>(R.id.alert_message)
                    alert.visibility = TextView.INVISIBLE
                    playerNumber.text = randomDice()
                    aiNumber.text = randomDice()
                    playerNumber.visibility = TextView.VISIBLE
                    aiNumber.visibility = TextView.VISIBLE
                    resume = true
                    Toast.makeText(applicationContext, "Shake Dice Result", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(applicationContext, "Already shake, please turn off the light to reset the game!", Toast.LENGTH_SHORT).show()
                }
            }
        }
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    private val lightSensorListener: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val lightValue = event.values[0]
            if(lightValue < 200){
                Toast.makeText(applicationContext, "Game Reset!", Toast.LENGTH_SHORT).show()
                var playerNumber = findViewById<TextView>(R.id.player_dice_number)
                var aiNumber = findViewById<TextView>(R.id.ai_dice_number)
                var alertMessage = findViewById<TextView>(R.id.alert_message)
                playerNumber.visibility = TextView.INVISIBLE
                aiNumber.visibility = TextView.INVISIBLE
                alertMessage.visibility = TextView.VISIBLE
                resume = false
            }
        }
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    fun randomDice(): String? {
        val randomGenerator = Random()
        val randomNum = randomGenerator.nextInt(6) + 1
        return ""+randomNum
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