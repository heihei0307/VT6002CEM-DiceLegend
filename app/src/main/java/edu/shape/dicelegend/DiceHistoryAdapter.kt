package edu.shape.dicelegend

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class DiceHistoryAdapter(context: Context, diceHistoryList: MutableList<DiceHistory>) :
    BaseAdapter() {
    private val _inflater: LayoutInflater = LayoutInflater.from(context)
    private val _diceHistoryList = diceHistoryList

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val objectId: String = _diceHistoryList[position].objectId as String
        val playerId: String = _diceHistoryList[position].playerId as String
        val playerDice: Int = _diceHistoryList[position].playerDice as Int
        val aiDice: Int = _diceHistoryList[position].aiDice as Int
        val diceDate: String = _diceHistoryList[position].diceDate as String
        val winner: String = _diceHistoryList[position].winner as String

        val view: View
        val listRowHolder: ListRowHolder
        if (convertView == null) {
            view = _inflater.inflate(R.layout.dice_history_rows, parent, false)
            listRowHolder = ListRowHolder(view)
            view.tag = listRowHolder
        } else {
            view = convertView
            listRowHolder = view.tag as ListRowHolder
        }
        listRowHolder.playerId.text = playerId
        listRowHolder.dateTime.text = diceDate
        listRowHolder.playerDice.text = playerDice.toString()
        listRowHolder.aiDice.text = aiDice.toString()
        listRowHolder.winner.text = winner
        return view
    }

    override fun getItem(position: Int): Any {
        return _diceHistoryList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return _diceHistoryList.size
    }

    private class ListRowHolder(row: View?) {
        val playerId: TextView = row!!.findViewById(R.id.txtPlayerId)
        val dateTime: TextView = row!!.findViewById(R.id.txtDateTime)
        val playerDice: TextView = row!!.findViewById(R.id.txtPlayerDice)
        val aiDice: TextView = row!!.findViewById(R.id.txtAiDice)
        val winner: TextView = row!!.findViewById(R.id.txtWinner)
    }
}