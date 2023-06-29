package com.gadarts.shubutz.core.screens.game.view

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.math.Interpolation.circle
import com.badlogic.gdx.math.Interpolation.smoother
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Cell
import com.badlogic.gdx.scenes.scene2d.ui.Stack
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Disposable
import com.gadarts.shubutz.core.DebugSettings
import com.gadarts.shubutz.core.model.GameModel
import com.gadarts.shubutz.core.model.assets.definitions.FontsDefinitions
import com.gadarts.shubutz.core.model.assets.definitions.SoundsDefinitions
import com.gadarts.shubutz.core.model.assets.definitions.TexturesDefinitions
import com.gadarts.shubutz.core.screens.game.GamePlayScreen
import com.gadarts.shubutz.core.screens.game.GlobalHandlers
import com.gadarts.shubutz.core.screens.game.view.actors.Brick
import com.gadarts.shubutz.core.screens.menu.view.stage.GameStage


class GamePlayScreenView(
    private val globalHandlers: GlobalHandlers,
    private val gameModel: GameModel,
    private val gamePlayScreen: GamePlayScreen,
    private val stage: GameStage,
) :
    Disposable {

    private val gamePlayScreenViewComponentsManager =
        GamePlayScreenViewComponentsManager(
            globalHandlers,
            gamePlayScreen,
            stage,
            gameModel,
        )
    private lateinit var uiTable: Table
    private var font80: BitmapFont =
        globalHandlers.assetsManager.getFont(FontsDefinitions.VARELA_80)
    private lateinit var letterSize: Vector2
    fun onShow() {
        val letterGlyphLayout = GlyphLayout(font80, "א")
        letterSize = Vector2(letterGlyphLayout.width, letterGlyphLayout.height)
        createInterface()
        initializeForGameBegin()
    }

    private fun createInterface() {
        addUiTable()
        gamePlayScreenViewComponentsManager.createViews(
            letterSize,
            globalHandlers.assetsManager,
            stage,
            gameModel,
            gamePlayScreen,
        )
    }


    fun initializeForGameBegin() {
        stage.root.clearActions()
        gamePlayScreenViewComponentsManager.init(
            uiTable,
            gameModel,
            letterSize,
            gamePlayScreen,
            stage
        )
        uiTable.pack()
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
        gamePlayScreenViewComponentsManager.dispose()
    }

    fun clear() {
        uiTable.remove()
        gamePlayScreenViewComponentsManager.clear()
        stage.root.clearActions()
    }

    fun onCorrectGuess(
        indices: List<Int>,
        gameWin: Boolean,
        coinsAmount: Int,
        perfectBonusAchieved: Boolean,
        prevScore: Long
    ) {
        gamePlayScreenViewComponentsManager.onCorrectGuess(
            coinsAmount,
            perfectBonusAchieved,
            gameWin,
            gameModel,
            prevScore
        )
        globalHandlers.soundPlayer.playSound(globalHandlers.assetsManager.getSound(SoundsDefinitions.CORRECT))
        if (gamePlayScreenViewComponentsManager.optionsView.selectedBrick != null) {
            val brickTexture = globalHandlers.assetsManager.getTexture(TexturesDefinitions.BRICK)
            indices.forEach {
                animateBrickSuccess(it, gameWin, brickTexture)
            }
            gamePlayScreenViewComponentsManager.optionsView.onCorrectGuess()
        }
    }


    private fun animateBrickSuccess(index: Int, gameWin: Boolean, brickTexture: Texture) {
        val cell =
            gamePlayScreenViewComponentsManager.targetPhraseView.findCellByIndex(index, gameModel)
        val selectedBrickScreenCoords =
            gamePlayScreenViewComponentsManager.optionsView.selectedBrick!!.localToStageCoordinates(
                auxVector.setZero()
            )
        val brick =
            Brick(gameModel.currentTargetData.currentPhrase[index].toString(), brickTexture, letterSize, font80)
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
            Actions.run { ((cell as Cell<*>).actor as Stack).add(brick) }
        )
        val actions = Actions.parallel(sequence, Actions.fadeOut(1F, smoother))
        if (gameWin) {
            sequence.addAction(Actions.run { roundWin(stage) })
            gamePlayScreenViewComponentsManager.onGameWin()
        }
        brick.addAction(actions)
    }

    private fun switchBrickToStage(brick: Brick) {
        brick.remove()
        stage.addActor(brick)
    }

    private fun roundWin(stage: GameStage) {
        gamePlayScreenViewComponentsManager.onRoundWin(
            stage,
            gameModel
        ) { clearScreen() }
    }

    private fun clearScreen() {
        gamePlayScreenViewComponentsManager.clearBombView()
        stage.addAction(Actions.delay(2F, Actions.run {
            uiTable.clear()
            gamePlayScreen.onScreenEmpty()
        }))
    }


    fun onIncorrectGuess(gameOver: Boolean) {
        globalHandlers.soundPlayer.playSound(globalHandlers.assetsManager.getSound(SoundsDefinitions.INCORRECT))
        gamePlayScreenViewComponentsManager.bombView.updateLabel(gameModel)
        if (gameOver) {
            gameOver()
        } else if (gamePlayScreenViewComponentsManager.optionsView.selectedBrick != null) {
            gamePlayScreenViewComponentsManager.onIncorrectGuess(gameModel)
        }
    }

    private fun gameOver() {
        displayTargetAndFinishGame()
    }

    private fun displayTargetAndFinishGame() {
        if (GameModel.wordRevealFree) {
            stage.addAction(
                Actions.delay(
                    GAME_OVER_DURATION,
                    Actions.run { gamePlayScreen.onGameOverAnimationDone() })
            )
        }
        gamePlayScreenViewComponentsManager.gameOver(stage, gameModel)
    }

    fun onPurchasedCoins(amount: Int) {
        gamePlayScreenViewComponentsManager.onPurchasedCoins(amount)
        globalHandlers.effectsHandler.applyPartyEffect(
            globalHandlers.assetsManager,
            globalHandlers.soundPlayer,
            stage
        )
    }


    fun displayFailedPurchase(message: String) {
        val dialogView = Table()
        dialogView.add(ViewUtils.createDialogLabel(
            message,
            globalHandlers.assetsManager,
            globalHandlers.androidInterface
        ))
        stage.addDialog(dialogView, "purchase_failed_dialog", globalHandlers.assetsManager)
    }

    fun onLetterRevealed(letter: Char, cost: Int) {
        gamePlayScreenViewComponentsManager.onLetterRevealed(letter, cost)
    }

    fun onLetterRevealFailedNotEnoughCoins() {
        gamePlayScreenViewComponentsManager.onLetterRevealFailedNotEnoughCoins()
    }

    fun onRewardForVideoAd(rewardAmount: Int) {
        gamePlayScreenViewComponentsManager.onRewardForVideoAd(rewardAmount)
    }

    fun onPhysicalBackClicked() {
        gamePlayScreenViewComponentsManager.onPhysicalBackClicked()
    }

    fun onChampion(post: () -> Unit) {
        gamePlayScreenViewComponentsManager.onChampion(post)
    }

    fun onRevealedWordOnGameOver(cost: Int) {
        globalHandlers.stage.closeAllDialogs()
        gamePlayScreenViewComponentsManager.displayCoinsConsumed(cost)
        displayTargetAndFinishGame()
    }

    fun onFailedToRevealWordOnGameOver() {
        globalHandlers.dialogsHandler.openBuyCoinsDialog(gamePlayScreen)
    }

    companion object {
        private const val BRICK_SUCCESS_ANIMATION_DURATION = 1F
        private const val GAME_OVER_DURATION = 8F
        private val auxVector = Vector2()
    }
}