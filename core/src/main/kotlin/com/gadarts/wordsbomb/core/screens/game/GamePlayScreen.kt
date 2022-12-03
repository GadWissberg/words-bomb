package com.gadarts.wordsbomb.core.screens.game

import com.badlogic.gdx.Screen
import com.gadarts.wordsbomb.core.AndroidInterface
import com.gadarts.wordsbomb.core.GameLogicHandler
import com.gadarts.wordsbomb.core.Notifier
import com.gadarts.wordsbomb.core.model.GameModel
import com.gadarts.wordsbomb.core.model.assets.GameAssetManager
import com.gadarts.wordsbomb.core.screens.game.view.GamePlayScreenView
import com.gadarts.wordsbomb.core.screens.game.view.GamePlayScreenViewEventsSubscriber

class GamePlayScreen(
    assetsManager: GameAssetManager,
    private val androidInterface: AndroidInterface,
) :
    Screen, Notifier<GamePlayScreenEventsSubscriber>, GamePlayScreenViewEventsSubscriber {


    private val gameModel = GameModel()
    private val gameLogicHandler = GameLogicHandler()
    private val gamePlayScreenView = GamePlayScreenView(assetsManager)
    override val subscribers = HashSet<GamePlayScreenEventsSubscriber>()

    override fun show() {
        gameLogicHandler.beginGame(gameModel)
        gamePlayScreenView.subscribeForEvents(this)
        gamePlayScreenView.onShow(gameModel)
    }

    override fun render(delta: Float) {
        gamePlayScreenView.render()
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

}