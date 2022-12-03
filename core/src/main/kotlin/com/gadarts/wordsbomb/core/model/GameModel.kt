package com.gadarts.wordsbomb.core.model

class GameModel {
    lateinit var currentWord: String
    lateinit var hiddenLettersIndices: List<Int>
    lateinit var options: List<Char>

    companion object {
        private const val LETTERS = "-/:םןףץך?!אבגדהוזחטיכלמנסעפצקרשת"
        val allowedLetters = Player.LETTERS.subSequence(10, Player.LETTERS.length)
        const val MAX_OPTIONS = 21
    }
}