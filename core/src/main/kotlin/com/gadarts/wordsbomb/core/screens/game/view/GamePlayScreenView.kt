package com.gadarts.wordsbomb.core.screens.game.view

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
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
import com.gadarts.wordsbomb.core.model.view.BrickCell
import com.gadarts.wordsbomb.core.model.view.Brick
import com.gadarts.wordsbomb.core.screens.menu.view.stage.GameStage

private const val fl = 40F

class GamePlayScreenView(private val assetsManager: GameAssetManager) : Disposable,
    Notifier<GamePlayScreenViewEventsSubscriber> {


    private lateinit var targetWordTable: Table
    private lateinit var lettersOptionsTable: Table
    private lateinit var letterGlyphLayout: GlyphLayout
    private lateinit var uiTable: Table
    private lateinit var stage: GameStage
    override val subscribers = HashSet<GamePlayScreenViewEventsSubscriber>()
    private var font80: BitmapFont = assetsManager.getFont(FontsDefinitions.VARELA_80)
    private lateinit var letterSize: Vector2

    fun onShow(gameModel: GameModel) {
        letterGlyphLayout = GlyphLayout(font80, "א")
        letterSize = Vector2(letterGlyphLayout.width, letterGlyphLayout.height)
        createStage()
        addUiTable()
        addTargetWordTable(gameModel)
        addLettersOptionsTable(gameModel)
    }

    private fun addLettersOptionsTable(gameModel: GameModel) {
        lettersOptionsTable = Table()
        lettersOptionsTable.setSize(uiTable.width, uiTable.prefHeight)
        uiTable.add(lettersOptionsTable)
        val brickTexture = assetsManager.getTexture(TexturesDefinitions.BRICK)
        gameModel.options.forEach {
            lettersOptionsTable.add(
                Brick(
                    it.toString(),
                    brickTexture,
                    letterSize,
                    font80,
                )
            ).pad(10F)
            if (lettersOptionsTable.children.size % MAX_OPTIONS_IN_ROW == 0) {
                lettersOptionsTable.row()
            }
        }
    }

    private fun addTargetWordTable(gameModel: GameModel) {
        val cellTexture = assetsManager.getTexture(TexturesDefinitions.CELL)
        val flatBrickTexture = assetsManager.getTexture(TexturesDefinitions.FLAT_BRICK)
        targetWordTable = Table()
        uiTable.debug = DebugSettings.SHOW_UI_BORDERS
        uiTable.add(targetWordTable)
            .pad(TARGET_WORD_TABLE_VERTICAL_PADDING, 0F, TARGET_WORD_TABLE_VERTICAL_PADDING, 0F)
            .row()
        for (i in 0 until gameModel.currentWord.length) {
            addLetterToTargetWord(gameModel, i, cellTexture, flatBrickTexture)
        }
    }

    private fun addLetterToTargetWord(
        gameModel: GameModel,
        i: Int,
        cellTexture: Texture,
        texture: Texture
    ) {
        if (gameModel.hiddenLettersIndices.contains(i)) {
            targetWordTable.add(BrickCell(cellTexture)).pad(TARGET_LETTER_PADDING)
        } else {
            addGivenLetter(gameModel, i, texture)
        }
    }

    private fun addGivenLetter(
        gameModel: GameModel,
        i: Int,
        texture: Texture
    ) {
        targetWordTable.add(
            Brick(
                gameModel.currentWord[i].toString(),
                texture,
                letterSize,
                font80
            )
        ).pad(TARGET_LETTER_PADDING)
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
        private const val TARGET_WORD_TABLE_VERTICAL_PADDING = 160F
        private const val TARGET_LETTER_PADDING = 10F
    }
}