package com.gadarts.shubutz.core

import com.badlogic.gdx.*
import com.badlogic.gdx.utils.viewport.StretchViewport
import com.gadarts.shubutz.core.model.Difficulties
import com.gadarts.shubutz.core.model.assets.GameAssetManager
import com.gadarts.shubutz.core.screens.GameScreen
import com.gadarts.shubutz.core.screens.game.GamePlayScreenImpl
import com.gadarts.shubutz.core.screens.game.GlobalHandlers
import com.gadarts.shubutz.core.screens.game.view.EffectsHandler
import com.gadarts.shubutz.core.screens.menu.MenuScreenImpl
import com.gadarts.shubutz.core.screens.menu.view.stage.GameStage


class ShubutzGame(private val android: AndroidInterface) : Game(), GameLifeCycleManager {

    private lateinit var effectsHandler: EffectsHandler
    private lateinit var globalHandlers: GlobalHandlers
    private lateinit var stage: GameStage
    override var loadingDone: Boolean = false

    override fun create() {
        Gdx.input.setCatchKey(Input.Keys.BACK, true)
        val assetsManager = GameAssetManager()
        assetsManager.loadAssets()
        assetsManager.finishLoading()
        createStage(assetsManager)
        globalHandlers = GlobalHandlers(android, stage, assetsManager)
        goToMenu()
        android.initializeAds { gameReady() }
    }

    private fun gameReady() {
        loadingDone = true
        (screen as MenuScreenImpl).onGameReady()
    }

    private fun createStage(assetsManager: GameAssetManager) {
        stage =
            GameStage(
                StretchViewport(RESOLUTION_WIDTH.toFloat(), RESOLUTION_HEIGHT.toFloat()),
                assetsManager
            )
        stage.setDebugInvisible(DebugSettings.SHOW_UI_BORDERS)
        Gdx.input.inputProcessor = stage
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
        val menuScreenImpl = MenuScreenImpl(globalHandlers, android, this, stage)
        setScreen(menuScreenImpl)
    }

    override fun goToPlayScreen(selectedDifficulty: Difficulties) {
        setScreen(createGamePlayScreen(selectedDifficulty))
    }

    override fun onSuccessfulPurchase(products: MutableList<String>) {
        (screen as GameScreen).onSuccessfulPurchase(products)
    }

    override fun onFailedPurchase(message: String) {
        (screen as GameScreen).onFailedPurchase(message)
    }

    override fun onLeaderboardClosed() {
        (screen as GameScreen).onLeaderboardClosed()
    }

    fun onRewardForVideoAd(rewardAmount: Int) {
        (screen as GameScreen).onRewardForVideoAd(rewardAmount)
    }

    private fun createGamePlayScreen(selectedDifficulty: Difficulties) =
        GamePlayScreenImpl(
            globalHandlers,
            this,
            this.android,
            stage,
            selectedDifficulty,
            effectsHandler,
        )

    companion object {
        const val RESOLUTION_WIDTH = 1080
        const val RESOLUTION_HEIGHT = 2400
    }
}