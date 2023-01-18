package com.gadarts.shubutz.core

import com.badlogic.gdx.audio.Sound

class SoundPlayer {

    fun playSound(sound: Sound) {
        if (!DebugSettings.ENABLE_SOUNDS) return
        sound.play()
    }
}
