package com.gadarts.shubutz.core.screens.menu

import com.badlogic.gdx.Screen
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.gadarts.shubutz.core.AndroidInterface
import com.gadarts.shubutz.core.Notifier
import com.gadarts.shubutz.core.model.assets.GameAssetManager
import com.gadarts.shubutz.core.screens.menu.view.MenuScreenView
import com.gadarts.shubutz.core.screens.menu.view.MenuScreenViewEventsSubscriber

class MenuScreen(
    assetsManager: GameAssetManager,
    private val androidInterface: AndroidInterface,
) :
    Screen, Notifier<MenuScreenEventsSubscriber>, MenuScreenViewEventsSubscriber {


    private val menuScreenView = MenuScreenView(assetsManager)
    override val subscribers = HashSet<MenuScreenEventsSubscriber>()

    override fun show() {
        menuScreenView.subscribeForEvents(this)
        menuScreenView.onShow()
    }

    override fun render(delta: Float) {
        menuScreenView.render(delta)
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

    override fun subscribeForEvents(subscriber: MenuScreenEventsSubscriber) {
        subscribers.add(subscriber)
    }

    override fun onLoadingAnimationReady() {
        menuScreenView.onLoadingAnimationReady(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                subscribers.forEach { it.onBeginGame() }
            }
        })
    }

    companion object {
        const val HOST_IP = "192.168.1.136"
        const val SOCKET_TIMEOUT_SECONDS = 10
    }

}