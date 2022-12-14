package com.gadarts.shubutz.core.model.assets

import com.badlogic.gdx.assets.AssetLoaderParameters
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader
import com.gadarts.shubutz.core.model.GameModel.Companion.LETTERS
import java.util.*

enum class FontsDefinitions : AssetDefinition<BitmapFont> {
    VARELA_320 {
        override fun getParameters(): AssetLoaderParameters<BitmapFont> {
            return createFontParameters(160, 16f)
        }

    },
    VARELA_80 {
        override fun getParameters(): AssetLoaderParameters<BitmapFont> {
            return createFontParameters(80, 8f)
        }

    },
    VARELA_40 {
        override fun getParameters(): AssetLoaderParameters<BitmapFont> {
            return createFontParameters(40, 4f)
        }
    },
    VARELA_35 {
        override fun getParameters(): AssetLoaderParameters<BitmapFont> {
            return createFontParameters(35, 3.5f)
        }
    },
    VARELA_20 {
        override fun getParameters(): AssetLoaderParameters<BitmapFont> {
            return createFontParameters(20, 2f)
        }
    };

    private var path: String = "${name.toLowerCase(Locale.ROOT)}.ttf"

    override fun getPath(): String {
        return path
    }

    protected fun createFontParameters(
        size: Int,
        borderWidth: Float
    ): AssetLoaderParameters<BitmapFont> {
        val params = FreetypeFontLoader.FreeTypeFontLoaderParameter()
        params.fontFileName = "varela.ttf"
        params.fontParameters.size = size
        params.fontParameters.color = Color.WHITE
        params.fontParameters.borderColor = Color(0f, 0F, 0f, 0.5f)
        params.fontParameters.borderWidth = borderWidth
        params.fontParameters.shadowColor = Color(0f, 0F, 0f, 0.5f)
        params.fontParameters.shadowOffsetX = -4
        params.fontParameters.shadowOffsetY = 4
        params.fontParameters.borderStraight = true
        params.fontParameters.kerning = true
        params.fontParameters.characters += LETTERS
        return params
    }

    override fun getClazz(): Class<BitmapFont> {
        return BitmapFont::class.java
    }

    override fun getDefinitionName(): String {
        return name
    }
}