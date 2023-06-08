package com.gadarts.shubutz.core.screens.game.view

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.gadarts.shubutz.core.SoundPlayer
import com.gadarts.shubutz.core.model.GameModel
import com.gadarts.shubutz.core.model.assets.GameAssetManager
import com.gadarts.shubutz.core.model.assets.definitions.FontsDefinitions
import com.gadarts.shubutz.core.model.assets.definitions.ParticleEffectsDefinitions
import com.gadarts.shubutz.core.model.assets.definitions.SoundsDefinitions
import com.gadarts.shubutz.core.model.assets.definitions.TexturesDefinitions
import com.gadarts.shubutz.core.screens.game.GamePlayScreen
import com.gadarts.shubutz.core.screens.game.view.actors.Brick
import com.gadarts.shubutz.core.screens.menu.view.stage.GameStage
import kotlin.math.min

class OptionsView(
    private val stage: GameStage,
    private val soundPlayer: SoundPlayer,
    private val assetsManager: GameAssetManager,
    private val gameModel: GameModel
) {

    var selectedBrick: Brick? = null
    private lateinit var lettersOptionsTable: Table

    fun addLettersOptionsTable(
        uiTable: Table,
        assetsManager: GameAssetManager,
        maxBricksPerLine: Int,
        letterSize: Vector2,
        gamePlayScreen: GamePlayScreen,
    ) {
        lettersOptionsTable = Table()
        lettersOptionsTable.setSize(uiTable.width, uiTable.prefHeight)
        uiTable.add(lettersOptionsTable)
        val brickTexture = assetsManager.getTexture(TexturesDefinitions.BRICK)
        for (row in 0..GameModel.allowedLetters.length / (maxBricksPerLine)) {
            addOptionsRow(row, brickTexture, maxBricksPerLine, letterSize, gamePlayScreen)
        }
        lettersOptionsTable.pack()
    }

    fun onCorrectGuess() {
        selectedBrick!!.listeners.clear()
        markBrick(selectedBrick!!, true)
        selectedBrick = null
    }

    fun onScreenClear() {
        clearAllOptions()
    }

    fun onIncorrectGuess() {
        markBrick(selectedBrick!!, false)
        selectedBrick!!.listeners.clear()
        selectedBrick = null
    }

    fun clearAllOptions() {
        soundPlayer.playSound(assetsManager.getSound(SoundsDefinitions.FLYBY))
        for (i in 0 until lettersOptionsTable.cells.size) {
            if (lettersOptionsTable.cells[i].actor != null) {
                animateBrickFall(lettersOptionsTable.cells[i].actor as Brick)
            }
        }
    }

    private fun animateBrickFall(brick: Brick) {
        brick.remove()
        stage.addActor(brick)
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
                ),
                Actions.removeActor()
            ),
        )
    }

    private fun addBrick(
        letter: Char,
        brickTexture: Texture,
        letterSize: Vector2,
        font80: BitmapFont,
        gamePlayScreen: GamePlayScreen,
        maxBricksPerLine: Int
    ) {
        val brick = Brick(letter.toString(), brickTexture, letterSize, font80)
        initializeBrick(letter, brick, gamePlayScreen)
        lettersOptionsTable.add(brick)
            .pad(10F)
            .size(brickTexture.width.toFloat(), brickTexture.height.toFloat())
        if (lettersOptionsTable.children.size % (maxBricksPerLine) == 0) {
            lettersOptionsTable.row()
        }
    }

    private fun initializeBrick(
        letter: Char,
        brick: Brick,
        gamePlayScreen: GamePlayScreen
    ) {
        if (letter != ' ') {
            brick.addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    if (gameModel.triesLeft == 0 || stage.openDialogs.isNotEmpty() || gameModel.hiddenLettersIndices.isEmpty()) return
                    super.clicked(event, x, y)
                    selectedBrick = brick
                    gamePlayScreen.onBrickClicked(brick.letter[0])
                }
            })
            brick.addAction(
                Actions.sequence(
                    Actions.fadeOut(0F), Actions.fadeIn(
                        0.5F,
                        Interpolation.smooth2
                    )
                )
            )
        } else {
            brick.isVisible = false
        }
    }

    private fun addOptionsRow(
        row: Int,
        brickTexture: Texture,
        maxBricksPerLine: Int,
        letterSize: Vector2,
        gamePlayScreen: GamePlayScreen,
    ) {
        val startIndex = row * (maxBricksPerLine)
        val endIndex =
            min(
                startIndex + (maxBricksPerLine),
                GameModel.allowedLetters.length
            )
        val font80 = assetsManager.getFont(FontsDefinitions.VARELA_80)
        val chars = mutableListOf<Char>()
        chars.addAll(
            GameModel.allowedLetters.subSequence(startIndex, endIndex).asSequence()
        )
        if (row == 3) {
            val blanksToAdd = maxBricksPerLine - chars.size
            for (i in 0 until blanksToAdd) {
                chars.add(' ')
            }
        }
        chars.reversed().forEach {
            addBrick(it, brickTexture, letterSize, font80, gamePlayScreen, maxBricksPerLine)
        }
    }

    private fun markBrick(brick: Brick, correct: Boolean) {
        brick.addAction(
            Actions.sequence(
                Actions.color(
                    if (correct) (if (brick.helped) BRICK_MARK_COLOR_HELPED else BRICK_MARK_COLOR_CORRECT) else BRICK_MARK_COLOR_INCORRECT,
                    1F,
                    Interpolation.slowFast
                ),
                Actions.run { brick.disable() }
            ),
        )
    }

    fun clear() {
        lettersOptionsTable.remove()
    }

    fun onLetterRevealed(letter: Char) {
        val filter = lettersOptionsTable.cells.filter { (it.actor as Brick).letter[0] == letter }
        if (filter.isNotEmpty()) {
            val brick = filter.first().actor
            addEffectForRevealedLetter(brick)
            triggerBrick(brick as Brick)
        }
    }

    private fun triggerBrick(brick: Brick) {
        brick.helped = true
        val down = InputEvent()
        down.type = InputEvent.Type.touchDown
        brick.fire(down)
        val up = InputEvent()
        up.type = InputEvent.Type.touchUp
        brick.fire(up)
    }

    private fun addEffectForRevealedLetter(brick: Actor) {
        val particleEffect = assetsManager.getParticleEffect(ParticleEffectsDefinitions.STARS)
        val particleEffectActor = ParticleEffectActor(particleEffect)
        stage.addActor(particleEffectActor)
        val position = brick.localToScreenCoordinates(Vector2())
        particleEffectActor.setPosition(
            position.x + brick.width / 2F,
            stage.height - position.y - brick.height / 2F
        )
        particleEffectActor.start()
    }

    companion object {
        private val BRICK_MARK_COLOR_INCORRECT = Color.valueOf("#7D0000")
        private val BRICK_MARK_COLOR_CORRECT = Color.valueOf("#007D00")
        private val BRICK_MARK_COLOR_HELPED = Color.valueOf("#bdc22e")
        private const val BRICK_FAIL_ANIMATION_DURATION = 1F
    }

}
