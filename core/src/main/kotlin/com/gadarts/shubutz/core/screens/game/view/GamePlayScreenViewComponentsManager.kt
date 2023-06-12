package com.gadarts.shubutz.core.screens.game.view

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Disposable
import com.gadarts.shubutz.core.ShubutzGame
import com.gadarts.shubutz.core.model.Difficulties
import com.gadarts.shubutz.core.model.GameModel
import com.gadarts.shubutz.core.model.assets.GameAssetManager
import com.gadarts.shubutz.core.model.assets.definitions.FontsDefinitions
import com.gadarts.shubutz.core.model.assets.definitions.SoundsDefinitions
import com.gadarts.shubutz.core.model.assets.definitions.TexturesDefinitions.*
import com.gadarts.shubutz.core.screens.game.GamePlayScreen
import com.gadarts.shubutz.core.screens.game.GlobalHandlers
import com.gadarts.shubutz.core.screens.menu.view.stage.GameStage
import ktx.actors.alpha

class GamePlayScreenViewComponentsManager(
    private val globalHandlers: GlobalHandlers,
    private val gamePlayScreen: GamePlayScreen,
    private val stage: GameStage,
    gameModel: GameModel
) : Disposable {

    private lateinit var scoreView: ScoreView
    private lateinit var revealLetterButton: ImageTextButton
    lateinit var targetPhraseView: TargetPhraseView
    lateinit var optionsView: OptionsView
    val bombView = BombView(globalHandlers)
    private val topBarView = TopBarView(globalHandlers, gamePlayScreen, gameModel)


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
        addRevealLetterButton(am, stage, gameModel.selectedDifficulty)
        addScoreView()
    }

    private fun addScoreView() {
        scoreView = ScoreView(
            globalHandlers.assetsManager.getTexture(SCORE),
            globalHandlers.assetsManager.getFont(FontsDefinitions.VARELA_80)
        )
        scoreView.setPosition(SCORE_VIEW_POSITION_X, SCORE_VIEW_POSITION_Y)
        stage.addActor(scoreView)
    }

    private fun addRevealLetterButton(
        assetsManager: GameAssetManager,
        stage: GameStage,
        selectedDifficulty: Difficulties,
    ) {
        val font = assetsManager.getFont(FontsDefinitions.VARELA_40)
        val up = assetsManager.getTexture(BUTTON_CIRCLE_UP)
        revealLetterButton = createRevealButton(up, assetsManager, font)
        insertContentInRevealButton(revealLetterButton, up, assetsManager, font, selectedDifficulty)
        revealLetterButton.setPosition(REVEAL_BUTTON_POSITION_X, REVEAL_BUTTON_POSITION_Y)
        stage.addActor(revealLetterButton)
        revealLetterButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                if (!revealLetterButton.isVisible || revealLetterButton.alpha < 1F) return

                globalHandlers.soundPlayer.playSound(assetsManager.getSound(SoundsDefinitions.HELP))
                val result = gamePlayScreen.onRevealLetterButtonClicked()
                if (result) {
                    hideRevealLetterButton()
                }
            }
        })
    }

    private fun createRevealButton(
        up: Texture,
        assetsManager: GameAssetManager,
        font: BitmapFont
    ): ImageTextButton {
        val style = ImageTextButton.ImageTextButtonStyle(
            TextureRegionDrawable(up),
            TextureRegionDrawable(assetsManager.getTexture(BUTTON_CIRCLE_DOWN)),
            null,
            font
        )
        val imageTextButton = ImageTextButton(REVEAL_BUTTON_LABEL, style)
        imageTextButton.isVisible = false
        imageTextButton.alpha = 0F
        return imageTextButton
    }

    private fun insertContentInRevealButton(
        revealLetterButton: ImageTextButton,
        up: Texture,
        assetsManager: GameAssetManager,
        font: BitmapFont,
        selectedDifficulty: Difficulties
    ) {
        revealLetterButton.clearChildren()
        revealLetterButton.removeActor(revealLetterButton.image)
        revealLetterButton.removeActor(revealLetterButton.label)
        revealLetterButton.add(revealLetterButton.label).row()
        revealLetterButton.setSize(up.width.toFloat(), up.height.toFloat())
        val eye = assetsManager.getTexture(ICON_EYE)
        revealLetterButton.add(Image(eye)).size(eye.width.toFloat(), eye.height.toFloat()).row()
        val stack = Stack()
        val coin =
            assetsManager.getTexture(if (selectedDifficulty != Difficulties.KIDS) COIN else CANDY)
        val labelStyle = LabelStyle(font, Color.WHITE)
        stack.add(Image(coin))
        val cost = Label(selectedDifficulty.revealLetterCost.toString(), labelStyle)
        cost.setAlignment(Align.center)
        stack.add(cost)
        revealLetterButton.add(stack).size(coin.width.toFloat(), coin.height.toFloat())
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
        topBarView.setCategoryLabelText(gameModel.currentCategory)
        hideRevealLetterButton()
    }

    private fun hideRevealLetterButton() {
        if (revealLetterButton.isVisible) {
            revealLetterButton.addAction(
                Actions.sequence(
                    Actions.fadeOut(0.5F),
                    Actions.visible(false)
                )
            )
        }
    }

    fun onRoundWin(
        stage: GameStage,
        gameModel: GameModel,
        actionOnGameWinAnimationFinish: Runnable
    ) {
        bombView.stopFire()
        targetPhraseView.applyGameWinAnimation(
            globalHandlers.assetsManager,
            stage,
            actionOnGameWinAnimationFinish
        )
        applyCoinsFlyingFromBomb(gameModel, stage)
        hideRevealLetterButton()
    }

    private fun applyCoinsFlyingFromBomb(
        gameModel: GameModel,
        stage: GameStage
    ) {
        val coinTexture =
            globalHandlers.assetsManager.getTexture(if (gameModel.selectedDifficulty != Difficulties.KIDS) COIN else CANDY)
        val startPosition = bombView.bombComponent.localToStageCoordinates(Vector2())
        startPosition.x += bombView.bombComponent.width / 2F - coinTexture.width / 2F
        startPosition.y += bombView.bombComponent.width / 2F - coinTexture.width / 2F
        val targetPosition = topBarView.getCoinsIcon().localToStageCoordinates(Vector2())
        applyFlyingCoinsAnimation(
            stage,
            coinTexture,
            startPosition,
            gameModel.selectedDifficulty.winWorth,
            targetPosition
        )
    }

    private fun applyFlyingCoinsAnimation(
        stage: GameStage,
        coinTexture: Texture,
        startPosition: Vector2,
        numberOfCoins: Int,
        targetPosition: Vector2
    ) {
        for (i in 0 until numberOfCoins) {
            addFlyingCoin(coinTexture, stage, startPosition, i, targetPosition)
        }
    }

    private fun addFlyingCoin(
        coinTexture: Texture,
        stage: GameStage,
        startPosition: Vector2,
        i: Int,
        targetPosition: Vector2
    ) {
        val coin = Image(coinTexture)
        stage.addActor(coin)
        coin.setPosition(startPosition.x, startPosition.y)

        coin.addAction(
            Actions.sequence(
                Actions.delay(i.toFloat() * 0.25F),
                Actions.alpha(0F),
                Actions.sizeTo(0F, 0F),
                Actions.parallel(
                    Actions.alpha(1F, 0.5F),
                    Actions.sequence(
                        Actions.sizeTo(
                            coinTexture.width.toFloat(),
                            coinTexture.height.toFloat(),
                            0.25F
                        ),
                        Actions.delay(0.25F),
                        Actions.alpha(0F, 1F),
                    ),
                    Actions.moveTo(
                        targetPosition.x,
                        targetPosition.y,
                        1F,
                        Interpolation.smooth2
                    )
                ),
                Actions.removeActor()
            )
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
        if (gameModel.helpAvailable && (gameModel.selectedDifficulty.tries - gameModel.triesLeft > 1) && !revealLetterButton.isVisible) {
            revealLetterButton.addAction(
                Actions.sequence(
                    Actions.visible(true),
                    Actions.fadeOut(0F),
                    Actions.fadeIn(0.5F)
                )
            )
        }
    }

    fun onGameOver(stage: GameStage) {
        bombView.onGameOverAnimation(globalHandlers.assetsManager, stage)
        optionsView.clearAllOptions()
        hideRevealLetterButton()
    }

    override fun dispose() {
        topBarView.dispose()
    }

    fun clear() {
        topBarView.clear()
        bombView.clear()
        optionsView.clear()
        revealLetterButton.remove()
        scoreView.remove()
    }

    fun onCorrectGuess(
        coinsAmount: Int,
        perfectBonusAchieved: Boolean,
        gameWin: Boolean,
        gameModel: GameModel,
        prevScore: Long
    ) {
        topBarView.onCorrectGuess(coinsAmount)
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

    fun onLetterRevealed(letter: Char, cost: Int) {
        optionsView.onLetterRevealed(letter)
        topBarView.onLetterRevealed(cost)
    }

    fun onLetterRevealFailedNotEnoughCoins() {
        globalHandlers.dialogsHandler.openBuyCoinsDialog(stage, gamePlayScreen)
    }

    fun onPurchasedCoins(amount: Int) {
        topBarView.onPurchasedCoins(amount)
        globalHandlers.dialogsHandler.openCoinsPurchasedSuccessfully(
            globalHandlers.assetsManager,
            stage,
            amount
        )
    }

    fun onRewardForVideoAd(rewardAmount: Int) {
        topBarView.onRewardForVideoAd(rewardAmount)
    }

    fun onGameWin() {
        topBarView.onGameWin()
    }

    fun onPhysicalBackClicked() {
        if (stage.openDialogs.isEmpty()) {
            globalHandlers.dialogsHandler.openExitDialog(
                stage,
                globalHandlers.assetsManager,
                gamePlayScreen
            )
        } else {
            stage.closeAllDialogs()
        }
    }

    fun onChampion(post: () -> Unit) {
        ShubutzGame.lastChampionsFetch = 0L
        val texture = globalHandlers.assetsManager.getTexture(CHAMPION)
        displayBannerWithEffect(
            SoundsDefinitions.CHAMPION,
            createChampionAnimation(texture, post),
            texture
        )
    }

    private fun createChampionAnimation(texture: Texture, post: () -> Unit): SequenceAction {
        val centerX = stage.width / 2F - texture.width / 2F
        return Actions.sequence(
            Actions.parallel(
                Actions.sequence(
                    Actions.moveTo(centerX, 0F),
                    Actions.moveTo(
                        centerX,
                        stage.height / 2F,
                        2F,
                        Interpolation.swingIn
                    ),
                    Actions.delay(2F),
                    Actions.parallel(
                        Actions.sizeTo(0F, 0F, 0.5F, Interpolation.swingOut),
                        Actions.moveBy(
                            texture.width / 2F,
                            texture.height / 2F,
                            0.5F,
                            Interpolation.swingOut
                        )
                    ),
                    Actions.run { post.invoke() },
                    Actions.removeActor()
                ),
                Actions.repeat(
                    5,
                    Actions.sequence(
                        Actions.fadeOut(0.5F, Interpolation.smoother),
                        Actions.fadeIn(0.5F, Interpolation.smoother),
                    )
                )
            )
        )
    }

    companion object {
        const val REVEAL_BUTTON_POSITION_X = 800F
        const val REVEAL_BUTTON_POSITION_Y = 1112F
        const val SCORE_VIEW_POSITION_X = 850F
        const val SCORE_VIEW_POSITION_Y = 1700F
        val REVEAL_BUTTON_LABEL = "גלה אות".reversed()
    }
}
