package edu.shape.dicelegend.utils

import java.util.Random

class CommonUtil {
    companion object{
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
}