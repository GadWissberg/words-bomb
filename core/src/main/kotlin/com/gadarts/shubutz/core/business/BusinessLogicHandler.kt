package com.gadarts.shubutz.core.business

import com.gadarts.shubutz.core.DebugSettings.FORCE_TEST_WORD
import com.gadarts.shubutz.core.DebugSettings.TEST_WORD
import com.gadarts.shubutz.core.Notifier
import com.gadarts.shubutz.core.model.GameModel
import com.gadarts.shubutz.core.model.GameModel.Companion.allowedLetters
import com.gadarts.shubutz.core.model.WordObject
import java.util.HashMap

class BusinessLogicHandler : Notifier<BusinessLogicHandlerEventsSubscriber> {

    override val subscribers = HashSet<BusinessLogicHandlerEventsSubscriber>()

    fun beginGame(gameModel: GameModel, words: HashMap<String, List<WordObject>>) {
        val category = words[words.keys.random()]
        gameModel.currentTarget =
            if (FORCE_TEST_WORD) TEST_WORD.reversed() else category!!.random().word.reversed()
        decideHiddenLetters(gameModel)
        gameModel.options = allowedLetters.toMutableList()
        gameModel.options.reversed()
    }

    private fun decideHiddenLetters(gameModel: GameModel) {
        val listOfIndices = mutableListOf<Int>()
        for (i in 0 until gameModel.currentTarget.length) {
            if (gameModel.currentTarget[i] != ' ') {
                listOfIndices.add(i)
            }
        }
        gameModel.hiddenLettersIndices = listOfIndices.shuffled().toMutableList()
        val toDrop =
            gameModel.currentTarget.length - ((gameModel.currentTarget.length / 2F).toInt() + 1)
        gameModel.hiddenLettersIndices =
            gameModel.hiddenLettersIndices.drop(toDrop).toMutableList()
    }

    fun onBrickClicked(letter: Char, gameModel: GameModel) {
        val indices =
            gameModel.hiddenLettersIndices.filter { gameModel.currentTarget[it] == letter }
        if (indices.isNotEmpty()) {
            gameModel.hiddenLettersIndices.removeAll(indices)
            val gameWin = gameModel.hiddenLettersIndices.isEmpty()
            subscribers.forEach { it.onGuessSuccess(indices, gameWin) }
        } else {
            gameModel.triesLeft--
            subscribers.forEach { it.onGuessFail(gameModel.triesLeft <= 0) }
        }
    }

}