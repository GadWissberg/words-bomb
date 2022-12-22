package com.gadarts.shubutz.core.screens.game.view

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Pixmap.Format
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.ParticleEffect
import com.badlogic.gdx.math.Interpolation.*
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Cell
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.viewport.FitViewport
import com.gadarts.shubutz.core.DebugSettings
import com.gadarts.shubutz.core.model.GameModel
import com.gadarts.shubutz.core.model.GameModel.Companion.allowedLetters
import com.gadarts.shubutz.core.model.assets.FontsDefinitions
import com.gadarts.shubutz.core.model.assets.GameAssetManager
import com.gadarts.shubutz.core.model.assets.ParticleEffectsDefinitions
import com.gadarts.shubutz.core.model.assets.ParticleEffectsDefinitions.FIRE
import com.gadarts.shubutz.core.model.assets.ParticleEffectsDefinitions.STARS
import com.gadarts.shubutz.core.model.assets.TexturesDefinitions
import com.gadarts.shubutz.core.screens.game.GamePlayScreen
import com.gadarts.shubutz.core.screens.game.view.actors.Brick
import com.gadarts.shubutz.core.screens.game.view.actors.BrickCell
import com.gadarts.shubutz.core.screens.menu.view.stage.GameStage
import kotlin.math.min


class GamePlayScreenView(
    private val assetsManager: GameAssetManager,
    private val gameModel: GameModel,
    private val gamePlayScreen: GamePlayScreen
) :
    Disposable {


    private lateinit var topBarTexture: Texture
    private val wordsTables = ArrayList<Table>()
    private lateinit var targetTable: Table
    private var maxBricksPerLine: Int = 0
    private lateinit var bomb: Bomb
    private lateinit var fireParticleEffectActor: ParticleEffectActor
    private lateinit var explosionParticleEffectActor: ParticleEffectActor
    private var selectedBrick: Brick? = null
    private var targetWordLines = ArrayList<Table>()
    private lateinit var lettersOptionsTable: Table
    private lateinit var letterGlyphLayout: GlyphLayout
    private lateinit var uiTable: Table
    private lateinit var stage: GameStage
    private var font80: BitmapFont = assetsManager.getFont(FontsDefinitions.VARELA_80)
    private lateinit var letterSize: Vector2

    fun onShow() {
        letterGlyphLayout = GlyphLayout(font80, "א")
        letterSize = Vector2(letterGlyphLayout.width, letterGlyphLayout.height)
        calculateMaxBricksPerLine()
        createInterface()
    }

    private fun createInterface() {
        createStage()
        addUiTable()
        addTopBar()
        onGameBegin()
    }

    private fun calculateMaxBricksPerLine() {
        val brickTexture = assetsManager.getTexture(TexturesDefinitions.BRICK)
        maxBricksPerLine = Gdx.graphics.width / brickTexture.width - 1
    }

    private fun addTopBar() {
        createTopBarTexture()
        val table = Table()
        table.background = TextureRegionDrawable(topBarTexture)
        table.setSize(stage.width, TOP_BAR_HEIGHT.toFloat())
        table.debug = DebugSettings.SHOW_UI_BORDERS
        stage.addActor(table)
        table.setPosition(0F, stage.height - table.height)
        addBackButton(table)
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
        fireParticleEffectActor = ParticleEffectActor(assetsManager.getParticleEffect(FIRE))
        createBomb()
        stage.addActor(fireParticleEffectActor)
        val bombTexture = assetsManager.getTexture(TexturesDefinitions.BOMB)
        uiTable.add(bomb).size(bombTexture.width.toFloat(), bombTexture.height.toFloat())
            .pad(BOMB_PADDING).row()
    }

    private fun createBomb() {
        val bombTexture = assetsManager.getTexture(TexturesDefinitions.BOMB)
        bomb = Bomb(
            bombTexture,
            fireParticleEffectActor,
            assetsManager.getFont(FontsDefinitions.VARELA_320),
            gameModel.triesLeft
        )
        bomb.setOrigin(bombTexture.width / 2F, bombTexture.height / 2F)
    }

    private fun addLettersOptionsTable() {
        lettersOptionsTable = Table()
        lettersOptionsTable.setSize(uiTable.width, uiTable.prefHeight)
        uiTable.add(lettersOptionsTable)
        val brickTexture = assetsManager.getTexture(TexturesDefinitions.BRICK)
        for (row in 0..allowedLetters.length / (maxBricksPerLine - 1)) {
            addOptionsRow(row, brickTexture)
        }
    }

    private fun addOptionsRow(row: Int, brickTexture: Texture) {
        val startIndex = row * (maxBricksPerLine - 1)
        val endIndex = min(startIndex + (maxBricksPerLine - 1), allowedLetters.length)
        allowedLetters.subSequence(startIndex, endIndex)
            .reversed()
            .forEach {
                val brick = Brick(it.toString(), brickTexture, letterSize, font80)
                brick.addListener(object : ClickListener() {
                    override fun clicked(event: InputEvent?, x: Float, y: Float) {
                        super.clicked(event, x, y)
                        selectedBrick = brick
                        gamePlayScreen.onBrickClicked(brick.letter[0])
                    }
                })
                lettersOptionsTable.add(brick)
                    .pad(10F)
                    .size(brickTexture.width.toFloat(), brickTexture.height.toFloat())
                if (lettersOptionsTable.children.size % (maxBricksPerLine - 1) == 0) {
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
        uiTable.setFillParent(true)
        stage.addActor(uiTable)
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
        topBarTexture.dispose()
    }

    fun onHide() {
    }

    fun onGuessSuccess(indices: List<Int>, gameWin: Boolean) {
        if (selectedBrick != null) {
            selectionSuccessful(indices, gameWin)
        }
    }

    private fun selectionSuccessful(indices: List<Int>, gameWin: Boolean) {
        val brickTexture = assetsManager.getTexture(TexturesDefinitions.BRICK)
        indices.forEach {
            animateBrickSuccess(it, gameWin, brickTexture)
        }
        selectedBrick!!.listeners.clear()
        selectedBrick!!.remove()
        selectedBrick = null
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
        val wordTable = wordsTables[wordCount]
        val cell = wordTable.cells[letterIndexInWord]
        val selectedBrickScreenCoords = selectedBrick!!.localToStageCoordinates(auxVector.setZero())
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
            Actions.run { (cell as Cell).setActor(brick) }
        )

        if (gameWin) {
            sequence.addAction(Actions.run { animateGameWin() })
        }
        brick.addAction(sequence)
    }

    private fun switchBrickToStage(brick: Brick) {
        brick.remove()
        stage.addActor(brick)
    }

    private fun animateGameWin() {
        fireParticleEffectActor.stop()
        var i = 0
        val particleEffect = assetsManager.getParticleEffect(STARS)
        targetWordLines.reversed().forEach {
            it.cells.forEach { wordCell ->
                if (wordCell.actor is Table) {
                    val word = wordCell.actor as Table
                    val size = word.cells.size
                    for (letterIndex in 0 until size) {
                        val actor = word.cells[letterIndex].actor
                        animateLetterForGameWin(actor, i, word, particleEffect)
                        i++
                    }
                }
            }
        }
    }

    private fun animateLetterForGameWin(
        actor: Actor,
        i: Int,
        word: Table,
        particleEffect: ParticleEffect
    ) {
        val particleEffectActor = ParticleEffectActor(ParticleEffect(particleEffect))
        val localToScreenCoordinates = actor.localToStageCoordinates(auxVector.setZero())
        particleEffectActor.setPosition(
            localToScreenCoordinates.x + actor.width / 2F,
            localToScreenCoordinates.y + actor.height / 2F
        )
        stage.addActor(particleEffectActor)

        actor.addAction(
            Actions.sequence(
                Actions.delay(i / 10F),
                Actions.run { particleEffectActor.start() },
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
        if (i == word.cells.size - 1) {
            actor.addAction(
                Actions.sequence(
                    Actions.delay(WIN_DELAY),
                    Actions.run { clearScreen() }
                ))
        }
    }

    private fun clearScreen() {

        bomb.addAction(
            Actions.sequence(
                Actions.sizeTo(0F, 0F, 1F, exp10),
                Actions.removeActor()
            )
        )

        clearAllOptions()

        lettersOptionsTable.cells.forEach {
            if (it.actor != null) {
                it.actor.addAction(
                    Actions.sequence(
                        Actions.delay(MathUtils.random(OPTIONS_BRICK_FALL_MAX_DELAY)),
                        Actions.sizeTo(0F, 0F, 1F, circle),
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
                Actions.run { gamePlayScreen.onScreenEmpty() })
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
        fireParticleEffectActor.stop()
        val particleEffect = assetsManager.getParticleEffect(ParticleEffectsDefinitions.EXP)
        val bombPosition = bomb.localToScreenCoordinates(auxVector.setZero())
        explosionParticleEffectActor = ParticleEffectActor(particleEffect)
        explosionParticleEffectActor.setPosition(
            bombPosition.x + bomb.width / 2F,
            stage.height - bombPosition.y + bomb.height / 5F
        )

        bomb.addAction(
            Actions.parallel(
                Actions.sequence(
                    Actions.sizeTo(0F, 0F, BOMB_GAME_OVER_ANIMATION_DURATION, linear),
                    Actions.delay(10F, Actions.run { gamePlayScreen.onGameOverAnimationDone() })
                ),
                Actions.moveBy(
                    bomb.width / 2F,
                    bomb.height / 2F,
                    BOMB_GAME_OVER_ANIMATION_DURATION,
                    linear
                )
            )
        )

        stage.addActor(explosionParticleEffectActor)
        explosionParticleEffectActor.start()
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
                    exp10
                ), Actions.removeActor()
            ),
        )
    }

    companion object {
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
        private const val TOP_BAR_HEIGHT = 150
        private const val TOP_BAR_COLOR = "#85adb0"
        private const val BOMB_GAME_OVER_ANIMATION_DURATION = 0.5F
        private val auxVector = Vector2()
    }
}