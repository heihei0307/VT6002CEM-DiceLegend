package edu.shape.dicelegend

import java.time.LocalDateTime

class DiceHistory {
    companion object Factory {
        fun create(): DiceHistory = DiceHistory()
    }

    var objectId: String? = null
    var playerId: String? = null
    var playerDice: Int? = null
    var aiDice: Int? = null
    var diceDate: String? = null
    var winner: String? = null
}