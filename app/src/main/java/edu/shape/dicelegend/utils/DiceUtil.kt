package edu.shape.dicelegend.utils

class DiceUtil {
    companion object{
        fun compareNumber(FirstNumber: Int, SecondNumber: Int): Int {
            var result = 0
            if(FirstNumber > SecondNumber)
                result = 1
            else if(FirstNumber < SecondNumber)
                result = 2

            return result
        }
    }
}