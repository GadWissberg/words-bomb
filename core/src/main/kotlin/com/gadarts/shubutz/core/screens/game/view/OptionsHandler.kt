package com.gadarts.shubutz.core.screens.game.view

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.gadarts.shubutz.core.SoundPlayer
import com.gadarts.shubutz.core.model.GameModel
import com.gadarts.shubutz.core.model.assets.GameAssetManager
import com.gadarts.shubutz.core.model.assets.SoundsDefinitions
import com.gadarts.shubutz.core.model.assets.TexturesDefinitions
import com.gadarts.shubutz.core.screens.game.GamePlayScreen
import com.gadarts.shubutz.core.screens.game.view.actors.Brick
import com.gadarts.shubutz.core.screens.menu.view.stage.GameStage
import kotlin.math.min

class OptionsHandler(
    private val stage: GameStage,
    private val soundPlayer: SoundPlayer,
    private val assetsManager: GameAssetManager
) {

    var selectedBrick: Brick? = null
    private lateinit var lettersOptionsTable: Table

    fun addLettersOptionsTable(
        uiTable: Table,
        assetsManager: GameAssetManager,
        maxBricksPerLine: Int,
        letterSize: Vector2,
        font80: BitmapFont,
        gamePlayScreen: GamePlayScreen
    ) {
        lettersOptionsTable = Table()
        lettersOptionsTable.setSize(uiTable.width, uiTable.prefHeight)
        uiTable.add(lettersOptionsTable)
        val brickTexture = assetsManager.getTexture(TexturesDefinitions.BRICK)
        for (row in 0..GameModel.allowedLetters.length / (maxBricksPerLine - 1)) {
            addOptionsRow(row, brickTexture, maxBricksPerLine, letterSize, font80, gamePlayScreen)
        }
    }

    private fun addOptionsRow(
        row: Int,
        brickTexture: Texture,
        maxBricksPerLine: Int,
        letterSize: Vector2,
        font80: BitmapFont,
        gamePlayScreen: GamePlayScreen
    ) {
        val startIndex = row * (maxBricksPerLine - 1)
        val endIndex =
            min(
                startIndex + (maxBricksPerLine - 1),
                GameModel.allowedLetters.length
            )
        GameModel.allowedLetters.subSequence(startIndex, endIndex)
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
                brick.addAction(
                    Actions.sequence(
                        Actions.fadeOut(0F), Actions.fadeIn(
                            0.5F,
                            Interpolation.smooth2
                        )
                    )
                )
            }
    }

    fun onSelectionSuccessful() {
        selectedBrick!!.listeners.clear()
        selectedBrick!!.remove()
        selectedBrick = null
    }

    fun onScreenClear() {
        clearAllOptions()
    }

    fun onIncorrectGuess() {
        animateBrickFall(selectedBrick!!)
        selectedBrick!!.listeners.clear()
        selectedBrick = null
    }

    fun clearAllOptions() {
        soundPlayer.playSound(assetsManager.getSound(SoundsDefinitions.FLYBY))
        val lastIndex = lettersOptionsTable.cells.size - 1
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

    companion object {
        private const val BRICK_FAIL_ANIMATION_DURATION = 1F
    }

}
