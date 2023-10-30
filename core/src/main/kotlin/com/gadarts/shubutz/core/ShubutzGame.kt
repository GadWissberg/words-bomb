package com.gadarts.shubutz.core

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Screen
import com.badlogic.gdx.utils.viewport.StretchViewport
import com.gadarts.shubutz.core.model.GameModes
import com.gadarts.shubutz.core.model.assets.GameAssetManager
import com.gadarts.shubutz.core.screens.GameScreen
import com.gadarts.shubutz.core.screens.game.GamePlayScreenImpl
import com.gadarts.shubutz.core.screens.game.GlobalHandlers
import com.gadarts.shubutz.core.screens.menu.MenuScreenImpl
import com.gadarts.shubutz.core.screens.menu.view.stage.GameStage
import pl.mk5.gdx.fireapp.GdxFIRApp


class ShubutzGame(private val android: AndroidInterface) : Game(), GameLifeCycleManager {

    private lateinit var globalHandlers: GlobalHandlers
    private lateinit var stage: GameStage
    override var loadingDone: Boolean = false

    override fun create() {
        GdxFIRApp.inst().configure()
        Gdx.input.setCatchKey(Input.Keys.BACK, true)
        val assetsManager = GameAssetManager()
        assetsManager.loadAssets()
        assetsManager.finishLoading()
        createStage(assetsManager)
        globalHandlers = GlobalHandlers(android, stage, assetsManager)
        goToMenu()
        android.initializeAds { gameReady() }
    }

    override fun setScreen(screen: Screen?) {
        stage.openDialogs.forEach { it.value.remove() }
        stage.openDialogs.clear()
        val currentScreen = this.screen
        super.setScreen(screen)
        currentScreen?.dispose()
    }

    override fun dispose() {
        super.dispose()
        stage.dispose()
        globalHandlers.dispose()
    }


    override fun goToMenu() {
        if (getScreen() is MenuScreenImpl) return
        screen?.dispose()
        val screen = MenuScreenImpl(globalHandlers, android, this, stage)
        setScreen(screen)
    }

    override fun onSuccessfulPurchase(products: MutableList<String>) {
        if (screen == null) return

        (screen as GameScreen).onSuccessfulPurchase(products)
    }

    override fun onFailedPurchase(message: String) {
        if (screen == null) return

        (screen as GameScreen).onFailedPurchase(message)
    }

    override fun onLeaderboardClosed() {
        if (screen == null) return

        (screen as GameScreen).onLeaderboardClosed()
    }

    override fun restart() {
        screen?.dispose()
        val screen = MenuScreenImpl(globalHandlers, android, this, stage)
        setScreen(screen)
    }

    override fun goToPlayScreen(mode: GameModes) {
        setScreen(createGamePlayScreen(mode))
    }

    fun onRewardForVideoAd(rewardAmount: Int) {
        if (screen == null) return

        (screen as GameScreen).onRewardForVideoAd(rewardAmount)
    }

    private fun gameReady() {
        if (screen == null) return

        loadingDone = true
        (screen as MenuScreenImpl).onGameReady()
    }

    private fun createStage(assetsManager: GameAssetManager) {
        stage =
            GameStage(
                StretchViewport(RESOLUTION_WIDTH.toFloat(), RESOLUTION_HEIGHT.toFloat()),
                assetsManager,
            )
        stage.setDebugInvisible(DebugSettings.SHOW_UI_BORDERS)
        Gdx.input.inputProcessor = stage
    }

    private fun createGamePlayScreen(gameMode: GameModes) =
        GamePlayScreenImpl(
            globalHandlers,
            this,
            this.android,
            stage,
            gameMode,
        )

    companion object {
        const val RESOLUTION_WIDTH = 1080
        const val RESOLUTION_HEIGHT = 2400
        var lastChampionsFetch = 0L
    }
}