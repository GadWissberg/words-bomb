package com.gadarts.shubutz.core.business

import com.gadarts.shubutz.core.AndroidInterface
import com.gadarts.shubutz.core.DebugSettings
import com.gadarts.shubutz.core.DebugSettings.FORCE_TEST_WORD
import com.gadarts.shubutz.core.DebugSettings.TEST_WORD
import com.gadarts.shubutz.core.Notifier
import com.gadarts.shubutz.core.model.GameModel
import com.gadarts.shubutz.core.model.GameModel.Companion.allowedLetters
import java.util.HashMap

/**
 * Responsible to take care of the actual game's rules.
 */
class GameLogicHandler(
    private val words: HashMap<String, ArrayList<String>>,
    private val androidInterface: AndroidInterface
) :
    Notifier<GameLogicHandlerEventsSubscriber> {

    private var unusedWords: HashMap<String, ArrayList<String>> = HashMap(words)
    override val subscribers = HashSet<GameLogicHandlerEventsSubscriber>()

    /**
     * Initializes the bomb's counter, decide the letter to be hidden and initialize the options
     * letters array.
     */
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

    /**
     * Called when a brick is clicked.
     */
    fun onBrickClicked(selectedLetter: Char, gameModel: GameModel) {
        val indices = gameModel.hiddenLettersIndices.filter {
            val currentLetter = gameModel.currentTarget[it]
            currentLetter == selectedLetter || suffixLetters[selectedLetter] == currentLetter
        }
        if (indices.isNotEmpty()) {
            gameModel.hiddenLettersIndices.removeAll(indices)
            val gameWin = gameModel.hiddenLettersIndices.isEmpty()
            if (gameWin) {
                gameModel.coins++
                androidInterface.saveSharedPreferencesValue(
                    SHARED_PREFERENCES_DATA_KEY_COINS,
                    gameModel.coins
                )
            }
            subscribers.forEach { it.onCorrectGuess(indices, gameWin) }
        } else {
            gameModel.triesLeft--
            subscribers.forEach { it.onIncorrectGuess(gameModel.triesLeft <= 0) }
        }
    }

    private fun chooseWord(gameModel: GameModel) {
        val categoryName = unusedWords.keys.random()
        val category = unusedWords[categoryName]
        gameModel.currentTarget =
            if (FORCE_TEST_WORD) TEST_WORD.reversed() else category!!.random().reversed()
        category!!.remove(gameModel.currentTarget)
        if (category.isEmpty()) {
            unusedWords.remove(categoryName)
        }
    }

    private fun decideHiddenLetters(model: GameModel) {
        val listOfIndices = mutableListOf<Int>()
        for (i in 0 until model.currentTarget.length) {
            if (model.currentTarget[i] != ' ') {
                listOfIndices.add(i)
            }
        }
        model.hiddenLettersIndices = listOfIndices.shuffled().toMutableList()
            .drop(model.currentTarget.length - ((model.currentTarget.length / 2F).toInt() + 1))
            .toMutableList()
    }

    companion object {
        const val SHARED_PREFERENCES_DATA_KEY_COINS = "coins"

        private val suffixLetters = mapOf(
            'פ' to 'ף',
            'כ' to 'ך',
            'נ' to 'ן',
            'צ' to 'ץ',
            'מ' to 'ם'
        )
    }
}
