package com.gadarts.shubutz.core.screens.game.view

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.math.Interpolation.circle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Cell
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Disposable
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
    private val stage: GameStage,
) :
    Disposable {

    private val topBarHandler = TopBarHandler()
    private val gamePlayScreenViewHandlers = GamePlayScreenViewHandlers(assetsManager)
    private lateinit var uiTable: Table
    private var font80: BitmapFont = assetsManager.getFont(FontsDefinitions.VARELA_80)
    private lateinit var letterSize: Vector2

    fun onShow() {
        val letterGlyphLayout = GlyphLayout(font80, "א")
        letterSize = Vector2(letterGlyphLayout.width, letterGlyphLayout.height)
        createInterface()
        onGameBegin()
    }

    private fun createInterface() {
        addUiTable()
        topBarHandler.addTopBar(assetsManager, gameModel, gamePlayScreen, font80, stage)
        gamePlayScreenViewHandlers.onShow(
            letterSize,
            font80,
            assetsManager,
            stage,
        )
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

    fun render(delta: Float) {
        stage.act(delta)
        stage.draw()
    }

    override fun dispose() {
        topBarHandler.dispose()
    }

    fun onHide() {
        uiTable.remove()
        topBarHandler.onHide()
    }

    fun onCorrectGuess(indices: List<Int>, gameWin: Boolean) {
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
            topBarHandler.coinsLabel.setText(gameModel.coins.toString())
            val particleEffectActor = ParticleEffectActor(
                assetsManager.getParticleEffect(
                    ParticleEffectsDefinitions.STARS
                )
            )
            stage.addActor(
                particleEffectActor
            )
            particleEffectActor.start()
            val localToScreenCoordinates =
                topBarHandler.coinsLabel.localToStageCoordinates(auxVector.setZero())
            particleEffectActor.setPosition(
                localToScreenCoordinates.x + topBarHandler.coinsLabel.width / 2F,
                localToScreenCoordinates.y + topBarHandler.coinsLabel.height / 2F
            )
        }
        brick.addAction(sequence)
    }

    private fun switchBrickToStage(brick: Brick) {
        brick.remove()
        stage.addActor(brick)
    }

    private fun animateGameWin(stage: GameStage) {
        gamePlayScreenViewHandlers.onGameWinAnimation(stage) { clearScreen() }
    }

    private fun clearScreen() {
        gamePlayScreenViewHandlers.onScreenClear {
            uiTable.clear()
            uiTable.addAction(Actions.run { gamePlayScreen.onScreenEmpty() })
        }
    }


    fun onIncorrectGuess(gameOver: Boolean) {
        gamePlayScreenViewHandlers.bombHandler.updateLabel(gameModel)
        if (gameOver) {
            animateGameOver()
        } else if (gamePlayScreenViewHandlers.optionsHandler.selectedBrick != null) {
            gamePlayScreenViewHandlers.onLetterFail()
        }
    }

    private fun animateGameOver() {
        gamePlayScreenViewHandlers.onGameOverAnimation(stage)
        stage.addAction(Actions.delay(5F, Actions.run { gamePlayScreen.onGameOverAnimationDone() }))
    }


    companion object {
        private const val BRICK_SUCCESS_ANIMATION_DURATION = 1F
        private val auxVector = Vector2()
    }
}