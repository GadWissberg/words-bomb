package com.gadarts.shubutz.core.screens.game

import com.badlogic.gdx.Screen
import com.gadarts.shubutz.core.AndroidInterface
import com.gadarts.shubutz.core.business.BusinessLogicHandler
import com.gadarts.shubutz.core.Notifier
import com.gadarts.shubutz.core.business.BusinessLogicHandlerEventsSubscriber
import com.gadarts.shubutz.core.model.GameModel
import com.gadarts.shubutz.core.model.assets.GameAssetManager
import com.gadarts.shubutz.core.screens.game.view.GamePlayScreenView
import com.gadarts.shubutz.core.screens.game.view.GamePlayScreenViewEventsSubscriber

class GamePlayScreen(
    private val assetsManager: GameAssetManager,
    private val androidInterface: AndroidInterface,
) :
    Screen, Notifier<GamePlayScreenEventsSubscriber>, GamePlayScreenViewEventsSubscriber,
    BusinessLogicHandlerEventsSubscriber {


    private val gameModel = GameModel()
    private val businessLogicHandler = BusinessLogicHandler()
    private lateinit var gamePlayScreenView: GamePlayScreenView
    override val subscribers = HashSet<GamePlayScreenEventsSubscriber>()

    override fun show() {
        businessLogicHandler.beginGame(gameModel, assetsManager.words)
        businessLogicHandler.subscribeForEvents(this)
        gamePlayScreenView = GamePlayScreenView(assetsManager, gameModel)
        gamePlayScreenView.subscribeForEvents(this)
        gamePlayScreenView.onShow()
    }

    override fun render(delta: Float) {
        gamePlayScreenView.render(delta)
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

    override fun subscribeForEvents(subscriber: GamePlayScreenEventsSubscriber) {
        subscribers.add(subscriber)
    }

    override fun onBrickClicked(letter: Char) {
        businessLogicHandler.onBrickClicked(letter, gameModel)
    }

    override fun onScreenEmpty() {
        businessLogicHandler.beginGame(gameModel, assetsManager.words)
        gamePlayScreenView.onGameBegin()
    }

    override fun onGuessSuccess(indices: List<Int>, gameWin: Boolean) {
        gamePlayScreenView.onGuessSuccess(indices, gameWin)
    }

    override fun onGuessFail(gameOver: Boolean) {
        gamePlayScreenView.onGuessFail(gameOver)
    }


}