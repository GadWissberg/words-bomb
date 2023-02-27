package com.gadarts.shubutz.core.screens.game.view.actors

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.math.Vector2
import ktx.actors.alpha

/**
 * The stage actor that represents a missing letter.
 */
open class BrickCell(
    texture: Texture,
    letterSize: Vector2,
    font80: BitmapFont
) : Brick("_", texture, letterSize, font80) {
    init {
        alpha = 0F
    }

}