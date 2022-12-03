package com.gadarts.wordsbomb.core.model.assets

import com.badlogic.gdx.assets.AssetLoaderParameters
import java.util.*

enum class ShaderDefinitions : AssetDefinition<String> {
    VERTEX,
    FRAGMENT,
    BLUR_FRAGMENT;

    private var path: String = "shaders/${name.toLowerCase(Locale.ROOT)}.shader"

    override fun getPath(): String {
        return path
    }

    override fun getParameters(): AssetLoaderParameters<String>? {
        return null
    }

    override fun getClazz(): Class<String> {
        return String::class.java
    }

    override fun getDefinitionName(): String {
        return name
    }
}