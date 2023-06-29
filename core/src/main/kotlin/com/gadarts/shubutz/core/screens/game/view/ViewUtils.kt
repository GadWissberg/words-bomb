package com.gadarts.shubutz.core.screens.game.view

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.gadarts.shubutz.core.AndroidInterface
import com.gadarts.shubutz.core.model.assets.GameAssetManager
import com.gadarts.shubutz.core.model.assets.definitions.FontsDefinitions

object ViewUtils {
    fun createDialogLabel(
        text: String,
        assetsManager: GameAssetManager,
        androidInterface: AndroidInterface
    ): Label {
        return GameLabel(
            text.reversed(),
            Label.LabelStyle(assetsManager.getFont(FontsDefinitions.VARELA_40), Color.WHITE),
            androidInterface
        )
    }

}
