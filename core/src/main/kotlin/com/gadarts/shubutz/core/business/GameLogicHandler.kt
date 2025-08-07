package com.gadarts.shubutz.core.business

import com.badlogic.gdx.Gdx
import com.gadarts.shubutz.core.AndroidInterface
import com.gadarts.shubutz.core.DebugSettings
import com.gadarts.shubutz.core.DebugSettings.TEST_PHRASE
import com.gadarts.shubutz.core.model.GameModel
import com.gadarts.shubutz.core.model.GameModel.Companion.DISPLAY_TARGET_COST
import com.gadarts.shubutz.core.model.GameModel.Companion.allowedLetters
import com.gadarts.shubutz.core.model.Phrase
import com.gadarts.shubutz.core.screens.game.GamePlayScreen
import kotlin.math.max
import kotlin.random.Random

class GameLogicHandler(
    private val phrases: HashMap<String, ArrayList<Phrase>>,
    private val androidInterface: AndroidInterface,
    private val gamePlayScreen: GamePlayScreen,
) {

    private var unusedPhrases: HashMap<String, ArrayList<Phrase>> = HashMap(phrases)

    fun beginGame(gameModel: GameModel) {
        unusedPhrases = phrases
        unusedPhrases.forEach {
            unusedPhrases[it.key] = ArrayList(it.value.filter { p ->
                p.value.length >= gameModel.selectedDifficulty.minimumLength
            })
        }
        beginRound(gameModel)
    }

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
        val indices = gameModel.currentTargetData.hiddenLettersIndices.filter {
            val currentLetter = gameModel.currentTargetData.currentPhrase[it]
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
        gameModel.currentTargetData.hiddenLettersIndices.removeAll(indices.toSet())
        val gameWin = gameModel.currentTargetData.hiddenLettersIndices.isEmpty()
        var coinsAmount = 0
        var perfectBonus = false
        var scoreWin = 0
        if (gameWin) {
            coinsAmount = gameModel.selectedDifficulty.winWorth
            perfectBonus = isPerfectBonus(gameModel)
            coinsAmount += if (perfectBonus) max(coinsAmount / 2, 1) else 0
            scoreWin = if (perfectBonus) 2 else 1
            addCoinsValueAndSave(gameModel, coinsAmount)
        }
        val prevScore = gameModel.score
        gameModel.score += scoreWin
        gamePlayScreen.onCorrectGuess(indices, gameWin, coinsAmount, perfectBonus, prevScore)
    }

    private fun isPerfectBonus(gameModel: GameModel): Boolean {
        return gameModel.selectedDifficulty.perfectBonusEnabled && gameModel.selectedDifficulty.tries == gameModel.triesLeft
    }

    private fun addCoinsValueAndSave(
        gameModel: GameModel,
        coinsAmount: Int
    ) {
        gameModel.coins += coinsAmount
        androidInterface.saveSharedPreferencesIntValue(
            gameModel.selectedDifficulty.sharedPreferencesCoinsKey,
            gameModel.coins
        )
    }

    private fun chooseTarget(gameModel: GameModel) {
        val categoryName = unusedPhrases.keys.random(Random(System.currentTimeMillis()))
        val category = unusedPhrases[categoryName]
        val selected = if (TEST_PHRASE.value.isNotEmpty()) TEST_PHRASE else category!!.random()
        gameModel.setNewPhrase(selected.value.reversed())
        gameModel.currentTargetData.currentCategory = categoryName
        category!!.remove(selected)
        if (category.isEmpty()) {
            unusedPhrases.remove(categoryName)
        }
    }

    private fun decideHiddenLetters(model: GameModel) {
        val indices = mutableListOf<Int>()
        for (i in 0 until model.currentTargetData.currentPhrase.length) {
            if (model.currentTargetData.currentPhrase[i] != ' ') {
                indices.add(i)
            }
        }
        model.currentTargetData.hiddenLettersIndices = indices.shuffled()
            .toMutableList()
            .drop((model.currentTargetData.currentPhrase.length - model.currentTargetData.currentPhrase.length * model.selectedDifficulty.lettersToHideFactor).toInt())
            .toMutableList()
    }

    fun onPurchasedCoins(gameModel: GameModel, amount: Int) {
        addCoinsValueAndSave(gameModel, amount)
    }

    fun onRevealLetterButtonClicked(gameModel: GameModel): Boolean {
        var result = false
        if (gameModel.coins - gameModel.selectedDifficulty.revealLetterCost >= 0) {
            if (gameModel.currentTargetData.hiddenLettersIndices.isNotEmpty()) {
                revealLetter(gameModel)
                result = true
            }
        } else {
            gamePlayScreen.onLetterRevealFailedNotEnoughCoins()
        }
        return result
    }

    private fun revealLetter(gameModel: GameModel) {
        addCoinsValueAndSave(gameModel, -gameModel.selectedDifficulty.revealLetterCost)
        val letter =
            gameModel.currentTargetData.currentPhrase[gameModel.currentTargetData.hiddenLettersIndices.random()]
        gameModel.helpAvailable = false
        gamePlayScreen.onLetterRevealed(
            suffixLettersReverse[letter] ?: letter,
            gameModel.selectedDifficulty.revealLetterCost
        )
    }

    fun onRewardForVideoAd(rewardAmount: Int, gameModel: GameModel) {
        addCoinsValueAndSave(gameModel, rewardAmount)
    }

    fun onBuyCoinsDialogOpened(onLoaded: () -> Unit) {
        Gdx.app.postRunnable {
            androidInterface.loadVideoAd(onLoaded)
        }
    }

    fun onClickedToRevealWordOnGameOver(gameModel: GameModel) {
        if (gameModel.coins >= DISPLAY_TARGET_COST) {
            addCoinsValueAndSave(gameModel, -DISPLAY_TARGET_COST)
            gamePlayScreen.onRevealedWordOnGameOver(DISPLAY_TARGET_COST)
        } else {
            gamePlayScreen.onFailedToRevealWordOnGameOver()
        }
    }

    companion object {

        private val suffixLetters = mapOf(
            'פ' to 'ף',
            'כ' to 'ך',
            'נ' to 'ן',
            'צ' to 'ץ',
            'מ' to 'ם'
        )
        private val suffixLettersReverse = suffixLetters.entries.associate { it.value to it.key }
    }
}
