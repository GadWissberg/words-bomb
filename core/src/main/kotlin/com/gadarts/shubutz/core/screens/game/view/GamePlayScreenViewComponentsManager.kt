package com.gadarts.shubutz.core.screens.game.view

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Disposable
import com.gadarts.shubutz.core.model.GameModel
import com.gadarts.shubutz.core.model.assets.GameAssetManager
import com.gadarts.shubutz.core.model.assets.definitions.FontsDefinitions
import com.gadarts.shubutz.core.model.assets.definitions.SoundsDefinitions
import com.gadarts.shubutz.core.model.assets.definitions.TexturesDefinitions.PERFECT
import com.gadarts.shubutz.core.model.assets.definitions.TexturesDefinitions.SCORE
import com.gadarts.shubutz.core.screens.game.GamePlayScreen
import com.gadarts.shubutz.core.screens.game.GlobalHandlers
import com.gadarts.shubutz.core.screens.menu.view.stage.GameStage

class GamePlayScreenViewComponentsManager(
    private val globalHandlers: GlobalHandlers,
    private val gamePlayScreen: GamePlayScreen,
    private val stage: GameStage,
) : Disposable {

    private lateinit var scoreView: ScoreView
    lateinit var targetPhraseView: TargetPhraseView
    lateinit var optionsView: OptionsView
    val bombView = BombView(globalHandlers)
    private val topBarView = TopBarView(globalHandlers)


    fun createViews(
        letterSize: Vector2,
        am: GameAssetManager,
        stage: GameStage,
        gameModel: GameModel,
        gamePlayScreen: GamePlayScreen,
    ) {
        topBarView.addTopBar(am, gameModel, gamePlayScreen, stage, globalHandlers.dialogsHandler)
        val font80 = am.getFont(FontsDefinitions.VARELA_80)
        targetPhraseView =
            TargetPhraseView(letterSize, font80, globalHandlers.soundPlayer, am)
        targetPhraseView.calculateMaxBricksPerLine(am)
        optionsView = OptionsView(stage, globalHandlers.soundPlayer, am, gameModel)
        addScoreView()
    }

    private fun addScoreView() {
        scoreView = ScoreView(
            globalHandlers.assetsManager.getTexture(SCORE),
            globalHandlers.assetsManager.getFont(FontsDefinitions.VARELA_80),
            globalHandlers.androidInterface
        )
        scoreView.setPosition(SCORE_VIEW_POSITION_X, SCORE_VIEW_POSITION_Y)
        stage.addActor(scoreView)
    }

    fun init(
        uiTable: Table,
        gameModel: GameModel,
        letterSize: Vector2,
        gamePlayScreen: GamePlayScreen,
        stage: GameStage,
    ) {
        bombView.addBomb(globalHandlers.assetsManager, stage, uiTable, gameModel)
        targetPhraseView.onGameBegin(gameModel, globalHandlers.assetsManager, uiTable)
        optionsView.addLettersOptionsTable(
            uiTable,
            globalHandlers.assetsManager,
            targetPhraseView.maxBricksPerLine,
            letterSize,
            gamePlayScreen,
        )
        topBarView.setCategoryLabelText(gameModel.currentTargetData.currentCategory)
    }

    fun onRoundWin(
        stage: GameStage,
        actionOnGameWinAnimationFinish: Runnable
    ) {
        bombView.stopFire()
        targetPhraseView.applyGameWinAnimation(
            globalHandlers.assetsManager,
            stage,
            actionOnGameWinAnimationFinish
        )
    }

    /**
     * Plays the bomb disappear animation and clears the options and target in the end of it.
     */
    fun clearBombView() {
        bombView.animateBombVanish {
            optionsView.onScreenClear()
            targetPhraseView.onScreenClear()
        }
    }

    fun onIncorrectGuess(gameModel: GameModel) {
        bombView.onIncorrectGuess(gameModel)
        optionsView.onIncorrectGuess()
    }

    fun gameOver(stage: GameStage, gameModel: GameModel) {
        bombView.onGameOverAnimation(globalHandlers.assetsManager, stage)
        optionsView.clearAllOptions()
        targetPhraseView.revealWordOnGameOver(
            gameModel,
            globalHandlers,
            gamePlayScreen,
            bombView.bombComponent.hasParent()
        )
    }

    override fun dispose() {
        topBarView.dispose()
    }

    fun clear() {
        topBarView.clear()
        bombView.clear()
        optionsView.clear()
        scoreView.remove()
    }

    fun onCorrectGuess(
        perfectBonusAchieved: Boolean,
        gameWin: Boolean,
        gameModel: GameModel,
        prevScore: Long
    ) {
        if (perfectBonusAchieved) {
            displayPerfect()
        }
        if (gameWin) {
            scoreView.onGameWin(gameModel.score, prevScore)
        }
    }

    private fun displayPerfect() {
        val texture = globalHandlers.assetsManager.getTexture(PERFECT)
        displayBannerWithEffect(
            SoundsDefinitions.PERFECT,
            createPerfectAnimation(texture),
            texture
        )
    }

    private fun createPerfectAnimation(texture: Texture): SequenceAction =
        Actions.sequence(
            Actions.moveTo(
                stage.width / 2F - texture.width / 2F,
                stage.height / 2F,
                1F,
                Interpolation.bounce
            ),
            Actions.delay(1F),
            Actions.rotateBy(35F, 0.2F, Interpolation.swingIn),
            Actions.rotateBy(-70F, 0.2F, Interpolation.swingIn),
            Actions.rotateBy(35F, 0.2F, Interpolation.swingIn),
            Actions.delay(0.5F),
            Actions.parallel(
                Actions.sizeTo(0F, 0F, 0.5F, Interpolation.swingOut),
                Actions.moveBy(
                    texture.width / 2F,
                    texture.height / 2F,
                    0.5F,
                    Interpolation.swingOut
                )
            ),
            Actions.removeActor()
        )

    private fun displayBannerWithEffect(
        soundDefinition: SoundsDefinitions,
        sequenceAction: SequenceAction,
        texture: Texture
    ) {
        val image = Image(texture)
        image.setPosition(stage.width / 2F - texture.width / 2F, stage.height)
        image.setOrigin(Align.center)
        val sound = globalHandlers.assetsManager.getSound(soundDefinition)
        globalHandlers.soundPlayer.playSound(sound)
        image.addAction(sequenceAction)
        stage.addActor(image)
    }

    fun onLetterRevealed(letter: Char) {
        optionsView.onLetterRevealed(letter)
    }

    fun onPhysicalBackClicked() {
        if (stage.openDialogs.isEmpty()) {
            globalHandlers.dialogsHandler.openExitDialog(gamePlayScreen)
        } else {
            stage.closeAllDialogs()
        }
    }

    companion object {
        const val SCORE_VIEW_POSITION_X = 850F
        const val SCORE_VIEW_POSITION_Y = 1600F
    }
}
