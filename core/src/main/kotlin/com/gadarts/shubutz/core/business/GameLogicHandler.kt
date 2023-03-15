package com.gadarts.shubutz.core.business

import com.gadarts.shubutz.core.AndroidInterface
import com.gadarts.shubutz.core.DebugSettings
import com.gadarts.shubutz.core.DebugSettings.TEST_PHRASE
import com.gadarts.shubutz.core.model.GameModel
import com.gadarts.shubutz.core.model.GameModel.Companion.allowedLetters
import com.gadarts.shubutz.core.model.Phrase
import com.gadarts.shubutz.core.screens.game.GamePlayScreen

/**
 * Responsible to take care of the actual game's rules.
 */
class GameLogicHandler(
    private val phrases: HashMap<String, ArrayList<Phrase>>,
    private val androidInterface: AndroidInterface,
    private val gamePlayScreen: GamePlayScreen,
) {

    private var unusedPhrases: HashMap<String, ArrayList<Phrase>> = HashMap(phrases)

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
        gameModel.triesLeft =
            if (DebugSettings.NUMBER_OF_TRIES > 0) DebugSettings.NUMBER_OF_TRIES else gameModel.selectedDifficulty.tries
        chooseTarget(gameModel)
        decideHiddenLetters(gameModel)
        gameModel.options = allowedLetters.toMutableList()
        gameModel.options.reversed()
    }

    fun onBrickClicked(selectedLetter: Char, gameModel: GameModel) {
        val indices = gameModel.hiddenLettersIndices.filter {
            val currentLetter = gameModel.currentPhrase[it]
            currentLetter == selectedLetter || suffixLetters[selectedLetter] == currentLetter
        }
        if (indices.isNotEmpty()) {
            correctGuess(gameModel, indices)
        } else {
            gameModel.triesLeft--
            gamePlayScreen.onIncorrectGuess(gameModel.triesLeft <= 0)
        }
    }

    private fun correctGuess(
        gameModel: GameModel,
        indices: List<Int>
    ) {
        gameModel.hiddenLettersIndices.removeAll(indices)
        val gameWin = gameModel.hiddenLettersIndices.isEmpty()
        var coinsAmount = 0
        if (gameWin) {
            coinsAmount = gameModel.selectedDifficulty.winWorth
            addCoinsValueAndSave(gameModel, coinsAmount)
        }
        gamePlayScreen.onCorrectGuess(indices, gameWin, coinsAmount)
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
        gameModel.currentPhrase = selected.value.reversed()
        gameModel.currentCategory = categoryName
        category!!.remove(selected)
        if (category.isEmpty()) {
            unusedPhrases.remove(categoryName)
        }
    }

    private fun decideHiddenLetters(model: GameModel) {
        val indices = mutableListOf<Int>()
        val phraseLength = model.currentPhrase.length
        for (i in 0 until phraseLength) {
            if (model.currentPhrase[i] != ' ') {
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

    fun onRevealLetterButtonClicked(gameModel: GameModel) {
        if (gameModel.coins - REVEAL_LETTER_COST >= 0) {
            if (gameModel.hiddenLettersIndices.isNotEmpty()) {
                gameModel.coins -= REVEAL_LETTER_COST
                gamePlayScreen.onLetterRevealed(
                    gameModel.currentPhrase[gameModel.hiddenLettersIndices.random()],
                    REVEAL_LETTER_COST
                )
            }
        } else {
            gamePlayScreen.onLetterRevealFailedNotEnoughCoins()
        }
    }

    companion object {
        const val SHARED_PREFERENCES_DATA_KEY_COINS = "coins"
        const val REVEAL_LETTER_COST = 8

        private val suffixLetters = mapOf(
            'פ' to 'ף',
            'כ' to 'ך',
            'נ' to 'ן',
            'צ' to 'ץ',
            'מ' to 'ם'
        )
    }
}
