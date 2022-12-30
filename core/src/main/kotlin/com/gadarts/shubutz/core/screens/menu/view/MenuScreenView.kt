package com.gadarts.shubutz.core.screens.menu.view

import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Disposable
import com.gadarts.shubutz.core.Notifier
import com.gadarts.shubutz.core.model.assets.GameAssetManager
import com.gadarts.shubutz.core.screens.menu.LoadingAnimationHandler
import com.gadarts.shubutz.core.screens.menu.view.stage.GameStage

class MenuScreenView(
    private val assetsManager: GameAssetManager,
    versionName: String,
    private val stage: GameStage
) : Disposable, Notifier<MenuScreenViewEventsSubscriber> {


    private var loadingAnimationRenderer = LoadingAnimationHandler()
    private val menuScreenViewComponentsHandler = MenuScreenViewComponentsHandler(
        assetsManager,
        versionName,
        stage
    )

    override val subscribers = HashSet<MenuScreenViewEventsSubscriber>()

    fun onShow(loadingDone: Boolean, goToPlayScreenOnClick: ClickListener) {
        if (!loadingDone) {
            loadingAnimationRenderer.addLoadingAnimation(
                assetsManager,
                stage
            )
        } else {
            onLoadingAnimationReady(goToPlayScreenOnClick)
        }
    }

    fun render(delta: Float) {
        menuScreenViewComponentsHandler.render(delta)
        loadingAnimationRenderer.render(subscribers)
    }

    fun onHide() {
    }

    fun onLoadingAnimationReady(beginGameAction: ClickListener) {
        loadingAnimationRenderer.onLoadingAnimationReady()
        menuScreenViewComponentsHandler.onLoadingAnimationReady(beginGameAction)
    }

    class LetterArrivedAction(private val brickTable: BrickAnimation) : Action() {
        override fun act(delta: Float): Boolean {
            brickTable.ready = true
            return true
        }

    }

    class BrickAnimation : Table() {
        var ready: Boolean = false
    }

    companion object {
        const val LABEL_OPEN_ROOM = "התחל משחק"
        const val BUTTON_PADDING = 20F
    }

    override fun dispose() {
        menuScreenViewComponentsHandler.clear()
    }

}