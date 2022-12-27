package com.gadarts.shubutz.core.model

import com.gadarts.shubutz.core.DebugSettings

class GameModel(var coins: Int) {
    var triesLeft = DebugSettings.NUMBER_OF_TRIES
    lateinit var currentTarget: String
    lateinit var hiddenLettersIndices: MutableList<Int>
    lateinit var options: MutableList<Char>

    companion object {
        const val LETTERS = "-/:םןףץך?!אבגדהוזחטיכלמנסעפצקרשת"
        val allowedLetters = LETTERS.subSequence(10, LETTERS.length)
    }
}
