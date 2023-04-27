package com.gadarts.shubutz.core

import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.math.MathUtils
import com.gadarts.shubutz.core.model.assets.SharedPreferencesKeys

class SoundPlayer(android: AndroidInterface) {
    var enabled: Boolean = false

    init {
        enabled =
            android.getSharedPreferencesBooleanValue(SharedPreferencesKeys.SOUND_ENABLED, true)
    }


    fun playSound(sound: Sound, randomPitch: Boolean = false) {
        if (!DebugSettings.ENABLE_SOUNDS || !enabled) return
        sound.play(VOLUME, if (randomPitch) MathUtils.random(0.8F, 1.2F) else 1F, 0F)
    }

    companion object {
        const val VOLUME = 0.75F
    }
}
