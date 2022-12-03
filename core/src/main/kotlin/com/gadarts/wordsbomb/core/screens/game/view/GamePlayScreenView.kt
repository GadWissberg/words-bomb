package com.gadarts.wordsbomb.core.screens.game.view

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.viewport.FitViewport
import com.gadarts.wordsbomb.core.DebugSettings
import com.gadarts.wordsbomb.core.Notifier
import com.gadarts.wordsbomb.core.model.GameModel
import com.gadarts.wordsbomb.core.model.GameModel.Companion.MAX_OPTIONS
import com.gadarts.wordsbomb.core.model.assets.FontsDefinitions
import com.gadarts.wordsbomb.core.model.assets.GameAssetManager
import com.gadarts.wordsbomb.core.model.assets.TexturesDefinitions
import com.gadarts.wordsbomb.core.model.view.Brick
import com.gadarts.wordsbomb.core.screens.menu.view.stage.GameStage

class GamePlayScreenView(private val assetsManager: GameAssetManager) : Disposable,
    Notifier<GamePlayScreenViewEventsSubscriber> {


    private lateinit var lettersOptionsTable: Table
    private lateinit var glyphLayout: GlyphLayout
    private lateinit var uiTable: Table
    lateinit var stage: GameStage
    override val subscribers = HashSet<GamePlayScreenViewEventsSubscriber>()
    private var font80: BitmapFont = assetsManager.getFont(FontsDefinitions.VARELA_80)

    fun onShow(gameModel: GameModel) {
        createStage()
        addUiTable()
        lettersOptionsTable = Table()
        lettersOptionsTable.setSize(uiTable.width, uiTable.prefHeight)
        uiTable.add(lettersOptionsTable)
        glyphLayout = GlyphLayout(font80, "א")
        gameModel.options.forEach {
            lettersOptionsTable.add(
                Brick(
                    it.toString(),
                    assetsManager.getTexture(TexturesDefinitions.BRICK),
                    Vector2(glyphLayout.width, glyphLayout.height),
                    font80,
                )
            )
            if (lettersOptionsTable.children.size % MAX_OPTIONS_IN_ROW == 0) {
                lettersOptionsTable.row()
            }
        }
    }

    private fun addUiTable() {
        uiTable = Table()
        uiTable.debug = DebugSettings.SHOW_UI_BORDERS
        stage.addActor(uiTable)
        uiTable.setFillParent(true)
    }

    private fun createStage() {
        stage = GameStage(
            FitViewport(Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat()),
            assetsManager
        )
        stage.setDebugInvisible(DebugSettings.SHOW_UI_BORDERS)
    }

    fun render(delta: Float) {
        stage.act(delta)
        stage.draw()
    }

    override fun dispose() {
        stage.dispose()
    }

    fun onHide() {
    }

    companion object {
        private const val MAX_OPTIONS_IN_ROW = MAX_OPTIONS / 3
    }
}