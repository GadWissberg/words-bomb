package com.gadarts.shubutz.core

import com.badlogic.gdx.*
import com.badlogic.gdx.utils.viewport.FitViewport
import com.gadarts.shubutz.core.model.Difficulties
import com.gadarts.shubutz.core.model.assets.GameAssetManager
import com.gadarts.shubutz.core.model.assets.MusicDefinitions
import com.gadarts.shubutz.core.screens.game.GamePlayScreenImpl
import com.gadarts.shubutz.core.screens.menu.MenuScreen
import com.gadarts.shubutz.core.screens.menu.view.stage.GameStage


class ShubutzGame(private val android: AndroidInterface) : Game(), GameLifeCycleManager {

    private lateinit var stage: GameStage
    private lateinit var assetsManager: GameAssetManager
    private val soundPlayer = SoundPlayer()
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
        if (getScreen() is MenuScreen) return
        soundPlayer.playMusic(assetsManager.getMusic(MusicDefinitions.MENU))
        screen?.dispose()
        val menuScreen = MenuScreen(assetsManager, android, this, stage, soundPlayer)
        setScreen(menuScreen)
    }

    override fun goToPlayScreen(selectedDifficulty: Difficulties) {
        soundPlayer.playMusic(assetsManager.getMusic(MusicDefinitions.IN_GAME))
        setScreen(createGamePlayScreen(selectedDifficulty))
    }

    private fun createGamePlayScreen(selectedDifficulty: Difficulties) = GamePlayScreenImpl(
        assetsManager,
        this,
        android,
        stage,
        soundPlayer,
        selectedDifficulty
    )

}