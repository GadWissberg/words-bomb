package com.gadarts.shubutz.core.screens.game.view

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.gadarts.shubutz.core.DebugSettings

class Bomb(
    texture: Texture,
    private val particleEffectActor: ParticleEffectActor,
    font: BitmapFont,
    triesLeft: Int
) :
    Table() {
    private var label: Label

    init {
        setOrigin(texture.width / 2F, texture.height / 2F)
        background = TextureRegionDrawable(texture)
        val labelStyle = LabelStyle(font, Color.WHITE)
        label = Label("$triesLeft", labelStyle)
        add(label).padTop(NUMBER_PADDING_TOP)
        debug = DebugSettings.SHOW_UI_BORDERS
    }

    fun startFire() {
        particleEffectActor.start()
    }

    override fun positionChanged() {
        super.positionChanged()
        particleEffectActor.setPosition(x + FIRE_RELATIVE_X, y + FIRE_RELATIVE_Y)
    }

    fun updateLabel(triesLeft: Int) {
        label.addAction(
            Actions.sequence(
                Actions.fadeOut(0.5F, Interpolation.swingIn),
                Actions.run { label.setText(triesLeft.toString()) },
                Actions.fadeIn(0.5F, Interpolation.smoother)
            )
        )
    }

    fun hideLabel() {
        label.isVisible = false
    }

    companion object {
        const val FIRE_RELATIVE_X = 45
        const val FIRE_RELATIVE_Y = 620
        const val NUMBER_PADDING_TOP = 300F
    }
}
