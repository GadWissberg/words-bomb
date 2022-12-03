package com.gadarts.wordsbomb.core.view

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.gadarts.wordsbomb.core.view.board.BoardCell

/**
 * Represents a letter brick that can be placed in the deck and in the board.
 */
open class Brick(
    val letter: String,
    brickTexture: Texture,
    private val letterSize: Vector2,
    private val font80: BitmapFont,

    /**
     * Whether all intro animations had finished.
     */
    var ready: Boolean = false
) :
    Image(brickTexture) {

    /**
     * Whether this brick is a draft.
     */
    var draft: Boolean = false

    /**
     * The cell it is in (if it is at all)
     */
    var onCell: BoardCell? = null

    override fun draw(batch: Batch?, parentAlpha: Float) {
        super.draw(batch, parentAlpha)
        drawLetter(batch, x, y, 1F, font80)
    }


    /**
     * Draws a letter inside the brick.
     */
    fun drawLetter(batch: Batch?, x: Float, y: Float, scale: Float, bitmapFont: BitmapFont) {
        drawChar(
            batch,
            auxVector.set(
                x + width * scale / 2F - letterSize.x * scale / 2F,
                y + height * scale / 2F + letterSize.y * scale / 2F + LETTER_HEIGHT_BIAS
            ),
            scale,
            bitmapFont,
            letter
        )
    }

    private fun drawChar(
        batch: Batch?, position: Vector2, scale: Float, bitmapFont: BitmapFont, text: String
    ) {
        if (batch != null && ready) {
            bitmapFont.color = if (!draft) LETTER_COLOR_REGULAR else LETTER_COLOR_DRAFT
            bitmapFont.data.setScale(scale)
            bitmapFont.draw(batch, text, position.x, position.y)
            bitmapFont.data.scale(1F)
        }
    }

    class DisplayLetterAction : Action() {

        override fun act(delta: Float): Boolean {
            (actor as Brick).ready = true
            actor.removeAction(this)
            return true
        }

    }

    companion object {
        private const val LETTER_HEIGHT_BIAS = 15F
        private val LETTER_COLOR_REGULAR = Color.WHITE
        private val LETTER_COLOR_DRAFT = Color.GREEN
        private val auxVector = Vector2()
    }
}
