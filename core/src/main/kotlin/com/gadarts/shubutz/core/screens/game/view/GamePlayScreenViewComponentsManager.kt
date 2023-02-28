package com.gadarts.shubutz.core.screens.game.view

import com.badlogic.gdx.Gdx
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
import com.gadarts.shubutz.core.SoundPlayer
import com.gadarts.shubutz.core.model.GameModel
import com.gadarts.shubutz.core.model.assets.GameAssetManager
import com.gadarts.shubutz.core.model.assets.definitions.FontsDefinitions
import com.gadarts.shubutz.core.model.assets.definitions.TexturesDefinitions.*
import com.gadarts.shubutz.core.screens.game.GamePlayScreen
import com.gadarts.shubutz.core.screens.menu.view.stage.GameStage

/**
 * Handles display-related logic of the game-play components.
 */
class GamePlayScreenViewComponentsManager(
    private val assetsManager: GameAssetManager,
    private val soundPlayer: SoundPlayer,
    gamePlayScreen: GamePlayScreen
) : Disposable {

    /**
     * The view of the phrase the player needs to discover.
     */
    lateinit var targetPhraseView: TargetPhraseView

    /**
     * The view of all the alphabet letters.
     */
    lateinit var optionsView: OptionsView

    /**
     * The view of the bomb with the counter.
     */
    val bombView = BombView(soundPlayer, assetsManager)

    /**
     * The view of the bar on top of the screen.
     */
    val topBarView = TopBarView(soundPlayer, assetsManager, gamePlayScreen)


    /**
     * Creates the views instances of the components.
     */
    fun createViews(
        letterSize: Vector2,
        assetsManager: GameAssetManager,
        stage: GameStage,
        gameModel: GameModel,
        gamePlayScreen: GamePlayScreen,
    ) {
        topBarView.addTopBar(assetsManager, gameModel, gamePlayScreen, stage)
        val font80 = assetsManager.getFont(FontsDefinitions.VARELA_80)
        targetPhraseView = TargetPhraseView(letterSize, font80, soundPlayer, assetsManager)
        targetPhraseView.calculateMaxBricksPerLine(assetsManager)
        optionsView = OptionsView(stage, soundPlayer, assetsManager, gameModel)
        addRevealLetterButton(assetsManager, stage)
    }

    private fun addRevealLetterButton(
        assetsManager: GameAssetManager,
        stage: GameStage
    ) {
        val font = assetsManager.getFont(FontsDefinitions.VARELA_40)
        val up = assetsManager.getTexture(BUTTON_CIRCLE_UP)
        val revealLetterButton = createRevealButton(up, assetsManager, font)
        insertContentInRevealButton(revealLetterButton, up, assetsManager, font)
        revealLetterButton.setPosition(REVEAL_BUTTON_POSITION_X, REVEAL_BUTTON_POSITION_Y)
        stage.addActor(revealLetterButton)
        revealLetterButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                Gdx.app.log("!!", "!!")
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
        return ImageTextButton(REVEAL_BUTTON_LABEL, style)
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

    /**
     * Initializes the components.
     */
    fun init(
        uiTable: Table,
        gameModel: GameModel,
        letterSize: Vector2,
        gamePlayScreen: GamePlayScreen,
        stage: GameStage,
    ) {
        bombView.addBomb(assetsManager, stage, uiTable, gameModel)
        targetPhraseView.onGameBegin(gameModel, assetsManager, uiTable)
        optionsView.addLettersOptionsTable(
            uiTable,
            assetsManager,
            targetPhraseView.maxBricksPerLine,
            letterSize,
            gamePlayScreen,
        )
        topBarView.setCategoryLabelText(gameModel.currentCategory)
    }

    /**
     * Stops the bomb's fire, plays the winning animation of the targets phrase view and plays
     * the coins flying animation.
     */
    fun applyGameWinAnimation(
        stage: GameStage,
        gameModel: GameModel,
        actionOnGameWinAnimationFinish: Runnable
    ) {
        bombView.stopFire()
        targetPhraseView.applyGameWinAnimation(assetsManager, stage, actionOnGameWinAnimationFinish)
        applyCoinsEffect(gameModel, stage)
    }

    private fun applyCoinsEffect(
        gameModel: GameModel,
        stage: GameStage
    ) {
        val coinTexture = assetsManager.getTexture(COIN)
        val startPosition = bombView.bombComponent.localToScreenCoordinates(Vector2())
        startPosition.x += bombView.bombComponent.width / 2F - coinTexture.width / 2F
        startPosition.y = stage.height - startPosition.y
        val targetPosition = topBarView.coinsIcon.localToScreenCoordinates(Vector2())
        targetPosition.y = stage.height - targetPosition.y
        for (i in 0 until gameModel.selectedDifficulty.winWorth) {
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

    /**
     * Plays the animation for the incorrect guess event.
     */
    fun applyIncorrectGuessAnimations() {
        bombView.onIncorrectGuess()
        optionsView.onIncorrectGuess()
    }

    /**
     * Plays the animation for the game over event and clears all options.
     */
    fun applyGameOverAnimation(stage: GameStage) {
        bombView.onGameOverAnimation(assetsManager, stage)
        optionsView.clearAllOptions()
    }

    override fun dispose() {
        topBarView.dispose()
    }

    /**
     * Removes the UI of the top-bar and the bomb.
     */
    fun clearTopBarAndBomb() {
        topBarView.clear()
        bombView.clear()
    }

    /**
     * Plays the animation for the correct guess event.
     */
    fun applyCorrectGuessAnimation(coinsAmount: Int) {
        topBarView.applyWinCoinEffect(coinsAmount, assetsManager)
    }

    companion object {
        const val REVEAL_BUTTON_POSITION_X = 800F
        const val REVEAL_BUTTON_POSITION_Y = 1112F
        val REVEAL_BUTTON_LABEL = "גלה אות".reversed()
    }
}
