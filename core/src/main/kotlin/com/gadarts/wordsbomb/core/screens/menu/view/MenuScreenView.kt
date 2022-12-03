package com.gadarts.wordsbomb.core.screens.menu.view

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Disposable
import com.gadarts.wordsbomb.core.Notifier
import com.gadarts.wordsbomb.core.model.assets.FontsDefinitions
import com.gadarts.wordsbomb.core.model.assets.GameAssetManager
import com.gadarts.wordsbomb.core.model.assets.TexturesDefinitions
import com.gadarts.wordsbomb.core.screens.menu.LoadingAnimationHandler

class MenuScreenView(private val assetsManager: GameAssetManager) : Disposable,
    Notifier<MenuScreenViewEventsSubscriber> {


    private var loadingAnimationRenderer = LoadingAnimationHandler()
    private val menuScreenViewComponentsHandler = MenuScreenViewComponentsHandler(assetsManager)

    override val subscribers = HashSet<MenuScreenViewEventsSubscriber>()

    fun onShow() {
        menuScreenViewComponentsHandler.init(assetsManager)
        val brickTexture = assetsManager.getTexture(TexturesDefinitions.BRICK)
        val font = assetsManager.getFont(FontsDefinitions.VARELA_80)
        val style = Label.LabelStyle(font, Color.WHITE)
        loadingAnimationRenderer.addLoadingAnimation(
            brickTexture,
            style,
            menuScreenViewComponentsHandler.stage
        )
    }

    fun render(delta: Float) {
        menuScreenViewComponentsHandler.render(delta)
        loadingAnimationRenderer.render(subscribers)
    }

    override fun dispose() {
        menuScreenViewComponentsHandler.dispose()
    }

    fun onHide() {
        menuScreenViewComponentsHandler.onHide()
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

}