package com.gadarts.wordsbomb.core.model.assets

import com.badlogic.gdx.assets.AssetLoaderParameters

interface AssetDefinition<T> {
    fun getPath(): String
    fun getDefinitionName(): String
    fun getClazz(): Class<T>
    fun getParameters(): AssetLoaderParameters<T>?
}
