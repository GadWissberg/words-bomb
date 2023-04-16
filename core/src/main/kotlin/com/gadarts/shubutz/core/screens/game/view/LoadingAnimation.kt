package com.gadarts.shubutz.core.screens.game.view

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Scaling

class LoadingAnimation(keyFrames: Array<TextureAtlas.AtlasRegion>) : Image() {
    private val drawables: List<TextureRegionDrawable>
    private var stateTime: Float = 0.0f
    private val animation: Animation<TextureAtlas.AtlasRegion>

    init {
        animation = Animation(FRAME_DURATION, keyFrames, Animation.PlayMode.LOOP)
        drawables = keyFrames.map { TextureRegionDrawable(TextureRegion(it)) }
        setScaling(Scaling.none)
    }

    override fun act(delta: Float) {
        super.act(delta)
        val newFrame = drawables[animation.getKeyFrame(stateTime).index]
        drawable = newFrame
        stateTime += delta
    }

    companion object {
        const val FRAME_DURATION = 0.1F
    }
}
