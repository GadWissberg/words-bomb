package com.gadarts.wordsbomb.core.model

class Player(val letters: ArrayList<Char>, val name: String) {
    val maxLetters: Int = letters.size
    var score = 0

    companion object {
        const val LETTERS = "-/:םןףץך?!אבגדהוזחטיכלמנסעפצקרשת"
        val allowedLetters = LETTERS.subSequence(10, LETTERS.length)
    }

}