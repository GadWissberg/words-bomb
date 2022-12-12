package com.gadarts.shubutz.core.model

class GameModel() {
    var triesLeft = 6
    lateinit var currentTarget: String
    lateinit var hiddenLettersIndices: MutableList<Int>
    lateinit var options: List<Char>

    companion object {
        private const val LETTERS = "-/:םןףץך?!אבגדהוזחטיכלמנסעפצקרשת"
        val allowedLetters = Player.LETTERS.subSequence(10, Player.LETTERS.length)
        const val MAX_OPTIONS = 21
    }
}
