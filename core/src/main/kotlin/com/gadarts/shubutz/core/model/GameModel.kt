package com.gadarts.shubutz.core.model

import com.gadarts.shubutz.core.DebugSettings

class GameModel(var coins: Int, val selectedDifficulty: Difficulties) {

    lateinit var currentCategory: String
    var triesLeft =
        if (DebugSettings.NUMBER_OF_TRIES > 0) DebugSettings.NUMBER_OF_TRIES else selectedDifficulty.tries
    lateinit var currentPhrase: String
    var hiddenLettersIndices = mutableListOf<Int>()
    lateinit var options: MutableList<Char>
    var score: Long = 0

    companion object {
        const val LETTERS = "₪-/:םןףץך?!אבגדהוזחטיכלמנסעפצקרשת"
        val allowedLetters = LETTERS.subSequence(11, LETTERS.length)
    }
}
