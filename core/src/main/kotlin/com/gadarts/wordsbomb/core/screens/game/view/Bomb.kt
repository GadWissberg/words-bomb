package com.gadarts.wordsbomb.core.screens.game.view

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.ui.Image

class Bomb(texture: Texture, private val particleEffectActor: FireParticleEffectActor) :
    Image(texture) {

    fun startFire() {
        particleEffectActor.setPosition(x + 45, y + 620)
        particleEffectActor.start()
    }
}
