package com.gadarts.shubutz.core.screens.game.view

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.viewport.FitViewport
import com.gadarts.shubutz.core.DebugSettings
import com.gadarts.shubutz.core.Notifier
import com.gadarts.shubutz.core.model.GameModel
import com.gadarts.shubutz.core.model.GameModel.Companion.MAX_OPTIONS
import com.gadarts.shubutz.core.model.assets.FontsDefinitions
import com.gadarts.shubutz.core.model.assets.GameAssetManager
import com.gadarts.shubutz.core.model.assets.ParticleEffectsDefinitions.FIRE
import com.gadarts.shubutz.core.model.assets.TexturesDefinitions
import com.gadarts.shubutz.core.model.view.Brick
import com.gadarts.shubutz.core.model.view.BrickCell
import com.gadarts.shubutz.core.screens.menu.view.stage.GameStage


class GamePlayScreenView(
    private val assetsManager: GameAssetManager,
    private val gameModel: GameModel
) :
    Disposable,
    Notifier<GamePlayScreenViewEventsSubscriber> {


    private var maxBricksPerLine: Int = 0
    private lateinit var bomb: Bomb
    private lateinit var fireParticleEffectActor: FireParticleEffectActor
    private var selectedBrick: Brick? = null
    private var targetWordLines = ArrayList<Table>()
    private lateinit var lettersOptionsTable: Table
    private lateinit var letterGlyphLayout: GlyphLayout
    private lateinit var uiTable: Table
    private lateinit var stage: GameStage
    override val subscribers = HashSet<GamePlayScreenViewEventsSubscriber>()
    private var font80: BitmapFont = assetsManager.getFont(FontsDefinitions.VARELA_80)
    private lateinit var letterSize: Vector2

    fun onShow() {
        letterGlyphLayout = GlyphLayout(font80, "א")
        letterSize = Vector2(letterGlyphLayout.width, letterGlyphLayout.height)
        val brickTexture = assetsManager.getTexture(TexturesDefinitions.BRICK)
        maxBricksPerLine = Gdx.graphics.width / brickTexture.width - 1
        createStage()
        addUiTable()
        onGameBegin()
    }

    fun onGameBegin() {
        addBomb()
        addTargetWordTable()
        addLettersOptionsTable()
    }

    private fun addBomb() {
        fireParticleEffectActor = FireParticleEffectActor(assetsManager.getParticleEffect(FIRE))
        bomb = Bomb(
            assetsManager.getTexture(TexturesDefinitions.BOMB),
            fireParticleEffectActor,
            assetsManager.getFont(FontsDefinitions.VARELA_320),
            gameModel.triesLeft
        )
        stage.addActor(fireParticleEffectActor)
        uiTable.add(bomb).pad(BOMB_PADDING).row()
    }

    private fun addLettersOptionsTable() {
        lettersOptionsTable = Table()
        lettersOptionsTable.setSize(uiTable.width, uiTable.prefHeight)
        uiTable.add(lettersOptionsTable)
        val brickTexture = assetsManager.getTexture(TexturesDefinitions.BRICK)
        gameModel.options.forEach {
            val brick = Brick(it.toString(), brickTexture, letterSize, font80)
            brick.addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    super.clicked(event, x, y)
                    selectedBrick = brick
                    subscribers.forEach { sub -> sub.onBrickClicked(brick.letter[0]) }
                }
            })
            lettersOptionsTable.add(brick).pad(10F)
            if (lettersOptionsTable.children.size % MAX_OPTIONS_IN_ROW == 0) {
                lettersOptionsTable.row()
            }
        }
    }

    private fun addTargetWordTable() {
        val cellTexture = assetsManager.getTexture(TexturesDefinitions.CELL)
        val brickTexture = assetsManager.getTexture(TexturesDefinitions.BRICK)
        createTargetWordTable()
        targetWordLines.forEach {
            uiTable.add(it)
                .pad(TARGET_WORD_TABLE_VERTICAL_PADDING, 0F, TARGET_WORD_TABLE_VERTICAL_PADDING, 0F)
                .row()
        }
        for (i in 0 until gameModel.currentWord.length) {
            addLetterToTargetWord(i, cellTexture, brickTexture)
        }
    }

    private fun addLetterToTargetWord(
        i: Int,
        texture: Texture,
        brickTexture: Texture,
    ) {
        if (gameModel.hiddenLettersIndices.contains(i)) {
            addBrickCell(texture)
        } else {
            addGivenLetter(gameModel, i, brickTexture)
        }
        if (i > 0 && i % maxBricksPerLine == maxBricksPerLine - 1) {
            targetWordLines.add(Table())
        }
    }

    private fun addBrickCell(texture: Texture) {
        val brickCell = BrickCell(texture)
        targetWordLines[targetWordLines.size - 1].add(brickCell).pad(TARGET_LETTER_PADDING)
        brickCell.touchable = Touchable.enabled
    }

    private fun addGivenLetter(
        gameModel: GameModel,
        i: Int,
        texture: Texture,
    ) {
        targetWordLines.last().add(
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

    private fun createTargetWordTable() {
        targetWordLines.add(Table())
        uiTable.debug = DebugSettings.SHOW_UI_BORDERS
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
    }

    fun onHide() {
    }

    fun onGuessSuccess(index: Int, gameWin: Boolean) {
        if (selectedBrick != null) {
            selectionSuccessful(index, gameWin)
        }
    }

    private fun selectionSuccessful(index: Int, gameWin: Boolean) {
        switchBrickToStage(selectedBrick!!)
        val cell = targetWordLines.last().cells[index].actor
        selectedBrick!!.setPosition(
            lettersOptionsTable.x + selectedBrick!!.x,
            lettersOptionsTable.y + selectedBrick!!.y
        )
        animateBrickSuccess(cell, index, gameWin)
        selectedBrick!!.listeners.clear()
        selectedBrick = null
    }

    private fun switchBrickToStage(brick: Brick) {
        brick.remove()
        stage.addActor(brick)
    }

    private fun animateBrickSuccess(cell: Actor, index: Int, gameWin: Boolean) {
        val last = targetWordLines.last()
        val sequence = Actions.sequence(
            Actions.moveTo(
                last.x + cell.x,
                last.y + cell.y,
                BRICK_SUCCESS_ANIMATION_DURATION,
                Interpolation.circle
            ),
            ReplaceCellWithBrickAction(last, selectedBrick!!, index)
        )

        if (gameWin) {
            sequence.addAction(WordRevealedAction { animateGameWin() })
        }
        selectedBrick!!.addAction(sequence)
    }

    private fun animateGameWin() {
        fireParticleEffectActor.stop()
        val last = targetWordLines.last()
        for (i in 0 until last.cells.size) {
            val actor = last.cells[i].actor
            actor.addAction(
                Actions.sequence(
                    Actions.delay(i / 10F),
                    Actions.moveBy(
                        0F,
                        GAME_SUCCESS_ANIMATION_DISTANCE,
                        GAME_SUCCESS_ANIMATION_DURATION
                    ),
                    Actions.moveBy(
                        0F,
                        -2 * GAME_SUCCESS_ANIMATION_DISTANCE,
                        GAME_SUCCESS_ANIMATION_DURATION
                    ),
                    Actions.moveBy(
                        0F,
                        GAME_SUCCESS_ANIMATION_DISTANCE,
                        GAME_SUCCESS_ANIMATION_DURATION
                    ),
                )
            )
            if (i == last.cells.size - 1) {
                actor.addAction(
                    Actions.sequence(
                        Actions.delay(WIN_DELAY),
                        Actions.run { clearScreen() }
                    ))
            }
        }
    }

    private fun clearScreen() {

        bomb.addAction(
            Actions.sequence(
                Actions.sizeTo(0F, 0F, 1F, Interpolation.exp10),
                Actions.removeActor()
            )
        )

        clearAllOptions()

        lettersOptionsTable.cells.forEach {
            if (it.actor != null) {
                it.actor.addAction(
                    Actions.sequence(
                        Actions.delay(MathUtils.random(OPTIONS_BRICK_FALL_MAX_DELAY)),
                        Actions.sizeTo(0F, 0F, 1F, Interpolation.circle),
                        Actions.removeActor()
                    )
                )
            }
        }

        targetWordLines.forEach { it.remove() }
        lettersOptionsTable.remove()
        uiTable.clear()
        uiTable.addAction(
            Actions.delay(
                NOTIFY_SCREEN_EMPTY_DELAY,
                Actions.run { subscribers.forEach { sub -> sub.onScreenEmpty() } })
        )
    }

    fun onGuessFail(gameOver: Boolean) {
        bomb.updateLabel(gameModel.triesLeft)
        if (gameOver) {
            animateGameOver()
        } else if (selectedBrick != null) {
            if (!fireParticleEffectActor.started) {
                bomb.startFire()
            }
            animateBrickFail(selectedBrick!!)
            selectedBrick!!.listeners.clear()
            selectedBrick = null
        }
    }

    private fun animateGameOver() {
        clearAllOptions()
    }

    private fun clearAllOptions() {
        lettersOptionsTable.cells.forEach {
            if (it.actor != null) {
                animateBrickFail(it.actor as Brick)
            }
        }
    }

    private fun animateBrickFail(brick: Brick) {
        switchBrickToStage(brick)
        brick.setPosition(
            lettersOptionsTable.x + brick.x,
            lettersOptionsTable.y + brick.y
        )

        brick.addAction(
            Actions.sequence(
                Actions.moveTo(
                    brick.x,
                    -brick.height,
                    BRICK_FAIL_ANIMATION_DURATION,
                    Interpolation.exp10
                ), Actions.removeActor()
            ),
        )
    }

    companion object {
        private const val MAX_OPTIONS_IN_ROW = MAX_OPTIONS / 3
        private const val TARGET_WORD_TABLE_VERTICAL_PADDING = 160F
        private const val TARGET_LETTER_PADDING = 10F
        private const val BRICK_SUCCESS_ANIMATION_DURATION = 1F
        private const val BRICK_FAIL_ANIMATION_DURATION = 1F
        private const val GAME_SUCCESS_ANIMATION_DISTANCE = 50F
        private const val GAME_SUCCESS_ANIMATION_DURATION = 0.5F
        private const val BOMB_PADDING = 20F
        private const val WIN_DELAY = 3F
        private const val OPTIONS_BRICK_FALL_MAX_DELAY = 1000F
        private const val NOTIFY_SCREEN_EMPTY_DELAY = 2F
    }
}