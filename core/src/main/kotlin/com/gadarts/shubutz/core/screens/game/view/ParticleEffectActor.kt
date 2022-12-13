package com.gadarts.shubutz.core.screens.game.view

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.ParticleEffect
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Group

class ParticleEffectActor(private val particleEffect: ParticleEffect) : Actor() {

    var started = false

    override fun act(delta: Float) {
        super.act(delta)
        if (started) {
            particleEffect.update(delta)
        }
        if (particleEffect.isComplete) {
            remove()
        }
    }

    override fun setParent(parent: Group?) {
        super.setParent(parent)
        particleEffect.setPosition(x, y)
    }

    override fun draw(batch: Batch?, parentAlpha: Float) {
        super.draw(batch, parentAlpha)
        if (started) {
            particleEffect.draw(batch)
        }
    }

    fun start() {
        started = true
        particleEffect.start()
        particleEffect.setPosition(x, y)
    }

    fun stop() {
        particleEffect.emitters.forEach { it.isContinuous = false }
    }
}
