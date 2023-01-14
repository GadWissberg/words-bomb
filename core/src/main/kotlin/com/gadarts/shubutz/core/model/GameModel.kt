package com.gadarts.shubutz.core.model

import com.gadarts.shubutz.core.DebugSettings

class GameModel(var coins: Int, val selectedDifficulty: Difficulties) {
    lateinit var currentCategory: String
    var triesLeft =
        if (DebugSettings.NUMBER_OF_TRIES > 0) DebugSettings.NUMBER_OF_TRIES else selectedDifficulty.tries
    lateinit var currentTarget: String
    var hiddenLettersIndices = mutableListOf<Int>()
    lateinit var options: MutableList<Char>

    companion object {
        const val AMOUNT_PACK_FIRST = 8
        const val AMOUNT_PACK_SECOND = 16
        const val AMOUNT_PACK_THIRD = 32
        const val LETTERS = "-/:םןףץך?!אבגדהוזחטיכלמנסעפצקרשת"
        val allowedLetters = LETTERS.subSequence(10, LETTERS.length)
    }
}
