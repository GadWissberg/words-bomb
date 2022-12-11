package com.gadarts.shubutz.core.business

import com.gadarts.shubutz.core.DebugSettings
import com.gadarts.shubutz.core.DebugSettings.FORCE_TEST_WORD
import com.gadarts.shubutz.core.DebugSettings.TEST_WORD
import com.gadarts.shubutz.core.Notifier
import com.gadarts.shubutz.core.model.GameModel
import com.gadarts.shubutz.core.model.WordObject
import java.util.HashMap

class BusinessLogicHandler : Notifier<BusinessLogicHandlerEventsSubscriber> {

    override val subscribers = HashSet<BusinessLogicHandlerEventsSubscriber>()

    fun beginGame(gameModel: GameModel, words: HashMap<String, List<WordObject>>) {
        val category = words[words.keys.random()]
        gameModel.currentWord =
            if (FORCE_TEST_WORD) TEST_WORD.reversed() else category!!.random().word.reversed()
        decideHiddenLetters(gameModel)
        val ops = gameModel.hiddenLettersIndices.map { gameModel.currentWord[it] }.toMutableList()
        for (i in 0 until GameModel.MAX_OPTIONS - gameModel.currentWord.length) {
            ops.add(GameModel.allowedLetters.random())
        }
        gameModel.options = ops
    }

    private fun decideHiddenLetters(gameModel: GameModel) {
        val listOfIndices = mutableListOf<Int>()
        for (i in 0 until gameModel.currentWord.length) {
            if (gameModel.currentWord[i] != ' ') {
                listOfIndices.add(i)
            }
        }
        gameModel.hiddenLettersIndices = listOfIndices.shuffled().toMutableList()
        val toDrop = gameModel.currentWord.length - (gameModel.currentWord.length / 2F).toInt() + 1
        gameModel.hiddenLettersIndices =
            gameModel.hiddenLettersIndices.drop(toDrop).toMutableList()
    }

    fun onBrickClicked(letter: Char, gameModel: GameModel) {
        val index = gameModel.hiddenLettersIndices.find { gameModel.currentWord[it] == letter }
        if (index != null) {
            gameModel.hiddenLettersIndices.remove(index)
            val gameWin = gameModel.hiddenLettersIndices.isEmpty()
            subscribers.forEach { it.onGuessSuccess(index, gameWin) }
        } else {
            gameModel.triesLeft--
            subscribers.forEach { it.onGuessFail(gameModel.triesLeft <= 0) }
        }
    }

}