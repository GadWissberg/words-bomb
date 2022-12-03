package com.gadarts.wordsbomb.core.view.hud

import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.gadarts.wordsbomb.core.model.assets.FontsDefinitions
import com.gadarts.wordsbomb.core.model.assets.GameAssetManager

class HudStageFontData {

    lateinit var font40: BitmapFont
    lateinit var font80: BitmapFont
    lateinit var glyphLayout: GlyphLayout
    fun init(assetsManager: GameAssetManager) {
        font80 = assetsManager.getFont(FontsDefinitions.VARELA_80)
        font40 = assetsManager.getFont(FontsDefinitions.VARELA_40)
        glyphLayout = GlyphLayout(font80, "א")
    }

}
