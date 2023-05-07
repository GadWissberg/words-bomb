package com.gadarts.shubutz.core.screens.game.view

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable

class ScoreView(texture: Texture, font: BitmapFont) : Table() {

    private val label = Label(0.toString(), Label.LabelStyle(font, Color.WHITE))

    init {
        background = TextureRegionDrawable(texture)
        add(label).pad(LABEL_PADDING_TOP, 0F, 0F, 0F)
        pack()
    }

    private fun updateLabel(score: Long) {
        label.setText(score.toString())
    }

    fun onGameWin(score: Long) {
        updateLabel(score)

        addAction(
            Actions.sequence(
                Actions.sizeTo(width * WIN_SIZE_FACTOR, height * WIN_SIZE_FACTOR),
                Actions.sizeTo(width, height, 0.5F, Interpolation.smoother),
            )
        )
    }

    companion object {
        private const val LABEL_PADDING_TOP = 70F
        private const val WIN_SIZE_FACTOR = 1.05F
    }
}
