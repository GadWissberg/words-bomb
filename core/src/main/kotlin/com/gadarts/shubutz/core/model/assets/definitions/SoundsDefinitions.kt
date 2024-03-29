package com.gadarts.shubutz.core.model.assets.definitions

import com.badlogic.gdx.assets.AssetLoaderParameters
import com.badlogic.gdx.audio.Sound

enum class SoundsDefinitions : AssetDefinition<Sound> {
    INCORRECT,
    BRICK_JUMP,
    WIN,
    IGNITE,
    EXPLOSION,
    BUTTON,
    FLYBY,
    BUBBLE,
    CORRECT,
    PURCHASED,
    PERFECT,
    CHAMPION,
    HELP;

    private var path: String =
        "sfx/${name.lowercase()}.wav"

    override fun getPath(): String {
        return path
    }

    override fun getParameters(): AssetLoaderParameters<Sound>? {
        return null
    }

    override fun getClazz(): Class<Sound> {
        return Sound::class.java
    }

    override fun getDefinitionName(): String {
        return name
    }
}