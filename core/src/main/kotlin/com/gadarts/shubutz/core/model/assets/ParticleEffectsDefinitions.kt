package com.gadarts.shubutz.core.model.assets

import com.badlogic.gdx.assets.AssetLoaderParameters
import com.badlogic.gdx.graphics.g2d.ParticleEffect

enum class ParticleEffectsDefinitions : AssetDefinition<ParticleEffect> {
    FIRE;

    private var path: String =
        "particles/${name.lowercase()}.pe"

    override fun getPath(): String {
        return path
    }

    override fun getParameters(): AssetLoaderParameters<ParticleEffect>? {
        return null
    }

    override fun getClazz(): Class<ParticleEffect> {
        return ParticleEffect::class.java
    }

    override fun getDefinitionName(): String {
        return name
    }
}