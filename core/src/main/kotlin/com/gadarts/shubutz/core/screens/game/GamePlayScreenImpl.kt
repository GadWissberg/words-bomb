package com.gadarts.shubutz.core.screens.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.gadarts.shubutz.core.AndroidInterface
import com.gadarts.shubutz.core.DebugSettings
import com.gadarts.shubutz.core.GameLifeCycleManager
import com.gadarts.shubutz.core.business.GameLogicHandler
import com.gadarts.shubutz.core.model.Difficulties
import com.gadarts.shubutz.core.model.GameModel
import com.gadarts.shubutz.core.screens.GameScreen
import com.gadarts.shubutz.core.screens.game.view.GamePlayScreenView
import com.gadarts.shubutz.core.screens.menu.view.stage.GameStage

class GamePlayScreenImpl(
    private val globalHandlers: GlobalHandlers,
    private val lifeCycleManager: GameLifeCycleManager,
    private val android: AndroidInterface,
    private val stage: GameStage,
    private val selectedDifficulty: Difficulties,
) : GameScreen(), GamePlayScreen {


    private var roundsCounter = 0
    private val gameModel = createGameModel()
    private lateinit var gameLogicHandler: GameLogicHandler
    private lateinit var gamePlayScreenView: GamePlayScreenView

    private fun createGameModel(): GameModel {
        val coins: Int = if (DebugSettings.FORCE_NUMBER_OF_COINS >= 0) {
            DebugSettings.FORCE_NUMBER_OF_COINS
        } else {
            android.getSharedPreferencesIntValue(
                selectedDifficulty.sharedPreferencesCoinsKey,
                INITIAL_COINS_VALUE
            )
        }
        return GameModel(coins, selectedDifficulty)
    }





    override fun show() {
        gameLogicHandler = GameLogicHandler(
            globalHandlers.assetsManager.phrases[selectedDifficulty.phrasesFileName]!!,
            android,
            this
        )
        gameLogicHandler.beginGame(gameModel)
        gamePlayScreenView = createGamePlayScreenView()
        gamePlayScreenView.onShow()
    }

    private fun createGamePlayScreenView() = GamePlayScreenView(
        globalHandlers,
        gameModel,
        this,
        stage,
    )

    override fun render(delta: Float) {
        gamePlayScreenView.render(delta)
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            gamePlayScreenView.onPhysicalBackClicked()
        }
    }

    override fun resize(width: Int, height: Int) {
    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun hide() {
        gamePlayScreenView.clear()
    }

    override fun dispose() {
        gamePlayScreenView.dispose()
    }

    override fun onBrickClicked(letter: Char) {
        gameLogicHandler.onBrickClicked(letter, gameModel)
    }

    override fun onScreenEmpty() {
        if (gameModel.currentTargetData.hiddenLettersIndices.isNotEmpty()) return
        roundsCounter++
        gameLogicHandler.beginRound(gameModel)
        gamePlayScreenView.initializeForGameBegin()
    }

    override fun onRevealLetterButtonClicked(): Boolean {
        return gameLogicHandler.onRevealLetterButtonClicked(gameModel)
    }

    override fun onLetterRevealed(letter: Char, cost: Int) {
        gamePlayScreenView.onLetterRevealed(letter)
    }

    override fun onQuitSession() {
        lifeCycleManager.goToMenu()
    }

    override fun onClickedToRevealWordOnGameOver() {
        gameLogicHandler.onClickedToRevealWordOnGameOver(gameModel)
    }

    override fun onRevealedWordOnGameOver(cost: Int) {
        gamePlayScreenView.onRevealedWordOnGameOver()
    }

    override fun onIncorrectGuess(gameOver: Boolean) {
        gamePlayScreenView.onIncorrectGuess(gameOver)
    }

    override fun onCorrectGuess(
        indices: List<Int>,
        roundWin: Boolean,
        coinsAmount: Int,
        perfectBonusAchieved: Boolean,
        prevScore: Long
    ) {
        gamePlayScreenView.onCorrectGuess(
            indices,
            roundWin,
            perfectBonusAchieved,
            prevScore
        )
    }

    companion object {
        private const val INITIAL_COINS_VALUE = 32
    }


}