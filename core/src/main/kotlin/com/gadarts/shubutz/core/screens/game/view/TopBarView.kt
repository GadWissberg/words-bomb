package com.gadarts.shubutz.core.screens.game.view

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Disposable
import com.gadarts.shubutz.core.DebugSettings
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

class TopBarView(
    private val globalHandlers: GlobalHandlers,
    private val gamePlayScreen: GamePlayScreen,
    private val gameModel: GameModel
) : Table(), Disposable {
    private var letterGlyphLayout: GlyphLayout =
        GlyphLayout(globalHandlers.assetsManager.getFont(FontsDefinitions.VARELA_80), "א")
    private lateinit var categoryLabel: Label
    private lateinit var topPartTable: Table
    private lateinit var topPartTexture: Texture
    private lateinit var categoryBackgroundTexture: Texture
    private var coinsLabelHandler = CoinsLabelHandler()

    fun getCoinsIcon(): Image {
        return coinsLabelHandler.coinsIcon
    }

    override fun dispose() {
        topPartTexture.dispose()
        categoryBackgroundTexture.dispose()
    }

    fun setCategoryLabelText(currentCategory: String) {
        categoryLabel.setText(currentCategory.reversed())
        categoryLabel.toFront()
    }

    fun onCorrectGuess(coinsAmount: Int) {
        coinsLabelHandler.onCorrectGuess(coinsAmount)
    }

    fun addTopBar(
        assetsManager: GameAssetManager,
        gameModel: GameModel,
        gamePlayScreen: GamePlayScreen,
        stage: GameStage,
        dialogsManager: DialogsManager,
    ) {
        stage.addActor(this)
        addTopPart(stage, assetsManager, gamePlayScreen, gameModel, dialogsManager)
        addCategoryLabel(gameModel, assetsManager)
        setPosition(stage.width / 2F, stage.height - prefHeight / 2F)
        setDebug(DebugSettings.SHOW_UI_BORDERS, true)
    }

    override fun act(delta: Float) {
        super.act(delta)
        coinsLabelHandler.act(topPartTable, gameModel, globalHandlers)
    }


    fun onLetterRevealed(cost: Int) {
        coinsLabelHandler.onLetterRevealed(cost)
    }

    fun onRewardForVideoAd(rewardAmount: Int) {
        coinsLabelHandler.onRewardForVideoAd(rewardAmount)
    }

    fun onPurchasedCoins(amount: Int) {
        coinsLabelHandler.onPurchasedCoins(amount)
    }


    fun onGameWin() {
        coinsLabelHandler.onGameWin(globalHandlers, stage)
    }

    private fun addCategoryLabel(
        gameModel: GameModel,
        assetsManager: GameAssetManager
    ) {
        val labelStyle = LabelStyle(assetsManager.getFont(FontsDefinitions.VARELA_80), Color.WHITE)
        labelStyle.background = NinePatchDrawable(
            NinePatch(categoryBackgroundTexture, 10, 10, 10, 10)
        )
        categoryLabel = Label(
            gameModel.currentCategory,
            labelStyle
        )
        categoryLabel.setAlignment(Align.center)
        add(categoryLabel).size(
            ShubutzGame.RESOLUTION_WIDTH.toFloat(),
            letterGlyphLayout.height * 2
        )
    }

    private fun addTopPart(
        stage: GameStage,
        am: GameAssetManager,
        gamePlayScreen: GamePlayScreen,
        gameModel: GameModel,
        dialogs: DialogsManager,
    ) {
        topPartTexture = createTopPartTexture(stage, TOP_BAR_COLOR)
        categoryBackgroundTexture = createTopPartTexture(stage, CATEGORY_BACKGROUND_COLOR)
        topPartTable = Table()
        topPartTable.background = TextureRegionDrawable(topPartTexture)
        topPartTable.debug = DebugSettings.SHOW_UI_BORDERS
        topPartTable.setSize(ShubutzGame.RESOLUTION_WIDTH.toFloat(), TOP_PART_HEIGHT.toFloat())
        addTopPartComponents(topPartTable, am, gamePlayScreen, gameModel, dialogs)
        add(topPartTable).row()
    }

    private fun addExitButton(
        table: Table,
        assetsManager: GameAssetManager,
        gamePlayScreen: GamePlayScreen,
        dialogsManager: DialogsManager
    ) {
        val texture = assetsManager.getTexture(BACK_BUTTON)
        val button = ImageButton(TextureRegionDrawable(texture))
        button.pad(10F, 80F, 10F, 40F)
        addClickListenerToButton(
            button,
            assetsManager
        ) { dialogsManager.openExitDialog(stage as GameStage, assetsManager, gamePlayScreen) }
        table.add(button).left()
    }

    private fun addClickListenerToButton(
        button: Button,
        assetsManager: GameAssetManager,
        runnable: Runnable,
    ) {
        button.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                runnable.run()
                globalHandlers.soundPlayer.playSound(assetsManager.getSound(SoundsDefinitions.BUTTON))
            }
        })
    }


    private fun addTopPartComponents(
        table: Table,
        assetsManager: GameAssetManager,
        gamePlayScreen: GamePlayScreen,
        gameModel: GameModel,
        dialogsManager: DialogsManager,
    ) {
        val leftSideTable = Table()
        leftSideTable.debug = DebugSettings.SHOW_UI_BORDERS
        addExitButton(leftSideTable, assetsManager, gamePlayScreen, dialogsManager)
        addBuyCoinsButton(leftSideTable, dialogsManager)
        table.add(leftSideTable).expandX().left()
        val font80 = assetsManager.getFont(FontsDefinitions.VARELA_80)
        coinsLabelHandler.addCoinsLabel(gameModel, font80, table, assetsManager, topPartTexture)
    }

    private fun addBuyCoinsButton(
        table: Table,
        dialogsManager: DialogsManager,
    ) {
        val coinsButton = createBuyCoinsButton(table, dialogsManager)
        coinsButton.setOrigin(Align.center)
        coinsButton.isTransform = true
        table.add(coinsButton).pad(
            COINS_BUTTON_PAD_TOP,
            COINS_BUTTON_PAD_LEFT,
            COINS_BUTTON_PAD_BOTTOM,
            COINS_BUTTON_PAD_RIGHT
        )
        coinsButton.addAction(
            Actions.forever(
                Actions.sequence(
                    Actions.delay(20F),
                    Actions.sizeBy(
                        -COINS_BUTTON_ANIMATION_SIZE_BY, -COINS_BUTTON_ANIMATION_SIZE_BY, 2F,
                        Interpolation.slowFast
                    ),
                    Actions.sizeBy(
                        COINS_BUTTON_ANIMATION_SIZE_BY, COINS_BUTTON_ANIMATION_SIZE_BY, 0.5F,
                        Interpolation.pow5
                    ),
                ),
            )
        )
    }


    private fun createBuyCoinsButton(
        table: Table,
        dialogsManager: DialogsManager,
    ): ImageButton {
        val up =
            if (gameModel.selectedDifficulty != Difficulties.KIDS) COINS_BUTTON_UP else CANDY_BUTTON_UP
        val down =
            if (gameModel.selectedDifficulty != Difficulties.KIDS) COINS_BUTTON_DOWN else CANDY_BUTTON_DOWN
        val coinsButton = ImageButton(
            TextureRegionDrawable(globalHandlers.assetsManager.getTexture(up)),
            TextureRegionDrawable(globalHandlers.assetsManager.getTexture(down))
        )
        addClickListenerToButton(
            coinsButton,
            globalHandlers.assetsManager
        ) {
            dialogsManager.openBuyCoinsDialog(table.stage as GameStage, gamePlayScreen)
        }
        return coinsButton
    }

    private fun createTopPartTexture(stage: GameStage, backgroundColor: String): Texture {
        val pixmap = Pixmap(stage.width.toInt(), TOP_PART_HEIGHT, Pixmap.Format.RGBA8888)
        val color = Color.valueOf(backgroundColor)
        color.a /= 1.5F
        pixmap.setColor(color)
        pixmap.fill()
        val topPartTexture = Texture(pixmap)
        pixmap.dispose()
        return topPartTexture
    }


    companion object {
        private const val TOP_PART_HEIGHT = 150
        private const val TOP_BAR_COLOR = "#85adb0"
        private const val CATEGORY_BACKGROUND_COLOR = "#557d80"
        private const val COINS_BUTTON_PAD_TOP = 60F
        private const val COINS_BUTTON_PAD_RIGHT = 20F
        private const val COINS_BUTTON_PAD_LEFT = 20F
        private const val COINS_BUTTON_PAD_BOTTOM = 20F
        private const val COINS_BUTTON_ANIMATION_SIZE_BY = 40F
    }
}
