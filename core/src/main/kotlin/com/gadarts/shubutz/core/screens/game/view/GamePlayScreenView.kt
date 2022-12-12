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
import com.badlogic.gdx.scenes.scene2d.ui.Cell
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


    private val wordsTables = ArrayList<Table>()
    private lateinit var targetTable: Table
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

    private fun addTargetTable() {
        targetTable = addTable()
        targetTable.pad(
            TARGET_WORD_TABLE_VERTICAL_PADDING,
            0F,
            TARGET_WORD_TABLE_VERTICAL_PADDING,
            0F
        )
        uiTable.add(targetTable).row()
    }

    fun onGameBegin() {
        addBomb()
        addTargetTable()
        addTargetWordLines()
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

    private fun addTargetWordLines() {
        val cellTexture = assetsManager.getTexture(TexturesDefinitions.CELL)
        val brickTexture = assetsManager.getTexture(TexturesDefinitions.BRICK)
        val words = gameModel.currentTarget.split(' ')
        var letterIndexInCurrentLine = 0
        var globalIndex = 0
        addLineToTargetTable()
        words.indices.forEach {
            val wordTable = Table()
            val currentWord = words[it]
            if (currentWord.length + 1 > maxBricksPerLine - letterIndexInCurrentLine) {
                if (letterIndexInCurrentLine > 0) {
                    globalIndex++
                    addLineToTargetTable()
                    letterIndexInCurrentLine = 0
                }
            }
            if (letterIndexInCurrentLine > 0) {
                addGivenLetter(gameModel, -1, brickTexture, targetWordLines.last())
                globalIndex++
            }
            targetWordLines.last().add(wordTable)
            currentWord.indices.forEach { _ ->
                addLetterToTarget(globalIndex, cellTexture, brickTexture, wordTable)
                letterIndexInCurrentLine++
                globalIndex++
            }
            wordsTables.add(wordTable)
        }
        targetWordLines.reversed().forEach {
            targetTable.add(it)
                .pad(TARGET_WORD_LINE_VERTICAL_PADDING, 0F, TARGET_WORD_LINE_VERTICAL_PADDING, 0F)
                .row()
        }
    }

    private fun addLetterToTarget(
        index: Int,
        texture: Texture,
        brickTexture: Texture,
        wordTable: Table,
    ) {
        if (gameModel.hiddenLettersIndices.contains(index)) {
            addBrickCell(texture, wordTable)
        } else {
            addGivenLetter(gameModel, index, brickTexture, wordTable)
        }
    }

    private fun addBrickCell(texture: Texture, wordTable: Table) {
        val brickCell = BrickCell(texture)
        wordTable.add(brickCell).pad(TARGET_LETTER_PADDING)
        brickCell.touchable = Touchable.enabled
    }

    private fun addGivenLetter(
        gameModel: GameModel,
        i: Int,
        texture: Texture,
        wordTable: Table,
    ) {
        val isLetter = i >= 0 && i < gameModel.currentTarget.length
        val brick = Brick(
            if (isLetter) gameModel.currentTarget[i].toString() else " ",
            texture,
            letterSize,
            font80
        )
        brick.isVisible = isLetter
        wordTable.add(brick).pad(TARGET_LETTER_PADDING)
    }

    private fun addUiTable() {
        uiTable = addTable()
        stage.addActor(uiTable)
        uiTable.setFillParent(true)
    }

    private fun addTable(): Table {
        val table = Table()
        table.debug = DebugSettings.SHOW_UI_BORDERS
        return table
    }

    private fun addLineToTargetTable() {
        val line = Table()
        targetWordLines.add(line)
        line.debug = DebugSettings.SHOW_UI_BORDERS
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
        val wordTable = wordsTables[wordCount]
        val cell = wordTable.cells[letterIndexInWord]
        val localToScreenCoordinates = selectedBrick!!.localToStageCoordinates(auxVector.setZero())
        switchBrickToStage(selectedBrick!!)
        selectedBrick!!.setPosition(
            localToScreenCoordinates.x,
            localToScreenCoordinates.y,
        )
        animateBrickSuccess(cell, gameWin)
        selectedBrick!!.listeners.clear()
        selectedBrick = null
    }

    private fun switchBrickToStage(brick: Brick) {
        brick.remove()
        stage.addActor(brick)
    }

    private fun animateBrickSuccess(cell: Cell<Actor>, gameWin: Boolean) {
        val localToScreenCoordinates = cell.actor.localToStageCoordinates(auxVector.setZero())
        val sequence = Actions.sequence(
            Actions.moveTo(
                localToScreenCoordinates.x,
                localToScreenCoordinates.y,
                BRICK_SUCCESS_ANIMATION_DURATION,
                Interpolation.circle
            ),
            ReplaceCellWithBrickAction(cell, selectedBrick!!)
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
        targetWordLines.clear()
        lettersOptionsTable.remove()
        wordsTables.forEach {
            it.clear()
            it.remove()
        }
        wordsTables.clear()
        targetTable.clear()
        targetTable.remove()
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
        private const val TARGET_WORD_TABLE_VERTICAL_PADDING = 80F
        private const val TARGET_WORD_LINE_VERTICAL_PADDING = 15F
        private const val TARGET_LETTER_PADDING = 10F
        private const val BRICK_SUCCESS_ANIMATION_DURATION = 1F
        private const val BRICK_FAIL_ANIMATION_DURATION = 1F
        private const val GAME_SUCCESS_ANIMATION_DISTANCE = 50F
        private const val GAME_SUCCESS_ANIMATION_DURATION = 0.5F
        private const val BOMB_PADDING = 20F
        private const val WIN_DELAY = 3F
        private const val OPTIONS_BRICK_FALL_MAX_DELAY = 1000F
        private const val NOTIFY_SCREEN_EMPTY_DELAY = 2F
        private val auxVector = Vector2()
    }
}