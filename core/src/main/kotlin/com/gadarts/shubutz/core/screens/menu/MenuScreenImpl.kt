package com.gadarts.shubutz.core.screens.menu

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.gadarts.shubutz.core.AndroidInterface
import com.gadarts.shubutz.core.GameLifeCycleManager
import com.gadarts.shubutz.core.model.Difficulties
import com.gadarts.shubutz.core.screens.GameScreen
import com.gadarts.shubutz.core.screens.game.GlobalHandlers
import com.gadarts.shubutz.core.screens.menu.view.MenuScreenView
import com.gadarts.shubutz.core.screens.menu.view.stage.GameStage

class MenuScreenImpl(
    globalHandlers: GlobalHandlers,
    private val androidInterface: AndroidInterface,
    private val gameLifeCycleManager: GameLifeCycleManager,
    stage: GameStage,
) :
    GameScreen(), MenuScreen {

    private val menuScreenView = MenuScreenView(
        globalHandlers.assetsManager,
        androidInterface.versionName(),
        stage,
        globalHandlers.soundPlayer,
        this,
        androidInterface
    )

    override fun onSuccessfulPurchase(products: MutableList<String>) {

    }

    override fun onFailedPurchase(message: String) {

    }

    override fun onRewardForVideoAd(rewardAmount: Int) {
    }

    override fun show() {
        menuScreenView.onShow(gameLifeCycleManager.loadingDone, goToPlayScreenOnClick())
        androidInterface.hideBannerAd()
    }

    override fun render(delta: Float) {
        menuScreenView.render(delta)
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            Gdx.app.exit()
        }
    }

    override fun resize(width: Int, height: Int) {
    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun hide() {
        menuScreenView.clearScreen()
    }

    override fun dispose() {
        menuScreenView.dispose()
    }

    override fun onLoadingAnimationFinished() {
        if (gameLifeCycleManager.loadingDone) {
            menuScreenView.finishLoadingAnimationAndDisplayMenu(goToPlayScreenOnClick())
        }
    }

    fun onGameReady() {
        if (menuScreenView.loadingAnimationRenderer.loadingAnimationFinished) {
            menuScreenView.finishLoadingAnimationAndDisplayMenu(goToPlayScreenOnClick())
        }
    }

    private fun goToPlayScreenOnClick() = object : BeginGameAction {
        override fun begin(selectedDifficulty: Difficulties) {
            gameLifeCycleManager.goToPlayScreen(selectedDifficulty)
        }
    }

}