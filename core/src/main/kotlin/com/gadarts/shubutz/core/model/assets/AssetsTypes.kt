package com.gadarts.shubutz.core.model.assets

import com.gadarts.shubutz.core.model.assets.definitions.*

enum class AssetsTypes(
    val assets: Array<out AssetDefinition<*>>,
    private val loadedUsingLoader: Boolean
) {
    TEXTURES(TexturesDefinitions.values(), true),
    SFX(SoundsDefinitions.values(), true),
    SHADERS(ShaderDefinitions.values(), false),
    FONTS(FontsDefinitions.values(), true),
    PARTICLES(ParticleEffectsDefinitions.values(), true),
    ATLASES(AtlasesDefinitions.values(), true);

    fun isLoadedUsingLoader(): Boolean {
        return loadedUsingLoader
    }

}