package com.gadarts.shubutz.core

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound

class SoundPlayer {
    private var currentMusic: Music? = null

    fun playMusic(music: Music) {
        currentMusic?.stop()
        music.isLooping = true
        music.play()
        currentMusic = music
    }

    fun playSound(sound: Sound) {
        sound.play()
    }

}
