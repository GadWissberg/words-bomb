package com.gadarts.wordsbomb.core

import com.gadarts.wordsbomb.core.model.GameModel
import com.gadarts.wordsbomb.core.model.GameModel.Companion.MAX_OPTIONS

class GameLogicHandler {

    fun beginGame(gameModel: GameModel) {
        gameModel.currentWord = "דוגמא".reversed()
        decideHiddenLetters(gameModel)
        val toMutableList = gameModel.currentWord.toMutableList()
        for (i in 0 until MAX_OPTIONS - gameModel.currentWord.length) {
            toMutableList.add(GameModel.allowedLetters.random())
        }
        gameModel.options = toMutableList
    }

    private fun decideHiddenLetters(gameModel: GameModel) {
        val numberOfHiddenLetters = (gameModel.currentWord.length / 2F).toInt() + 1
        val list = mutableListOf<Int>()
        for (i in 0 until gameModel.currentWord.length) {
            list.add(i)
        }
        gameModel.hiddenLettersIndices = list.shuffled().toMutableList()
        val toDrop = gameModel.currentWord.length - numberOfHiddenLetters
        gameModel.hiddenLettersIndices = gameModel.hiddenLettersIndices.drop(toDrop)
    }


}
