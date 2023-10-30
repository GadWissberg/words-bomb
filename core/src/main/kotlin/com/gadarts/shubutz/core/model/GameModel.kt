package com.gadarts.shubutz.core.model

import com.gadarts.shubutz.core.DebugSettings

class GameModel(var coins: Int, val selectedMode: GameModes) {

    var helpAvailable: Boolean = true

    @Suppress("KotlinConstantConditions")
    var triesLeft =
        if (DebugSettings.NUMBER_OF_TRIES > 0) DebugSettings.NUMBER_OF_TRIES else selectedMode.getNumberOfTries()
    val currentTargetData = CurrentTargetData()
    lateinit var options: MutableList<Char>
    var score: Long = 0

    fun setNewPhrase(phrase: String) {
        currentTargetData.currentPhrase = phrase
        helpAvailable = true
    }

    companion object {
        const val LETTERS = "₪-/:םןףץך?!אבגדהוזחטיכלמנסעפצקרשת"
        val allowedLetters = LETTERS.subSequence(11, LETTERS.length)
        var wordRevealFree = true
        const val DISPLAY_TARGET_COST = 4
    }
}
