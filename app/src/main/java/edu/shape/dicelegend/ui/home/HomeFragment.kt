package edu.shape.dicelegend.ui.home

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CaptureResult.SENSOR_SENSITIVITY
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import edu.shape.dicelegend.DiceHistory
import edu.shape.dicelegend.MainActivity
import edu.shape.dicelegend.R
import edu.shape.dicelegend.Statics
import edu.shape.dicelegend.databinding.FragmentHomeBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.w3c.dom.Text
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Objects
import java.util.Random
import java.util.concurrent.CountDownLatch
import kotlin.math.sqrt

class HomeFragment : Fragment() {

    data class DiceResult(val dice: Int = 0)
    class Extra() {
        fun compareDiceSize(PlayerDice: Int, AiDice: Int): Int {
            var result = 0
            if(PlayerDice > AiDice)
                result = 1
            else if(PlayerDice < AiDice)
                result = 2

            return result
        }
    }

    private var _binding: FragmentHomeBinding? = null
    private var context: Context? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var sensorManager: SensorManager? = null
    private var acceleration = 0f
    private var currentAcceleration = 0f
    private var lastAcceleration = 0f
    private var status: MainActivity.GameStatus = MainActivity.GameStatus.PendingStart
    private var currentAiDice = 0
    private var currentPlayerDice = 0
    private var currentWinner = ""
    lateinit var _db: DatabaseReference
    private var playerKey: String? = null
    private var client: OkHttpClient? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        root.setOnClickListener{
            Log.d("Click event", "screen is clicked")
            if(status == MainActivity.GameStatus.PendingStart){
                status = MainActivity.GameStatus.NewGame
                val snakeText = binding.txtSnake
                snakeText.visibility = TextView.INVISIBLE
                val alertText: TextView = binding.alertMessage
                alertText.text = "Shake to random dice"
                alertText.visibility = TextView.VISIBLE
            }
        }

        context = getContext()

        sensorManager = activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        Objects.requireNonNull(sensorManager)!!
            .registerListener(
                sensorListener, sensorManager!!
                    .getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL
            )

        acceleration = 10f
        currentAcceleration = SensorManager.GRAVITY_EARTH
        lastAcceleration = SensorManager.GRAVITY_EARTH

        Objects.requireNonNull(sensorManager)!!
            .registerListener(
                lightSensorListener, sensorManager!!
                    .getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_NORMAL
            )

        Objects.requireNonNull(sensorManager)!!
            .registerListener(
                proximitySensorListener, sensorManager!!
                    .getDefaultSensor(Sensor.TYPE_PROXIMITY), SensorManager.SENSOR_DELAY_NORMAL
            )

        val sharedPrefs = activity?.getPreferences(AppCompatActivity.MODE_PRIVATE)
        playerKey = sharedPrefs?.getString("player_key", null)

        _db = FirebaseDatabase.getInstance().reference

