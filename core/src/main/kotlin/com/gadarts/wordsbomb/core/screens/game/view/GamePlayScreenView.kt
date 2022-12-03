package com.gadarts.wordsbomb.core.screens.game.view

import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Disposable
import com.gadarts.wordsbomb.core.Notifier
import com.gadarts.wordsbomb.core.model.GameModel
import com.gadarts.wordsbomb.core.model.assets.FontsDefinitions
import com.gadarts.wordsbomb.core.model.assets.GameAssetManager
import com.gadarts.wordsbomb.core.model.assets.TexturesDefinitions
import com.gadarts.wordsbomb.core.screens.menu.view.stage.GameStage
import com.gadarts.wordsbomb.core.view.Brick

class GamePlayScreenView(private val assetsManager: GameAssetManager) : Disposable,
    Notifier<GamePlayScreenViewEventsSubscriber> {


    private lateinit var glyphLayout: GlyphLayout
    private var uiTable: Table? = null
    lateinit var stage: GameStage
    override val subscribers = HashSet<GamePlayScreenViewEventsSubscriber>()
    var font80: BitmapFont = assetsManager.getFont(FontsDefinitions.VARELA_80)

    fun onShow(gameModel: GameModel) {
        glyphLayout = GlyphLayout(font80, "א")
        gameModel.options.forEach {
            Brick(
                it.toString(),
                assetsManager.getTexture(TexturesDefinitions.BRICK),
                Vector2(glyphLayout.width, glyphLayout.height),
                font80,
            )
        }
    }

    fun render() {
    }

    override fun dispose() {

    }

    fun onHide() {
    }

}