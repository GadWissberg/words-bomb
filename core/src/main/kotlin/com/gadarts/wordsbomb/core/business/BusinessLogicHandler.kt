package com.gadarts.wordsbomb.core.business

import com.gadarts.wordsbomb.core.Notifier
import com.gadarts.wordsbomb.core.model.GameModel

class BusinessLogicHandler : Notifier<BusinessLogicHandlerEventsSubscriber> {

    override val subscribers = HashSet<BusinessLogicHandlerEventsSubscriber>()

    fun beginGame(gameModel: GameModel) {
        gameModel.currentWord = "דוגמא".reversed()
        decideHiddenLetters(gameModel)
        val ops = gameModel.hiddenLettersIndices.map { gameModel.currentWord[it] }.toMutableList()
        for (i in 0 until GameModel.MAX_OPTIONS - gameModel.currentWord.length) {
            ops.add(GameModel.allowedLetters.random())
        }
        gameModel.options = ops
    }

    private fun decideHiddenLetters(gameModel: GameModel) {
        val numberOfHiddenLetters = (gameModel.currentWord.length / 2F).toInt() + 1
        val list = mutableListOf<Int>()
        for (i in 0 until gameModel.currentWord.length) {
            list.add(i)
        }
        gameModel.hiddenLettersIndices = list.shuffled().toMutableList()
        val toDrop = gameModel.currentWord.length - numberOfHiddenLetters
        gameModel.hiddenLettersIndices = gameModel.hiddenLettersIndices.drop(toDrop).toMutableList()
    }

    fun onBrickClicked(letter: Char, gameModel: GameModel) {
        val index = gameModel.hiddenLettersIndices.find { gameModel.currentWord[it] == letter }
        if (index != null) {
            gameModel.hiddenLettersIndices.remove(index)
            val gameWin = gameModel.hiddenLettersIndices.isEmpty()
            subscribers.forEach { it.onGuessSuccess(index, gameWin) }
        } else {
            gameModel.numberOfMisses++
            subscribers.forEach { it.onGuessFail(gameModel.numberOfMisses >= 5) }
        }
    }

}