        client = OkHttpClient()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        sensorManager?.registerListener(
            sensorListener, sensorManager!!.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER
            ), SensorManager.SENSOR_DELAY_NORMAL
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
                var alert = binding.alertMessage
                var playerImage = binding.imgPlayerDice
                var aiImage = binding.imgAiDice
                if (status == MainActivity.GameStatus.NewGame) {
                    alert.visibility = TextView.INVISIBLE
                    currentAiDice = getDice()
                    currentPlayerDice = getDice()
                    playerImage.visibility = ImageView.VISIBLE
                    aiImage.visibility = ImageView.VISIBLE
                    status = MainActivity.GameStatus.ShakeDice
                } else if (status == MainActivity.GameStatus.OpenUp) {
                    currentAiDice = 0
                    currentPlayerDice = 0
                    currentWinner = ""
                    playerImage.setImageResource(R.drawable.dice_target)
                    aiImage.setImageResource(R.drawable.dice_target)
                    playerImage.visibility = ImageView.INVISIBLE
                    aiImage.visibility = ImageView.INVISIBLE
                    alert.visibility = TextView.INVISIBLE
                    binding.txtSnake.visibility = TextView.VISIBLE
                    status = MainActivity.GameStatus.PendingStart
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    private val lightSensorListener: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val lightValue = event.values[0]
            if (status == MainActivity.GameStatus.ShakeDice) {
                var playerImage = binding.imgPlayerDice
                if (lightValue < 200) {
                    when(currentPlayerDice){
                        1 -> playerImage.setImageResource(R.drawable.dice_six_faces_one)
                        2 -> playerImage.setImageResource(R.drawable.dice_six_faces_two)
                        3 -> playerImage.setImageResource(R.drawable.dice_six_faces_three)
                        4 -> playerImage.setImageResource(R.drawable.dice_six_faces_four)
                        5 -> playerImage.setImageResource(R.drawable.dice_six_faces_five)
                        6 -> playerImage.setImageResource(R.drawable.dice_six_faces_six)
                    }
                } else {
                    playerImage.setImageResource(R.drawable.dice_target)
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    private val proximitySensorListener: SensorEventListener = object : SensorEventListener{
        override fun onSensorChanged(event: SensorEvent?) {
            val distance = event?.values?.first()
            Log.d("Tag", "prox dis:${distance} cm")
            if(distance!! < 1){
                var playerImage = binding.imgPlayerDice
                var aiImage = binding.imgAiDice
                if (status == MainActivity.GameStatus.ShakeDice) {
                    when(currentPlayerDice){
                        1 -> playerImage.setImageResource(R.drawable.dice_six_faces_one)
                        2 -> playerImage.setImageResource(R.drawable.dice_six_faces_two)
                        3 -> playerImage.setImageResource(R.drawable.dice_six_faces_three)
                        4 -> playerImage.setImageResource(R.drawable.dice_six_faces_four)
                        5 -> playerImage.setImageResource(R.drawable.dice_six_faces_five)
                        6 -> playerImage.setImageResource(R.drawable.dice_six_faces_six)
                    }
                    when(currentAiDice){
                        1 -> aiImage.setImageResource(R.drawable.dice_six_faces_one)
                        2 -> aiImage.setImageResource(R.drawable.dice_six_faces_two)
                        3 -> aiImage.setImageResource(R.drawable.dice_six_faces_three)
                        4 -> aiImage.setImageResource(R.drawable.dice_six_faces_four)
                        5 -> aiImage.setImageResource(R.drawable.dice_six_faces_five)
                        6 -> aiImage.setImageResource(R.drawable.dice_six_faces_six)
                    }
                    var winner = ""
                    val result = Extra().compareDiceSize(currentPlayerDice, currentAiDice)
                    if(result == 0)
                        winner = "draw"
                    else if(result == 1)
                        winner = "player win"
                    else if(result == 2)
                        winner = "ai win"

                    status = MainActivity.GameStatus.OpenUp
                    currentWinner = winner
                    addDiceHistory()
                    Toast.makeText(context, "This round is $winner", Toast.LENGTH_SHORT).show()
                    val alertText: TextView = binding.alertMessage
                    alertText.text = "Shake to end game!"
                    alertText.visibility = TextView.VISIBLE
                }
            }
        }
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    fun randomDice(): Int {
        val randomGenerator = Random()
        return randomGenerator.nextInt(6) + 1
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
                val result = Gson().fromJson(resStr, DiceResult::class.java)

                dice = result.dice
                Log.d("HKT", "response: $dice")
                latch.countDown()
            }
        })

        latch.await()

        return dice
    }

    fun addDiceHistory() {
        val history = DiceHistory.create()
        history.playerId = playerKey
        history.diceDate =
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        history.playerDice = currentPlayerDice
        history.aiDice = currentAiDice
        history.winner = currentWinner
        val newHistory = _db.child(Statics.FIREBASE_DICE_HISTORY).push()
        history.objectId = newHistory.key
        newHistory.setValue(history)

        Toast.makeText(
            context,
            "History added to list successfully" + history.objectId,
            Toast.LENGTH_SHORT
        ).show()
    }
}