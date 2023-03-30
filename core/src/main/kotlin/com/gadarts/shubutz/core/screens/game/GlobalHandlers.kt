package com.gadarts.shubutz.core.screens.game

import com.badlogic.gdx.utils.Disposable
import com.gadarts.shubutz.core.AndroidInterface
import com.gadarts.shubutz.core.SoundPlayer
import com.gadarts.shubutz.core.model.assets.GameAssetManager

class GlobalHandlers(val androidInterface: AndroidInterface) : Disposable {
    val assetsManager: GameAssetManager = GameAssetManager()
    val soundPlayer: SoundPlayer = SoundPlayer()
    override fun dispose() {
        assetsManager.dispose()
    }

}
