package com.gadarts.wordsbomb.core.model

class GameModel {
    lateinit var currentWord: String
    lateinit var options: List<Char>

    companion object {
        private const val LETTERS = "-/:םןףץך?!אבגדהוזחטיכלמנסעפצקרשת"
        val allowedLetters = Player.LETTERS.subSequence(10, Player.LETTERS.length)
    }
}
