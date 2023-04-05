package com.gadarts.shubutz.core.screens.game.view

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Disposable
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
    effectsHandler: EffectsHandler
) : Disposable {

    private lateinit var revealLetterButton: ImageTextButton
    private var dialogsManager = DialogsManager(globalHandlers, effectsHandler, stage)
    lateinit var targetPhraseView: TargetPhraseView
    lateinit var optionsView: OptionsView
    val bombView = BombView(globalHandlers)
    val topBarView = TopBarView(globalHandlers, gamePlayScreen)


    fun createViews(
        letterSize: Vector2,
        am: GameAssetManager,
        stage: GameStage,
        gameModel: GameModel,
        gamePlayScreen: GamePlayScreen,
    ) {
        topBarView.addTopBar(am, gameModel, gamePlayScreen, stage, dialogsManager)
        val font80 = am.getFont(FontsDefinitions.VARELA_80)
        targetPhraseView =
            TargetPhraseView(letterSize, font80, globalHandlers.soundPlayer, am)
        targetPhraseView.calculateMaxBricksPerLine(am)
        optionsView = OptionsView(stage, globalHandlers.soundPlayer, am, gameModel)
        addRevealLetterButton(am, stage)
    }

    private fun addRevealLetterButton(
        assetsManager: GameAssetManager,
        stage: GameStage,
    ) {
        val font = assetsManager.getFont(FontsDefinitions.VARELA_40)
        val up = assetsManager.getTexture(BUTTON_CIRCLE_UP)
        revealLetterButton = createRevealButton(up, assetsManager, font)
        insertContentInRevealButton(revealLetterButton, up, assetsManager, font)
        revealLetterButton.setPosition(REVEAL_BUTTON_POSITION_X, REVEAL_BUTTON_POSITION_Y)
        stage.addActor(revealLetterButton)
        revealLetterButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                if (!revealLetterButton.isVisible || revealLetterButton.alpha < 1F) return

                globalHandlers.soundPlayer.playSound(assetsManager.getSound(SoundsDefinitions.HELP))
                gamePlayScreen.onRevealLetterButtonClicked()
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
        font: BitmapFont
    ) {
        revealLetterButton.clearChildren()
        revealLetterButton.removeActor(revealLetterButton.image)
        revealLetterButton.removeActor(revealLetterButton.label)
        revealLetterButton.add(revealLetterButton.label).row()
        revealLetterButton.setSize(up.width.toFloat(), up.height.toFloat())
        val eye = assetsManager.getTexture(ICON_EYE)
        revealLetterButton.add(Image(eye)).size(eye.width.toFloat(), eye.height.toFloat()).row()
        val stack = Stack()
        val coin = assetsManager.getTexture(COIN)
        val labelStyle = LabelStyle(font, Color.WHITE)
        stack.add(Image(coin))
        val cost = Label("8", labelStyle)
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
        val coinTexture = globalHandlers.assetsManager.getTexture(COIN)
        val startPosition = bombView.bombComponent.localToStageCoordinates(Vector2())
        startPosition.x += bombView.bombComponent.width / 2F - coinTexture.width / 2F
        startPosition.y += bombView.bombComponent.width / 2F - coinTexture.width / 2F
        val targetPosition = topBarView.coinsIcon.localToStageCoordinates(Vector2())
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
        if ((gameModel.selectedDifficulty.tries - gameModel.triesLeft > 1) && !revealLetterButton.isVisible) {
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
    }

    fun onCorrectGuess(coinsAmount: Int) {
        topBarView.onCorrectGuess(coinsAmount)
    }

    fun onLetterRevealed(letter: Char, gameModel: GameModel, cost: Int) {
        optionsView.onLetterRevealed(letter)
        topBarView.onLetterRevealed(gameModel, cost)
    }

    fun onLetterRevealFailedNotEnoughCoins() {
        dialogsManager.openBuyCoinsDialog(stage, gamePlayScreen)
    }

    fun onPurchasedCoins(gameModel: GameModel, amount: Int) {
        topBarView.coinsLabel.setText(gameModel.coins)
        dialogsManager.openCoinsPurchasedSuccessfully(
            globalHandlers.assetsManager,
            stage,
            amount
        )
    }

    fun onRewardForVideoAd(rewardAmount: Int, gameModel: GameModel) {
        topBarView.onRewardForVideoAd(rewardAmount, gameModel)
    }

    companion object {
        const val REVEAL_BUTTON_POSITION_X = 800F
        const val REVEAL_BUTTON_POSITION_Y = 1112F
        val REVEAL_BUTTON_LABEL = "גלה אות".reversed()
    }
}
