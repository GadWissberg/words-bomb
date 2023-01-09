package com.gadarts.shubutz.core.model.assets

import com.badlogic.gdx.assets.AssetLoaderParameters
import com.badlogic.gdx.audio.Music

enum class MusicDefinitions : AssetDefinition<Music> {
    MENU,
    IN_GAME;

    private var path: String =
        "music/${name.lowercase()}.ogg"

    override fun getPath(): String {
        return path
    }

    override fun getParameters(): AssetLoaderParameters<Music>? {
        return null
    }

    override fun getClazz(): Class<Music> {
        return Music::class.java
    }

    override fun getDefinitionName(): String {
        return name
    }
}