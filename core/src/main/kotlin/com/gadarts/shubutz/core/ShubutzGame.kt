package com.gadarts.shubutz.core

import com.badlogic.gdx.*
import com.badlogic.gdx.utils.viewport.FitViewport
import com.gadarts.shubutz.core.business.GameLogicHandler
import com.gadarts.shubutz.core.model.assets.GameAssetManager
import com.gadarts.shubutz.core.screens.game.GamePlayScreenImpl
import com.gadarts.shubutz.core.screens.menu.MenuScreen
import com.gadarts.shubutz.core.screens.menu.view.stage.GameStage


class ShubutzGame(private val androidInterface: AndroidInterface) : Game(), GameLifeCycleManager {

    private lateinit var stage: GameStage
    private lateinit var assetsManager: GameAssetManager
    override var loadingDone: Boolean = false

    override fun create() {
        Gdx.input.setCatchKey(Input.Keys.BACK, true)
        loadAssets()
        Gdx.input.inputProcessor = InputMultiplexer()
        createStage()
        goToMenu()
    }

    private fun createStage() {
        stage = GameStage(
            FitViewport(Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat()),
            assetsManager
        )
        stage.setDebugInvisible(DebugSettings.SHOW_UI_BORDERS)
        Gdx.input.inputProcessor = stage
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
        stage.dispose()
        assetsManager.dispose()
    }

    override fun goToMenu() {
        screen?.dispose()
        val menuScreen = MenuScreen(assetsManager, androidInterface, this, stage)
        setScreen(menuScreen)
    }

    override fun goToPlayScreen() {
        screen?.dispose()
        val gameplayScreenImpl = GamePlayScreenImpl(
            assetsManager,
            this,
            androidInterface.getSharedPreferencesValue(GameLogicHandler.SHARED_PREFERENCES_DATA_KEY_COINS),
            androidInterface,
            stage
        )
        setScreen(gameplayScreenImpl)
    }

}