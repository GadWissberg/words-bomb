package com.gadarts.wordsbomb.core

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.Screen
import com.gadarts.wordsbomb.core.model.assets.GameAssetManager
import com.gadarts.wordsbomb.core.screens.game.GamePlayScreen
import com.gadarts.wordsbomb.core.screens.game.GamePlayScreenEventsSubscriber
import com.gadarts.wordsbomb.core.screens.menu.MenuScreen
import com.gadarts.wordsbomb.core.screens.menu.MenuScreenEventsSubscriber


class WordsBombGame(private val androidInterface: AndroidInterface) : Game(),
    MenuScreenEventsSubscriber, GamePlayScreenEventsSubscriber {

    private lateinit var assetsManager: GameAssetManager

    override fun create() {
        loadAssets()
        Gdx.input.inputProcessor = InputMultiplexer()
        val menuScreen = MenuScreen(assetsManager, androidInterface)
        menuScreen.subscribeForEvents(this)
        setScreen(menuScreen)
    }

    override fun setScreen(screen: Screen?) {
        val currentScreen = this.screen
        super.setScreen(screen)
        currentScreen?.dispose()
    }

    private fun loadAssets() {
        assetsManager = GameAssetManager()
        assetsManager.loadAssets()
        assetsManager.finishLoading()
    }

    override fun onBeginGame() {
        val gameplayScreen = GamePlayScreen(assetsManager, androidInterface)
        gameplayScreen.subscribeForEvents(this)
        setScreen(gameplayScreen)
    }

}