package com.gadarts.shubutz.core.screens.game.view

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor

class FlashEffect(texture: Texture) : Actor() {
    private var rotate = 0F
    private var region: TextureRegion

    init {
        region = TextureRegion(texture)
    }

    override fun act(delta: Float) {
        super.act(delta)
        rotate += 0.5F
    }

    override fun draw(batch: Batch?, parentAlpha: Float) {
        super.draw(batch, parentAlpha)
        batch?.draw(
            region,
            x - region.regionWidth / 4F, y - region.regionHeight / 4F,
            region.regionWidth / 2F, region.regionHeight / 2F,
            region.regionWidth.toFloat(), region.regionHeight.toFloat(),
            1F, 1F,
            rotate
        )
    }
}
