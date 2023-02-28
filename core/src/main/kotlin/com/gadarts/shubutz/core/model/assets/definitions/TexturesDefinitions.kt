package com.gadarts.shubutz.core.model.assets.definitions

import com.badlogic.gdx.assets.AssetLoaderParameters
import com.badlogic.gdx.graphics.Texture
import java.util.*

enum class TexturesDefinitions(ninepatch: Boolean = false) : AssetDefinition<Texture> {
    HUD(true),
    BACK_BUTTON,
    BUTTON_UP,
    BUTTON_DOWN,
    CELL,
    BRICK,
    LIST(true),
    DIALOG(true),
    POPUP_BUTTON_UP(true),
    POPUP_BUTTON_DOWN(true),
    CLOUD_1,
    CLOUD_2,
    CLOUD_3,
    CLOUD_4,
    LOGO_SHIN,
    LOGO_VAV1,
    LOGO_BET,
    LOGO_VAV2,
    LOGO_LAST,
    BOMB,
    COINS_ICON,
    COINS_BUTTON_UP,
    COINS_BUTTON_DOWN,
    ICON_PACK_1,
    ICON_PACK_2,
    ICON_PACK_3,
    DIALOG_CLOSE_BUTTON,
    COIN,
    FLASH,
    ICON_EYE,
    BUTTON_CIRCLE_UP,
    BUTTON_CIRCLE_DOWN;

    private var path: String =
        "textures/${(if (ninepatch) "$name.9" else name).lowercase(Locale.ROOT)}.png"

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