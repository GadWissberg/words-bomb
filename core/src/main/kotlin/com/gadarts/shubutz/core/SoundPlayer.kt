package com.gadarts.shubutz.core

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound

class SoundPlayer {
    private var currentMusic: Music? = null

    fun playMusic(music: Music) {
        currentMusic?.stop()
        music.isLooping = true
        music.volume = MUSIC_VOLUME
        music.play()
        currentMusic = music
    }

    fun playSound(sound: Sound) {
        sound.play()
    }

    companion object {
        private const val MUSIC_VOLUME = 0.5F
    }
}
