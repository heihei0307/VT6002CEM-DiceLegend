package edu.shape.dicelegend.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import edu.shape.dicelegend.DiceHistory
import edu.shape.dicelegend.DiceHistoryAdapter
import edu.shape.dicelegend.R
import edu.shape.dicelegend.databinding.FragmentDiceHistoryBinding
import java.time.LocalDateTime

class DashboardFragment : Fragment() {

    private var _binding: FragmentDiceHistoryBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    lateinit var _adapter: DiceHistoryAdapter
    var _diceHistoryList: MutableList<DiceHistory>? = null
    lateinit var _db: DatabaseReference

    var _diceHistoryListener: ValueEventListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            loadDiceHistoryList(dataSnapshot)
        }
        override fun onCancelled(databaseError: DatabaseError) {
            // Getting Item failed, log a message
            Log.w("MainActivity", "loadItem:onCancelled", databaseError.toException())
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiceHistoryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        _db = FirebaseDatabase.getInstance().reference
        _diceHistoryList = mutableListOf()
        val context = requireContext()
        _adapter = DiceHistoryAdapter(context, _diceHistoryList!!)

        val listviewDiceHistory = root.findViewById<ListView>(R.id.listviewDiceHistory)
        listviewDiceHistory!!.setAdapter(_adapter)

        _db.orderByKey().addValueEventListener(_diceHistoryListener)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadDiceHistoryList(dataSnapshot: DataSnapshot){
        Log.d("MainActivity", "loadDiceHistoryList")

        val diceHistories = dataSnapshot.children.iterator()

        if(diceHistories.hasNext()){
            _diceHistoryList!!.clear()

            val listIndex = diceHistories.next()
            val itemsIterator = listIndex.children.iterator()

            while(itemsIterator.hasNext()){
                val currentItem = itemsIterator.next()
                val diceHistory = DiceHistory.create()

                val map = currentItem.getValue() as HashMap<String, Any>
                var longPlayerDice = map["playerDice"] as Long?
                var longAiDice = map["aiDice"] as Long?

                diceHistory.objectId = currentItem.key
                diceHistory.playerId = map["playerId"] as String?
                diceHistory.diceDate = map["diceDate"] as String?
                diceHistory.playerDice = longPlayerDice!!.toInt()
                diceHistory.aiDice = longAiDice!!.toInt()
                diceHistory.winner = map["winner"] as String?

                _diceHistoryList!!.add(diceHistory)
            }
        }
        _adapter.notifyDataSetChanged()
    }
}