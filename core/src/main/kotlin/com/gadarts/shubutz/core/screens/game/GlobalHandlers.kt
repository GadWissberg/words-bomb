package com.gadarts.shubutz.core.screens.game

import com.badlogic.gdx.utils.Disposable
import com.gadarts.shubutz.core.AndroidInterface
import com.gadarts.shubutz.core.SoundPlayer
import com.gadarts.shubutz.core.model.assets.GameAssetManager
import com.gadarts.shubutz.core.screens.game.view.DialogsHandler
import com.gadarts.shubutz.core.screens.game.view.EffectsHandler
import com.gadarts.shubutz.core.screens.menu.view.stage.GameStage

class GlobalHandlers(
    val androidInterface: AndroidInterface,
    stage: GameStage,
    val assetsManager: GameAssetManager
) : Disposable {
    private val effectsHandler: EffectsHandler = EffectsHandler()
    val dialogsHandler: DialogsHandler
    val soundPlayer: SoundPlayer = SoundPlayer(androidInterface)

    init {
        dialogsHandler = DialogsHandler(
            assetsManager,
            effectsHandler,
            stage,
            soundPlayer
        )
    }

    override fun dispose() {
        assetsManager.dispose()
    }

}
