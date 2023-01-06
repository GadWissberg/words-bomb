package com.gadarts.shubutz.core.business

import com.gadarts.shubutz.core.AndroidInterface
import com.gadarts.shubutz.core.DebugSettings.TEST_WORD
import com.gadarts.shubutz.core.Notifier
import com.gadarts.shubutz.core.model.GameModel
import com.gadarts.shubutz.core.model.GameModel.Companion.allowedLetters

/**
 * Responsible to take care of the actual game's rules.
 */
class GameLogicHandler(
    private val words: HashMap<String, ArrayList<String>>,
    private val androidInterface: AndroidInterface,
) :
    Notifier<GameLogicHandlerEventsSubscriber> {

    private var unusedWords: HashMap<String, ArrayList<String>> = HashMap(words)
    override val subscribers = HashSet<GameLogicHandlerEventsSubscriber>()

    /**
     * Initializes the bomb's counter, decide the letter to be hidden and initialize the options
     * letters array.
     */
    fun beginGame(gameModel: GameModel) {
        unusedWords = words
        unusedWords.forEach {
            unusedWords[it.key] = ArrayList(it.value.filter { p ->
                p.length >= gameModel.selectedDifficulty.minimumAmountOfLettersForRegularPhrases
            })
        }
        beginRound(gameModel)
    }

    /**
     * Initializes the bomb's counter, decide the letter to be hidden and initialize the options
     * letters array.
     */
    fun beginRound(gameModel: GameModel) {
        if (unusedWords.isEmpty()) {
            unusedWords = words
        }
        gameModel.triesLeft = gameModel.selectedDifficulty.tries
        chooseTarget(gameModel)
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
            gameModel.hiddenLettersIndices.removeAll(indices.toSet())
            val gameWin = gameModel.hiddenLettersIndices.isEmpty()
            var coinsAmount = 0
            if (gameWin) {
                coinsAmount = gameModel.selectedDifficulty.winWorth
                gameModel.coins += coinsAmount
                androidInterface.saveSharedPreferencesValue(
                    SHARED_PREFERENCES_DATA_KEY_COINS,
                    gameModel.coins
                )
            }
            subscribers.forEach {
                it.onCorrectGuess(indices, gameWin, coinsAmount)
            }
        } else {
            gameModel.triesLeft--
            subscribers.forEach { it.onIncorrectGuess(gameModel.triesLeft <= 0) }
        }
    }

    private fun chooseTarget(gameModel: GameModel) {
        val categoryName = unusedWords.keys.random()
        val category = unusedWords[categoryName]
        val selectedTarget = TEST_WORD.ifEmpty { category!!.random() }
        gameModel.currentTarget = selectedTarget.reversed()
        gameModel.currentCategory = categoryName
        category!!.remove(selectedTarget)
        if (category.isEmpty()) {
            unusedWords.remove(categoryName)
        }
    }

    private fun decideHiddenLetters(model: GameModel) {
        val indices = mutableListOf<Int>()
        val phraseLength = model.currentTarget.length
        for (i in 0 until phraseLength) {
            if (model.currentTarget[i] != ' ') {
                indices.add(i)
            }
        }
        val toDropFromHiddenIndices =
            (phraseLength - phraseLength * model.selectedDifficulty.lettersToHideFactor).toInt()
        model.hiddenLettersIndices = indices.shuffled()
            .toMutableList()
            .drop(toDropFromHiddenIndices)
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
