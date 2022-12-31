package com.gadarts.shubutz.core.model.assets

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.FileHandleResolver
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.ParticleEffect
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader

/**
 * Responsible to load the assets.
 */
open class GameAssetManager : AssetManager() {
    lateinit var words: HashMap<String, ArrayList<String>>
    private val wordsLoader = WordsLoader()

    /**
     * Loads all assets sync.
     */
    fun loadAssets() {
        words = wordsLoader.load()
        initializeFontLoaders()
        AssetsTypes.values().forEach { type ->
            if (type.isLoadedUsingLoader()) {
                type.assets.forEach { asset ->
                    if (asset.getParameters() != null) {
                        load(
                            asset.getPath(),
                            BitmapFont::class.java,
                            (asset.getParameters() as FreetypeFontLoader.FreeTypeFontLoaderParameter)
                        )
                    } else {
                        load(asset.getPath(), asset.getClazz())
                    }
                }
            } else {
                type.assets.forEach { asset ->
                    val path = asset.getPath()
                    addAsset(path, String::class.java, Gdx.files.internal(path).readString())
                }
            }
        }
    }

    private fun initializeFontLoaders() {
        val resolver: FileHandleResolver = InternalFileHandleResolver()
        setLoader(FreeTypeFontGenerator::class.java, FreeTypeFontGeneratorLoader(resolver))
        val loader = FreetypeFontLoader(resolver)
        setLoader(BitmapFont::class.java, "ttf", loader)
    }

    fun getShader(shader: ShaderDefinitions): String? {
        return get(shader.getPath(), String::class.java)
    }

    fun getTexture(definition: TexturesDefinitions): Texture {
        return get(definition.getPath(), Texture::class.java)
    }

    fun getParticleEffect(definition: ParticleEffectsDefinitions): ParticleEffect {
        return get(definition.getPath(), ParticleEffect::class.java)
    }

    fun getFont(font: FontsDefinitions): BitmapFont {
        return get(font.getPath(), BitmapFont::class.java)
    }

    fun getMusic(music: MusicDefinitions): Music {
        return get(music.getPath(), Music::class.java)
    }

    fun getSound(sound: SoundsDefinitions): Sound {
        return get(sound.getPath(), Sound::class.java)
    }

}