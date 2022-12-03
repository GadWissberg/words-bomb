package com.gadarts.wordsbomb.core.model.view

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.ui.Image

/**
 * Represents a letter brick that can be placed in the deck and in the board.
 */
open class Brick(
    private val letter: String,
    brickTexture: Texture,
    private val letterSize: Vector2,
    private val font80: BitmapFont,
) :
    Image(brickTexture) {

    override fun draw(batch: Batch?, parentAlpha: Float) {
        super.draw(batch, parentAlpha)
        drawLetter(batch, x, y, font80)
    }


    /**
     * Draws a letter inside the brick.
     */
    private fun drawLetter(batch: Batch?, x: Float, y: Float, bitmapFont: BitmapFont) {
        drawChar(
            batch,
            auxVector.set(
                x + letterSize.x / 2F,
                y + height / 2F + letterSize.y / 2F
            ),
            bitmapFont,
            letter
        )
    }

    private fun drawChar(
        batch: Batch?, position: Vector2, bitmapFont: BitmapFont, text: String
    ) {
        if (batch != null) {
            bitmapFont.color = LETTER_COLOR_REGULAR
            bitmapFont.draw(batch, text, position.x, position.y)
        }
    }

    companion object {
        private val LETTER_COLOR_REGULAR = Color.WHITE
        private val auxVector = Vector2()
    }
}