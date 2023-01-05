package com.gadarts.shubutz.core.screens.menu

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Screen
import com.gadarts.shubutz.core.AndroidInterface
import com.gadarts.shubutz.core.GameLifeCycleManager
import com.gadarts.shubutz.core.SoundPlayer
import com.gadarts.shubutz.core.model.Difficulties
import com.gadarts.shubutz.core.model.assets.GameAssetManager
import com.gadarts.shubutz.core.screens.menu.view.MenuScreenView
import com.gadarts.shubutz.core.screens.menu.view.MenuScreenViewEventsSubscriber
import com.gadarts.shubutz.core.screens.menu.view.stage.GameStage

class MenuScreen(
    assetsManager: GameAssetManager,
    androidInterface: AndroidInterface,
    private val gameLifeCycleManager: GameLifeCycleManager,
    stage: GameStage,
    soundPlayer: SoundPlayer,
) :
    Screen, MenuScreenViewEventsSubscriber {

    private val menuScreenView =
        MenuScreenView(assetsManager, androidInterface.versionName(), stage, soundPlayer)

    override fun show() {
        menuScreenView.subscribeForEvents(this)
        menuScreenView.onShow(gameLifeCycleManager.loadingDone, goToPlayScreenOnClick())
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

    override fun onLoadingAnimationReady() {
        gameLifeCycleManager.loadingDone = true
        menuScreenView.finishLoadingAnimationAndDisplayMenu(goToPlayScreenOnClick())
    }

    private fun goToPlayScreenOnClick() = object : BeginGameAction {
        override fun begin(selectedDifficulty: Difficulties) {
            gameLifeCycleManager.goToPlayScreen(selectedDifficulty)
        }
    }

}