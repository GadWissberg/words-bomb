package com.gadarts.shubutz.core.business

import com.gadarts.shubutz.core.AndroidInterface
import com.gadarts.shubutz.core.DebugSettings.TEST_PHRASE
import com.gadarts.shubutz.core.Notifier
import com.gadarts.shubutz.core.model.GameModel
import com.gadarts.shubutz.core.model.GameModel.Companion.allowedLetters
import com.gadarts.shubutz.core.model.Phrase

/**
 * Responsible to take care of the actual game's rules.
 */
class GameLogicHandler(
    private val phrases: HashMap<String, ArrayList<Phrase>>,
    private val androidInterface: AndroidInterface,
) :
    Notifier<GameLogicHandlerEventsSubscriber> {

    private var unusedPhrases: HashMap<String, ArrayList<Phrase>> = HashMap(phrases)
    override val subscribers = HashSet<GameLogicHandlerEventsSubscriber>()

    /**
     * Initializes the bomb's counter, decide the letter to be hidden and initialize the options
     * letters array.
     */
    fun beginGame(gameModel: GameModel) {
        unusedPhrases = phrases
        unusedPhrases.forEach {
            unusedPhrases[it.key] = ArrayList(it.value.filter { p ->
                p.value.length >= gameModel.selectedDifficulty.regularPhrasesMinimumLength
            })
        }
        beginRound(gameModel)
    }

    /**
     * Initializes the bomb's counter, decide the letter to be hidden and initialize the options
     * letters array.
     */
    fun beginRound(gameModel: GameModel) {
        if (unusedPhrases.isEmpty()) {
            unusedPhrases = phrases
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
                addCoinsValueAndSave(gameModel, gameModel.coins + coinsAmount)
            }
            subscribers.forEach {
                it.onCorrectGuess(indices, gameWin, coinsAmount)
            }
        } else {
            gameModel.triesLeft--
            subscribers.forEach { it.onIncorrectGuess(gameModel.triesLeft <= 0) }
        }
    }

    private fun addCoinsValueAndSave(
        gameModel: GameModel,
        coinsAmount: Int
    ) {
        gameModel.coins += coinsAmount
        androidInterface.saveSharedPreferencesValue(
            SHARED_PREFERENCES_DATA_KEY_COINS,
            gameModel.coins
        )
    }

    private fun chooseTarget(gameModel: GameModel) {
        val categoryName = unusedPhrases.keys.random()
        val category = unusedPhrases[categoryName]
        val selected = if (TEST_PHRASE.value.isNotEmpty()) TEST_PHRASE else category!!.random()
        gameModel.currentTarget = selected.value.reversed()
        gameModel.currentCategory = categoryName
        category!!.remove(selected)
        if (category.isEmpty()) {
            unusedPhrases.remove(categoryName)
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

    fun onPurchasedCoins(gameModel: GameModel, amount: Int) {
        addCoinsValueAndSave(gameModel, amount)
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
