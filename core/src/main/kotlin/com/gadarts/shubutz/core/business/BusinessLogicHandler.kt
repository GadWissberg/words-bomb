package com.gadarts.shubutz.core.business

import com.gadarts.shubutz.core.DebugSettings
import com.gadarts.shubutz.core.DebugSettings.FORCE_TEST_WORD
import com.gadarts.shubutz.core.DebugSettings.TEST_WORD
import com.gadarts.shubutz.core.Notifier
import com.gadarts.shubutz.core.model.GameModel
import com.gadarts.shubutz.core.model.GameModel.Companion.allowedLetters
import java.util.HashMap

class BusinessLogicHandler(private val words: HashMap<String, ArrayList<String>>) :
    Notifier<BusinessLogicHandlerEventsSubscriber> {

    private var unusedWords: HashMap<String, ArrayList<String>> = HashMap(words)
    override val subscribers = HashSet<BusinessLogicHandlerEventsSubscriber>()

    fun beginGame(gameModel: GameModel) {
        if (unusedWords.isEmpty()) {
            unusedWords = words
        }
        gameModel.triesLeft = DebugSettings.NUMBER_OF_TRIES
        chooseWord(gameModel)
        decideHiddenLetters(gameModel)
        gameModel.options = allowedLetters.toMutableList()
        gameModel.options.reversed()
    }

    private fun chooseWord(gameModel: GameModel) {
        val categoryName = unusedWords.keys.random()
        val category = words[categoryName]
        gameModel.currentTarget =
            if (FORCE_TEST_WORD) TEST_WORD.reversed() else category!!.random().reversed()
        category!!.remove(gameModel.currentTarget)
        if (category.isEmpty()) {
            words.remove(categoryName)
        }
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

    fun onBrickClicked(selectedLetter: Char, gameModel: GameModel) {
        val indices = gameModel.hiddenLettersIndices.filter {
            val currentLetter = gameModel.currentTarget[it]
            currentLetter == selectedLetter || suffixLetters[selectedLetter] == currentLetter
        }
        if (indices.isNotEmpty()) {
            gameModel.hiddenLettersIndices.removeAll(indices)
            val gameWin = gameModel.hiddenLettersIndices.isEmpty()
            subscribers.forEach { it.onGuessSuccess(indices, gameWin) }
        } else {
            gameModel.triesLeft--
            subscribers.forEach { it.onGuessFail(gameModel.triesLeft <= 0) }
        }
    }

    companion object {
        val suffixLetters = mapOf(
            'פ' to 'ף',
            'כ' to 'ך',
            'נ' to 'ן',
            'צ' to 'ץ',
            'מ' to 'ם'
        )
    }

}