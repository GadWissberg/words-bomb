package com.gadarts.shubutz.core.model.assets

import com.badlogic.gdx.assets.AssetLoaderParameters
import com.badlogic.gdx.graphics.Texture
import java.util.Locale.ROOT

enum class TexturesDefinitions(ninepatch: Boolean) : AssetDefinition<Texture> {
    HUD(true),
    BUTTON_UP(false),
    BUTTON_DOWN(false),
    CELL(false),
    BRICK(false),
    LIST(true),
    POPUP(true),
    POPUP_BUTTON_UP(true),
    POPUP_BUTTON_DOWN(true),
    CLOUD_1(false),
    CLOUD_2(false),
    CLOUD_3(false),
    CLOUD_4(false),
    GO_BUTTON_UP(false),
    GO_BUTTON_DOWN(false),
    GO_BUTTON_DISABLED(false),
    LOGO_SHIN(false),
    LOGO_VAV1(false),
    LOGO_BET(false),
    LOGO_VAV2(false),
    LOGO_LAST(false),
    BOMB(false);

    private var path: String =
        "textures/${(if (ninepatch) "$name.9" else name).toLowerCase(ROOT)}.png"

    override fun getPath(): String {
        return path
    }

    override fun getParameters(): AssetLoaderParameters<Texture>? {
        return null
    }

    override fun getClazz(): Class<Texture> {
        return Texture::class.java
    }

    override fun getDefinitionName(): String {
        return name
    }
}