package com.gadarts.shubutz.core.screens.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Screen
import com.gadarts.shubutz.core.AndroidInterface
import com.gadarts.shubutz.core.GameLifeCycleManager
import com.gadarts.shubutz.core.SoundPlayer
import com.gadarts.shubutz.core.business.GameLogicHandler
import com.gadarts.shubutz.core.business.GameLogicHandlerEventsSubscriber
import com.gadarts.shubutz.core.model.Difficulties
import com.gadarts.shubutz.core.model.GameModel
import com.gadarts.shubutz.core.model.assets.GameAssetManager
import com.gadarts.shubutz.core.screens.game.view.GamePlayScreenView
import com.gadarts.shubutz.core.screens.menu.view.stage.GameStage

class GamePlayScreenImpl(
    private val assetsManager: GameAssetManager,
    private val lifeCycleManager: GameLifeCycleManager,
    private val android: AndroidInterface,
    private val stage: GameStage,
    private val soundPlayer: SoundPlayer,
    selectedDifficulty: Difficulties,
) : Screen, GameLogicHandlerEventsSubscriber, GamePlayScreen {


    private val gameModel =
        GameModel(
            android.getSharedPreferencesValue(GameLogicHandler.SHARED_PREFERENCES_DATA_KEY_COINS),
            selectedDifficulty
        )
    private lateinit var gameLogicHandler: GameLogicHandler
    private lateinit var gamePlayScreenView: GamePlayScreenView

    override fun show() {
        gameLogicHandler = GameLogicHandler(assetsManager.phrases, android)
        gameLogicHandler.beginGame(gameModel)
        gameLogicHandler.subscribeForEvents(this)
        gamePlayScreenView = createGamePlayScreenView()
        gamePlayScreenView.onShow()
    }

    private fun createGamePlayScreenView() = GamePlayScreenView(
        assetsManager,
        gameModel,
        this,
        stage,
        soundPlayer
    )

    override fun render(delta: Float) {
        gamePlayScreenView.render(delta)
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            lifeCycleManager.goToMenu()
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
        if (gameModel.hiddenLettersIndices.isNotEmpty()) return
        gameLogicHandler.beginRound(gameModel)
        gamePlayScreenView.onGameBegin()
    }

    override fun onClickedBackButton() {
        lifeCycleManager.goToMenu()
    }

    override fun onCorrectGuess(indices: List<Int>, gameWin: Boolean, coinsAmount: Int) {
        gamePlayScreenView.onCorrectGuess(indices, gameWin, coinsAmount)
    }

    override fun onIncorrectGuess(gameOver: Boolean) {
        gamePlayScreenView.onIncorrectGuess(gameOver)
    }

    override fun onGameOverAnimationDone() {
        lifeCycleManager.goToMenu()
    }


}