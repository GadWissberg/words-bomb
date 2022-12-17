package com.gadarts.shubutz.core

import com.badlogic.gdx.*
import com.gadarts.shubutz.core.model.assets.GameAssetManager
import com.gadarts.shubutz.core.screens.game.GamePlayScreenImpl
import com.gadarts.shubutz.core.screens.menu.MenuScreen


class ShubutzGame(private val androidInterface: AndroidInterface) : Game(), GameLifeCycleManager {

    private lateinit var assetsManager: GameAssetManager
    override var loadingDone: Boolean = false

    override fun create() {
        Gdx.input.setCatchKey(Input.Keys.BACK, true)
        loadAssets()
        Gdx.input.inputProcessor = InputMultiplexer()
        goToMenu()
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

    override fun dispose() {
        super.dispose()
        assetsManager.dispose()
    }

    override fun goToMenu() {
        screen?.dispose()
        val menuScreen = MenuScreen(assetsManager, androidInterface, this)
        setScreen(menuScreen)
    }

    override fun goToPlayScreen() {
        screen?.dispose()
        val gameplayScreenImpl = GamePlayScreenImpl(assetsManager, this)
        setScreen(gameplayScreenImpl)
    }

}