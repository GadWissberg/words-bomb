package com.gadarts.shubutz.core.screens.game.view

import com.badlogic.gdx.scenes.scene2d.Action

class WordRevealedAction(private val runnable: Runnable) : Action() {
    override fun act(delta: Float): Boolean {
        runnable.run()
        return true
    }

}
