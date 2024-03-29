package com.gadarts.shubutz.core.screens.game.view

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.ParticleEffect
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Cell
import com.badlogic.gdx.scenes.scene2d.ui.Stack
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.gadarts.shubutz.core.DebugSettings
import com.gadarts.shubutz.core.ShubutzGame
import com.gadarts.shubutz.core.SoundPlayer
import com.gadarts.shubutz.core.model.GameModel
import com.gadarts.shubutz.core.model.GameModel.Companion.wordRevealFree
import com.gadarts.shubutz.core.model.assets.GameAssetManager
import com.gadarts.shubutz.core.model.assets.definitions.ParticleEffectsDefinitions
import com.gadarts.shubutz.core.model.assets.definitions.SoundsDefinitions
import com.gadarts.shubutz.core.model.assets.definitions.TexturesDefinitions
import com.gadarts.shubutz.core.screens.game.GamePlayScreen
import com.gadarts.shubutz.core.screens.game.GlobalHandlers
import com.gadarts.shubutz.core.screens.game.view.actors.Brick
import com.gadarts.shubutz.core.screens.game.view.actors.BrickCell
import com.gadarts.shubutz.core.screens.menu.view.stage.GameStage
import ktx.actors.alpha

class TargetPhraseView(
    private val letterSize: Vector2,
    private val font80: BitmapFont,
    private val soundPlayer: SoundPlayer,
    private val assetsManager: GameAssetManager
) {
    private var gameWinAnimationRuns: Boolean = false
    private val wordsTables = ArrayList<Table>()
    private lateinit var targetTable: Table
    var maxBricksPerLine: Int = 0
    private var targetWordLines = ArrayList<Table>()


    fun calculateMaxBricksPerLine(assetsManager: GameAssetManager) {
        val brickTexture = assetsManager.getTexture(TexturesDefinitions.BRICK)
        maxBricksPerLine = ShubutzGame.RESOLUTION_WIDTH / brickTexture.width - 2
    }

    private fun addBrickCell(texture: Texture, wordTable: Table) {
        val brickCell = BrickCell(texture, letterSize, font80)
        wordTable.add(Stack(brickCell)).size(letterSize.x).pad(LETTER_PADDING)
        brickCell.touchable = Touchable.enabled
    }

    private fun addGivenLetter(
        gameModel: GameModel,
        i: Int,
        brickTexture: Texture,
        wordTable: Table,
    ) {
        val isLetter = i >= 0 && i < gameModel.currentTargetData.currentPhrase.length
        val brick = Brick(
            if (isLetter) gameModel.currentTargetData.currentPhrase[i].toString() else " ",
            brickTexture,
            letterSize,
            font80
        )
        brick.isVisible = isLetter
        brick.alpha = 0F
        wordTable.add(brick).size(letterSize.x).pad(LETTER_PADDING)
    }

    private fun addLetterToTarget(
        index: Int,
        cellTexture: Texture,
        brickTexture: Texture,
        wordTable: Table,
        gameModel: GameModel,
    ) {
        if (gameModel.currentTargetData.hiddenLettersIndices.contains(index)) {
            addBrickCell(cellTexture, wordTable)
        } else {
            addGivenLetter(gameModel, index, brickTexture, wordTable)
        }
    }

    private fun addTable(): Table {
        val table = Table()
        table.debug = DebugSettings.SHOW_UI_BORDERS
        return table
    }

    private fun addTargetTable(uiTable: Table) {
        targetTable = addTable()
        targetTable.pad(
            TARGET_WORD_TABLE_VERTICAL_PADDING,
            0F,
            TARGET_WORD_TABLE_VERTICAL_PADDING,
            0F
        )
        uiTable.add(targetTable).row()
    }

    private fun addTargetWordLines(gameModel: GameModel, assetsManager: GameAssetManager) {
        val cellTexture = assetsManager.getTexture(TexturesDefinitions.CELL)
        val brickTexture = assetsManager.getTexture(TexturesDefinitions.BRICK)
        val words = gameModel.currentTargetData.currentPhrase.split(' ')
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
                addLetterToTarget(globalIndex, cellTexture, brickTexture, wordTable, gameModel)
                letterIndexInCurrentLine++
                globalIndex++
            }
            wordsTables.add(wordTable)
        }
        targetWordLines.reversed().forEach {
            targetTable.add(it)
                .pad(
                    TARGET_WORD_LINE_VERTICAL_PADDING, 0F,
                    TARGET_WORD_LINE_VERTICAL_PADDING, 0F
                )
                .row()
        }
    }

    private fun addLineToTargetTable() {
        val line = Table()
        targetWordLines.add(line)
        line.debug = DebugSettings.SHOW_UI_BORDERS
    }

    private fun animateLetterForGameWin(
        actor: Actor,
        i: Int,
        word: Table,
        particleEffect: ParticleEffect,
        stage: GameStage,
        runOnAnimationFinish: Runnable
    ) {
        val particleEffectActor = ParticleEffectActor(particleEffect)
        val localToScreenCoordinates =
            actor.localToStageCoordinates(auxVector.setZero())
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
                    GAME_WIN_ANIMATION_DISTANCE,
                    GAME_WIN_ANIMATION_LETTER_MOVE_DURATION
                ),
                Actions.run {
                    val sound = assetsManager.getSound(SoundsDefinitions.BRICK_JUMP)
                    soundPlayer.playSound(sound)
                },
                Actions.moveBy(
                    0F,
                    -2 * GAME_WIN_ANIMATION_DISTANCE,
                    GAME_WIN_ANIMATION_LETTER_MOVE_DURATION
                ),
                Actions.moveBy(
                    0F,
                    GAME_WIN_ANIMATION_DISTANCE,
                    GAME_WIN_ANIMATION_LETTER_MOVE_DURATION
                ),
            )
        )
        if (i == word.cells.size - 1) {
            actor.addAction(
                Actions.sequence(
                    Actions.delay(WIN_DELAY),
                    Actions.run { runOnAnimationFinish.run() }
                )
            )
        }
    }

    fun applyGameWinAnimation(
        assetsManager: GameAssetManager,
        stage: GameStage,
        runOnAnimationFinish: Runnable
    ) {
        if (gameWinAnimationRuns) return

        gameWinAnimationRuns = true
        soundPlayer.playSound(assetsManager.getSound(SoundsDefinitions.WIN))
        var i = 0
        val particleEffect = assetsManager.getParticleEffect(ParticleEffectsDefinitions.STARS)
        targetWordLines.reversed().forEach {
            it.cells.forEach { wordCell ->
                if (wordCell.actor is Table) {
                    val word = wordCell.actor as Table
                    val size = word.cells.size
                    for (letterIndex in 0 until size) {
                        val actor = word.cells[letterIndex].actor
                        if (actor is Stack) {
                            actor.removeActorAt(0, true)
                        }
                        animateLetterForGameWin(
                            actor,
                            i,
                            word,
                            particleEffect,
                            stage,
                            runOnAnimationFinish
                        )
                        i++
                    }
                }
            }
        }

    }

    fun onScreenClear() {
        targetWordLines.forEach { it.remove() }
        targetWordLines.clear()
        wordsTables.forEach {
            it.clear()
            it.remove()
        }
        wordsTables.clear()
        targetTable.clear()
        targetTable.remove()
    }

    fun onGameBegin(gameModel: GameModel, assetsManager: GameAssetManager, uiTable: Table) {
        gameWinAnimationRuns = false
        addTargetTable(uiTable)
        addTargetWordLines(gameModel, assetsManager)
    }

    fun revealWordOnGameOver(
        gameModel: GameModel,
        globalHandlers: GlobalHandlers,
        gamePlayScreen: GamePlayScreen,
        revealWordWithDelay: Boolean
    ) {
        gameModel.currentTargetData.hiddenLettersIndices.clear()
        fadeOutAndClearTarget()
        globalHandlers.stage.addAction(
            Actions.delay(
                if (revealWordWithDelay) REVEAL_WORD_ON_GAME_OVER_DELAY else 0F,
                Actions.run {
                    if (wordRevealFree) {
                        addTargetWordLines(gameModel, assetsManager)
                    } else {
                        displayRevealWordDialog(globalHandlers, gamePlayScreen)
                    }
                    wordRevealFree = !wordRevealFree
                })
        )
    }

    private fun fadeOutAndClearTarget() {
        targetWordLines.forEach {
            it.addAction(
                Actions.sequence(
                    Actions.delay(2F),
                    Actions.fadeOut(1F),
                    Actions.run {
                        it.clear()
                        targetWordLines.remove(it)
                    })
            )
        }
    }

    private fun displayRevealWordDialog(
        globalHandlers: GlobalHandlers,
        gamePlayScreen: GamePlayScreen
    ) {
        globalHandlers.dialogsHandler.openDialog(
            header = REVEAL_WORD_DIALOG_HEADER,
            description = REVEAL_WORD_DIALOG_DESCRIPTION.format(GameModel.DISPLAY_TARGET_COST),
            onYes = {
                gamePlayScreen.onClickedToRevealWordOnGameOver()
            },
            onNo = {
                gamePlayScreen.onGameOverAnimationDone()
            },
            dialogName = REVEAL_WORD_DIALOG_NAME,
        )
    }

    fun findCellByIndex(index: Int, gameModel: GameModel): Cell<Actor> {
        var wordCount = 0
        var letterIndexInWord = 0
        for (i in 0 until index) {
            if (gameModel.currentTargetData.currentPhrase[i] == ' ') {
                wordCount++
                letterIndexInWord = 0
            } else {
                letterIndexInWord++
            }
        }
        val wordTable = wordsTables[wordCount]
        return wordTable.cells[letterIndexInWord]
    }


    companion object {
        private const val TARGET_WORD_TABLE_VERTICAL_PADDING = 80F
        private const val TARGET_WORD_LINE_VERTICAL_PADDING = 15F
        private const val GAME_WIN_ANIMATION_DISTANCE = 50F
        private const val GAME_WIN_ANIMATION_LETTER_MOVE_DURATION = 0.25F
        private const val LETTER_PADDING = 5F
        private const val WIN_DELAY = 2F
        private const val REVEAL_WORD_ON_GAME_OVER_DELAY = 4F
        private val auxVector = Vector2()
        private const val REVEAL_WORD_DIALOG_HEADER = "רוצה לדעת את המילה?"
        private const val REVEAL_WORD_DIALOG_DESCRIPTION = "ניתן להציג את המילה עבור %s מטבעות"
        private const val REVEAL_WORD_DIALOG_NAME = "help"
    }
}
