package com.gadarts.shubutz.core.screens.menu

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Screen
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.gadarts.shubutz.core.AndroidInterface
import com.gadarts.shubutz.core.GameLifeCycleManager
import com.gadarts.shubutz.core.model.assets.GameAssetManager
import com.gadarts.shubutz.core.screens.menu.view.MenuScreenView
import com.gadarts.shubutz.core.screens.menu.view.MenuScreenViewEventsSubscriber
import com.gadarts.shubutz.core.screens.menu.view.stage.GameStage

class MenuScreen(
    assetsManager: GameAssetManager,
    androidInterface: AndroidInterface,
    private val gameLifeCycleManager: GameLifeCycleManager,
    stage: GameStage,
) :
    Screen, MenuScreenViewEventsSubscriber {

    private val menuScreenView =
        MenuScreenView(assetsManager, androidInterface.versionName(), stage)

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
        menuScreenView.onHide()
    }

    override fun dispose() {
        menuScreenView.dispose()
    }

    override fun onLoadingAnimationReady() {
        gameLifeCycleManager.loadingDone = true
        menuScreenView.onLoadingAnimationReady(goToPlayScreenOnClick())
    }

    private fun goToPlayScreenOnClick() = object : ClickListener() {
        override fun clicked(event: InputEvent?, x: Float, y: Float) {
            super.clicked(event, x, y)
            gameLifeCycleManager.goToPlayScreen()
        }
    }

}