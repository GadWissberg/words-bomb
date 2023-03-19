package com.gadarts.shubutz.core.screens.game.view

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.gadarts.shubutz.core.model.GameModel
import com.gadarts.shubutz.core.DebugSettings

class BombComponent(
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

    private fun applyLevelTwoWarning() {
        val newColor = Color.valueOf(color.toString())
        newColor.g -= WARNING_COLOR_DELTA
        newColor.b -= WARNING_COLOR_DELTA

        actions.clear()
        addAction(
            Actions.parallel(
                Actions.color(newColor, WARNING_COLOR_DURATION, Interpolation.smoother),
                Actions.forever(
                    Actions.parallel(
                        Actions.sequence(
                            Actions.moveBy(5F, 0F, 0.05F),
                            Actions.moveBy(-5F, 0F, 0.05F),
                        ),
                        Actions.sequence(
                            Actions.moveBy(0F, 5F, 0.05F),
                            Actions.moveBy(0F, -5F, 0.05F),
                        )
                    )
                )
            )
        )
    }

    fun onIncorrectGuess(gameModel: GameModel) {
        if (gameModel.triesLeft == 2) {
            applyLevelOneWarning()
        } else if (gameModel.triesLeft == 1) {
            applyLevelTwoWarning()
        }
    }

    private fun applyLevelOneWarning() {
        val newColor = Color.valueOf(color.toString())
        newColor.g -= WARNING_COLOR_DELTA
        newColor.b -= WARNING_COLOR_DELTA

        addAction(
            Actions.parallel(
                Actions.color(newColor, WARNING_COLOR_DURATION, Interpolation.smoother),
                Actions.forever(
                    Actions.parallel(
                        Actions.sequence(
                            Actions.delay(MathUtils.random(0.5F, 4F)),
                            Actions.moveBy(WARNING_MOVE_DISTANCE, 0F, 0.1F),
                            Actions.moveBy(-WARNING_MOVE_DISTANCE, 0F, 0.1F),
                        ),
                        Actions.sequence(
                            Actions.delay(MathUtils.random(0.5F, 4F)),
                            Actions.moveBy(0F, WARNING_MOVE_DISTANCE, 0.1F),
                            Actions.moveBy(0F, -WARNING_MOVE_DISTANCE, 0.1F),
                        )

                    )
                )
            )
        )
    }

    companion object {
        const val FIRE_RELATIVE_X = 45
        const val FIRE_RELATIVE_Y = 620
        const val NUMBER_PADDING_TOP = 300F
        const val WARNING_COLOR_DELTA = 15F
        const val WARNING_COLOR_DURATION = 16F
        const val WARNING_MOVE_DISTANCE = 5F
    }
}
