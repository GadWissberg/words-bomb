package com.gadarts.wordsbomb.core.view.board

import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.gadarts.wordsbomb.core.model.assets.FontsDefinitions
import com.gadarts.wordsbomb.core.model.assets.GameAssetManager

class BoardStageFontData(assetsManager: GameAssetManager) {
    var font80: BitmapFont = assetsManager.getFont(FontsDefinitions.VARELA_80)
    var font40: BitmapFont = assetsManager.getFont(FontsDefinitions.VARELA_40)

}
