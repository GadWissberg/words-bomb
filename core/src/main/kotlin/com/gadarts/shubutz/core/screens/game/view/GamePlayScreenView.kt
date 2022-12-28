package com.gadarts.shubutz.core.screens.game.view

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Pixmap.Format
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.math.Interpolation.*
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.viewport.FitViewport
import com.gadarts.shubutz.core.DebugSettings
import com.gadarts.shubutz.core.model.GameModel
import com.gadarts.shubutz.core.model.assets.FontsDefinitions
import com.gadarts.shubutz.core.model.assets.GameAssetManager
import com.gadarts.shubutz.core.model.assets.ParticleEffectsDefinitions
import com.gadarts.shubutz.core.model.assets.TexturesDefinitions
import com.gadarts.shubutz.core.screens.game.GamePlayScreen
import com.gadarts.shubutz.core.screens.game.view.actors.Brick
import com.gadarts.shubutz.core.screens.menu.view.stage.GameStage


class GamePlayScreenView(
    private val assetsManager: GameAssetManager,
    private val gameModel: GameModel,
    private val gamePlayScreen: GamePlayScreen,
) :
    Disposable {

    private lateinit var coinsLabel: Label
    private lateinit var topBarTexture: Texture
    private val gamePlayScreenViewHandlers = GamePlayScreenViewHandlers(assetsManager)
    private lateinit var uiTable: Table
    private lateinit var stage: GameStage
    private var font80: BitmapFont = assetsManager.getFont(FontsDefinitions.VARELA_80)
    private lateinit var letterSize: Vector2

    fun onShow() {
        val letterGlyphLayout = GlyphLayout(font80, "א")
        letterSize = Vector2(letterGlyphLayout.width, letterGlyphLayout.height)
        createInterface()
        onGameBegin()
    }

    private fun createInterface() {
        createStage()
        addUiTable()
        addTopBar()
        gamePlayScreenViewHandlers.onShow(
            letterSize,
            font80,
            assetsManager,
            stage,
            uiTable,
            gameModel
        )
    }

    private fun addTopBar() {
        createTopBarTexture()
        val table = Table()
        table.background = TextureRegionDrawable(topBarTexture)
        table.setSize(stage.width, TOP_BAR_HEIGHT.toFloat())
        table.debug = DebugSettings.SHOW_UI_BORDERS
        stage.addActor(table)
        table.setPosition(0F, stage.height - table.height)
        addTopBarComponents(table)
    }

    private fun addTopBarComponents(table: Table) {
        addBackButton(table)
        val coinsTexture = assetsManager.getTexture(TexturesDefinitions.COINS_ICON)
        coinsLabel = Label(gameModel.coins.toString(), LabelStyle(font80, Color.WHITE))
        table.add(coinsLabel)
            .pad(0F, 0F, 0F, COINS_LABEL_PADDING_RIGHT)
        table.add(Image(coinsTexture))
            .size(topBarTexture.height.toFloat(), topBarTexture.height.toFloat()).pad(40F)
    }

    private fun addBackButton(table: Table) {
        val texture = assetsManager.getTexture(TexturesDefinitions.BACK_BUTTON)
        val button = ImageButton(TextureRegionDrawable(texture))
        button.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                gamePlayScreen.onClickedBackButton()
            }
        })
        table.add(button).expandX().pad(40F).left()
    }

    private fun createTopBarTexture() {
        val pixmap = Pixmap(stage.width.toInt(), TOP_BAR_HEIGHT, Format.RGBA8888)
        val color = Color.valueOf(TOP_BAR_COLOR)
        color.a /= 2F
        pixmap.setColor(color)
        pixmap.fill()
        topBarTexture = Texture(pixmap)
        pixmap.dispose()
    }


    fun onGameBegin() {
        gamePlayScreenViewHandlers.onGameBegin(
            uiTable,
            gameModel,
            letterSize,
            font80,
            gamePlayScreen,
            stage
        )

    }

    private fun addUiTable() {
        uiTable = addTable()
        uiTable.setFillParent(true)
        stage.addActor(uiTable)
    }

    private fun addTable(): Table {
        val table = Table()
        table.debug = DebugSettings.SHOW_UI_BORDERS
        return table
    }


    private fun createStage() {
        stage = GameStage(
            FitViewport(Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat()),
            assetsManager
        )
        stage.setDebugInvisible(DebugSettings.SHOW_UI_BORDERS)
        Gdx.input.inputProcessor = stage
    }

    fun render(delta: Float) {
        stage.act(delta)
        stage.draw()
    }

    override fun dispose() {
        stage.dispose()
        topBarTexture.dispose()
    }

    fun onHide() {
    }

    fun onGuessSuccess(indices: List<Int>, gameWin: Boolean) {
        if (gamePlayScreenViewHandlers.optionsHandler.selectedBrick != null) {
            selectionSuccessful(indices, gameWin)
        }
    }

    private fun selectionSuccessful(indices: List<Int>, gameWin: Boolean) {
        val brickTexture = assetsManager.getTexture(TexturesDefinitions.BRICK)
        indices.forEach {
            animateBrickSuccess(it, gameWin, brickTexture)
        }
        gamePlayScreenViewHandlers.optionsHandler.onSelectionSuccessful()
    }

    private fun animateBrickSuccess(index: Int, gameWin: Boolean, brickTexture: Texture) {
        var wordCount = 0
        var letterIndexInWord = 0
        for (i in 0 until index) {
            if (gameModel.currentTarget[i] == ' ') {
                wordCount++
                letterIndexInWord = 0
            } else {
                letterIndexInWord++
            }
        }
        val wordTable = gamePlayScreenViewHandlers.targetWordsHandler.wordsTables[wordCount]
        val cell = wordTable.cells[letterIndexInWord]
        val selectedBrickScreenCoords =
            gamePlayScreenViewHandlers.optionsHandler.selectedBrick!!.localToStageCoordinates(
                auxVector.setZero()
            )
        val brick =
            Brick(gameModel.currentTarget[index].toString(), brickTexture, letterSize, font80)
        brick.setPosition(
            selectedBrickScreenCoords.x,
            selectedBrickScreenCoords.y,
        )
        val cellActorScreenCoordinates = cell.actor.localToStageCoordinates(auxVector.setZero())
        switchBrickToStage(brick)
        val sequence = Actions.sequence(
            Actions.moveTo(
                cellActorScreenCoordinates.x,
                cellActorScreenCoordinates.y,
                BRICK_SUCCESS_ANIMATION_DURATION,
                circle
            ),
            Actions.run { (cell as Cell<*>).setActor(brick) }
        )

        if (gameWin) {
            sequence.addAction(Actions.run { animateGameWin(stage) })
            coinsLabel.setText(gameModel.coins.toString())
            val particleEffectActor = ParticleEffectActor(
                assetsManager.getParticleEffect(
                    ParticleEffectsDefinitions.STARS
                )
            )
            stage.addActor(
                particleEffectActor
            )
            particleEffectActor.start()
            val localToScreenCoordinates = coinsLabel.localToStageCoordinates(auxVector.setZero())
            particleEffectActor.setPosition(
                localToScreenCoordinates.x + coinsLabel.width / 2F,
                localToScreenCoordinates.y + coinsLabel.height / 2F
            )
        }
        brick.addAction(sequence)
    }

    private fun switchBrickToStage(brick: Brick) {
        brick.remove()
        stage.addActor(brick)
    }

    private fun animateGameWin(stage: GameStage) {
        gamePlayScreenViewHandlers.onGameWinAnimation(stage, Actions.run { clearScreen() })
    }

    private fun clearScreen() {
        gamePlayScreenViewHandlers.onScreenClear {
            uiTable.clear()
            uiTable.addAction(Actions.run { gamePlayScreen.onScreenEmpty() })
        }
    }


    fun onGuessFail(gameOver: Boolean) {
        gamePlayScreenViewHandlers.bombHandler.updateLabel(gameModel)
        if (gameOver) {
            animateGameOver()
        } else if (gamePlayScreenViewHandlers.optionsHandler.selectedBrick != null) {
            gamePlayScreenViewHandlers.onLetterFail()
        }
    }

    private fun animateGameOver() {
        gamePlayScreenViewHandlers.onGameOverAnimation(stage, gamePlayScreen)
        stage.addAction(Actions.delay(5F, Actions.run { gamePlayScreen.onGameOverAnimationDone() }))
    }


    companion object {
        private const val BRICK_SUCCESS_ANIMATION_DURATION = 1F
        private const val TOP_BAR_HEIGHT = 150
        private const val TOP_BAR_COLOR = "#85adb0"
        private const val COINS_LABEL_PADDING_RIGHT = 40F
        private val auxVector = Vector2()
    }
}