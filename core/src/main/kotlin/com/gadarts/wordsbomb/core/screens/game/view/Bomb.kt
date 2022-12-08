package com.gadarts.wordsbomb.core.screens.game.view

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.gadarts.wordsbomb.core.DebugSettings

class Bomb(
    texture: Texture,
    private val particleEffectActor: FireParticleEffectActor,
    font: BitmapFont,
    triesLeft: Int
) :
    Table() {

    private var label: Label

    init {
        background = TextureRegionDrawable(texture)
        val labelStyle = LabelStyle(font, Color.WHITE)
        label = Label("$triesLeft", labelStyle)
        add(label).padTop(NUMBER_PADDING_TOP)
        debug = DebugSettings.SHOW_UI_BORDERS
    }

    fun startFire() {
        particleEffectActor.setPosition(x + FIRE_RELATIVE_X, y + FIRE_RELATIVE_Y)
        particleEffectActor.start()
    }

    fun updateLabel(triesLeft: Int) {
        label.setText(triesLeft.toString())
    }

    companion object {
        const val FIRE_RELATIVE_X = 45
        const val FIRE_RELATIVE_Y = 620
        const val NUMBER_PADDING_TOP = 300F
    }
}
