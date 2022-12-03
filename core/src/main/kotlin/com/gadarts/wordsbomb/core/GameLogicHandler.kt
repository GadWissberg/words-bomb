package com.gadarts.wordsbomb.core

import com.gadarts.wordsbomb.core.model.GameModel
import com.gadarts.wordsbomb.core.model.GameModel.Companion.MAX_OPTIONS

class GameLogicHandler {

    fun beginGame(gameModel: GameModel) {
        gameModel.currentWord = "דוגמא"
        val toMutableList = gameModel.currentWord.toMutableList()
        for (i in 0 until MAX_OPTIONS - gameModel.currentWord.length) {
            toMutableList.add(GameModel.allowedLetters.random())
        }
        gameModel.options = toMutableList
    }


}
