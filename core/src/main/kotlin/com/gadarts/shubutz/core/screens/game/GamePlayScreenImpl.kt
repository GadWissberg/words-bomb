package com.gadarts.shubutz.core.screens.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Screen
import com.gadarts.shubutz.core.GameLifeCycleManager
import com.gadarts.shubutz.core.business.BusinessLogicHandler
import com.gadarts.shubutz.core.business.BusinessLogicHandlerEventsSubscriber
import com.gadarts.shubutz.core.model.GameModel
import com.gadarts.shubutz.core.model.assets.GameAssetManager
import com.gadarts.shubutz.core.screens.game.view.GamePlayScreenView

class GamePlayScreenImpl(
    private val assetsManager: GameAssetManager,
    private val lifeCycleManager: GameLifeCycleManager,
) : Screen, BusinessLogicHandlerEventsSubscriber, GamePlayScreen {


    private val gameModel = GameModel()
    private val businessLogicHandler = BusinessLogicHandler()
    private lateinit var gamePlayScreenView: GamePlayScreenView

    override fun show() {
        businessLogicHandler.beginGame(gameModel, assetsManager.words)
        businessLogicHandler.subscribeForEvents(this)
        gamePlayScreenView = GamePlayScreenView(assetsManager, gameModel, this)
        gamePlayScreenView.onShow()
    }

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
        gamePlayScreenView.onHide()
    }

    override fun dispose() {
        gamePlayScreenView.dispose()
    }

    override fun onBrickClicked(letter: Char) {
        businessLogicHandler.onBrickClicked(letter, gameModel)
    }

    override fun onScreenEmpty() {
        businessLogicHandler.beginGame(gameModel, assetsManager.words)
        gamePlayScreenView.onGameBegin()
    }

    override fun onClickedBackButton() {
        lifeCycleManager.goToMenu()
    }

    override fun onGuessSuccess(indices: List<Int>, gameWin: Boolean) {
        gamePlayScreenView.onGuessSuccess(indices, gameWin)
    }

    override fun onGuessFail(gameOver: Boolean) {
        gamePlayScreenView.onGuessFail(gameOver)
    }

    override fun onGameOverAnimationDone() {
        lifeCycleManager.goToMenu()
    }


}