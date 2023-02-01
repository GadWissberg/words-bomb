package com.gadarts.shubutz.core.model.assets.definitions

import com.badlogic.gdx.assets.AssetLoaderParameters
import com.badlogic.gdx.graphics.g2d.TextureAtlas

enum class AtlasesDefinitions : AssetDefinition<TextureAtlas> {
    LOADING;

    private var path: String = "atlases/${name.lowercase()}.txt"

    override fun getPath(): String {
        return path
    }

    override fun getParameters(): AssetLoaderParameters<TextureAtlas>? {
        return null
    }

    override fun getClazz(): Class<TextureAtlas> {
        return TextureAtlas::class.java
    }

    override fun getDefinitionName(): String {
        return name
    }
}