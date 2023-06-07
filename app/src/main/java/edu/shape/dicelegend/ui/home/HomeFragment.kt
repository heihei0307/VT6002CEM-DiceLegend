package edu.shape.dicelegend.ui.home

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import edu.shape.dicelegend.DiceHistory
import edu.shape.dicelegend.MainActivity
import edu.shape.dicelegend.R
import edu.shape.dicelegend.Statics
import edu.shape.dicelegend.databinding.FragmentHomeBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Objects
import java.util.Random
import kotlin.math.sqrt

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private var context: Context? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var sensorManager: SensorManager? = null
    private var acceleration = 0f
    private var currentAcceleration = 0f
    private var lastAcceleration = 0f
    private var status: MainActivity.GameStatus = MainActivity.GameStatus.NewGame
    private var currentAiDice = 0
    private var currentPlayerDice = 0
    private var currentWinner = ""
    lateinit var _db: DatabaseReference
    private var playerKey: String? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
                ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        context = getContext()

        val alertText: TextView = binding.alertMessage
        alertText.text = "Shake to random dice"

        sensorManager = activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        Objects.requireNonNull(sensorManager)!!
            .registerListener(sensorListener, sensorManager!!
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)

        acceleration = 10f
        currentAcceleration = SensorManager.GRAVITY_EARTH
        lastAcceleration = SensorManager.GRAVITY_EARTH

        Objects.requireNonNull(sensorManager)!!
            .registerListener(lightSensorListener, sensorManager!!
                .getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_NORMAL)

        val sharedPrefs = activity?.getPreferences(AppCompatActivity.MODE_PRIVATE)
        playerKey = sharedPrefs?.getString("player_key", null)

        _db = FirebaseDatabase.getInstance().reference

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
                var alert = binding.root.findViewById<TextView>(R.id.alert_message)
                var playerView = binding.root.findViewById<TextView>(R.id.player_dice_number)
                var aiView = binding.root.findViewById<TextView>(R.id.ai_dice_number)
                if(status == MainActivity.GameStatus.NewGame){
                    alert.visibility = TextView.INVISIBLE
                    currentAiDice = randomDice()
                    currentPlayerDice = randomDice()

                    playerView.visibility = TextView.VISIBLE
                    aiView.visibility = TextView.VISIBLE
                    status = MainActivity.GameStatus.ShakeDice
                    Toast.makeText(context, "Shake Dice Result", Toast.LENGTH_SHORT).show()
                }
                else if(status == MainActivity.GameStatus.ShakeDice){
                    playerView.text = ""+currentPlayerDice
                    aiView.text = ""+currentAiDice
                    var winner = ""
                    if(currentPlayerDice == currentAiDice)
                        winner = "draw"
                    else if(currentPlayerDice > currentAiDice)
                        winner = "player win"
                    else if(currentPlayerDice < currentAiDice)
                        winner = "ai win"

                    status = MainActivity.GameStatus.OpenUp
                    currentWinner = winner
                    addDiceHistory()
                    Toast.makeText(context, "This round is $winner", Toast.LENGTH_SHORT).show()
                }
                else if(status == MainActivity.GameStatus.OpenUp){
                    currentAiDice = 0
                    currentPlayerDice = 0
                    currentWinner = ""

                    playerView.visibility = TextView.INVISIBLE
                    aiView.visibility = TextView.INVISIBLE
                    playerView.text = "Hidden"
                    aiView.text = "Hidden"
                    alert.visibility = TextView.VISIBLE
                    status = MainActivity.GameStatus.NewGame

                    Toast.makeText(context, "Game Reset!", Toast.LENGTH_SHORT).show()
                }
            }
        }
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    private val lightSensorListener: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val lightValue = event.values[0]
            var playerView = binding.root.findViewById<TextView>(R.id.player_dice_number)
            if(status == MainActivity.GameStatus.ShakeDice){
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

    fun addDiceHistory(){
        val history = DiceHistory.create()
        history.playerId = playerKey
        history.diceDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        history.playerDice = currentPlayerDice
        history.aiDice = currentAiDice
        history.winner = currentWinner
        val newHistory = _db.child(Statics.FIREBASE_DICE_HISTORY).push()
        history.objectId = newHistory.key
        newHistory.setValue(history)

        Toast.makeText(context, "History added to list successfully" + history.objectId, Toast.LENGTH_SHORT).show()
    }
}