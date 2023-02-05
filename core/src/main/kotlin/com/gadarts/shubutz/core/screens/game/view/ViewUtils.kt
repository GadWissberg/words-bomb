package com.gadarts.shubutz.core.screens.game.view

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.gadarts.shubutz.core.model.assets.GameAssetManager
import com.gadarts.shubutz.core.model.assets.definitions.FontsDefinitions

object ViewUtils {
    fun createDialogLabel(text: String, assetsManager: GameAssetManager): Label {
        return Label(
            text.reversed(),
            Label.LabelStyle(assetsManager.getFont(FontsDefinitions.VARELA_40), Color.WHITE)
        )
    }

}
