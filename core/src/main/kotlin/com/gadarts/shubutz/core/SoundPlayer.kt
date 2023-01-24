package com.gadarts.shubutz.core

import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.math.MathUtils

class SoundPlayer {

    fun playSound(sound: Sound, randomPitch: Boolean = false) {
        if (!DebugSettings.ENABLE_SOUNDS) return
        sound.play(1F, if (randomPitch) MathUtils.random(0.8F, 1.2F) else 1F, 0F)
    }
}
