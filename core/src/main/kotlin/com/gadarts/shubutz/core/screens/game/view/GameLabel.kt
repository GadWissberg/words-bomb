package com.gadarts.shubutz.core.screens.game.view

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.gadarts.shubutz.core.AndroidInterface

class GameLabel(
    text: CharSequence,
    labelStyle: LabelStyle,
    private val androidInterface: AndroidInterface
) : Label(text, labelStyle) {
    override fun draw(batch: Batch?, parentAlpha: Float) {
        try {
            super.draw(batch, parentAlpha)
        } catch (e: Exception) {
        }
    }
}
