package com.gadarts.shubutz.core.screens.game.view

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable

class ScoreView(texture: Texture, font: BitmapFont, score: Int) : Table() {

    private val label = Label(score.toString(), Label.LabelStyle(font, Color.WHITE))

    init {
        background = TextureRegionDrawable(texture)
        add(label).pad(LABEL_PADDING_TOP, 0F, 0F, 0F)
        pack()
    }

    fun updateLabel(score: Int) {
        label.setText(score)
    }

    companion object {
        private const val LABEL_PADDING_TOP = 70F
    }
}
