package com.gadarts.shubutz.core.screens.game.view

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
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
import com.gadarts.shubutz.core.DebugSettings
import com.gadarts.shubutz.core.ShubutzGame
import com.gadarts.shubutz.core.SoundPlayer
import com.gadarts.shubutz.core.model.GameModel
import com.gadarts.shubutz.core.model.assets.GameAssetManager
import com.gadarts.shubutz.core.model.assets.definitions.FontsDefinitions
import com.gadarts.shubutz.core.model.assets.definitions.SoundsDefinitions
import com.gadarts.shubutz.core.model.assets.definitions.TexturesDefinitions.*
import com.gadarts.shubutz.core.screens.game.GamePlayScreen
import com.gadarts.shubutz.core.screens.menu.view.stage.GameStage

/**
 * Handle the in-game UI top-bar's view.
 */
class TopBarView(
    private val soundPlayer: SoundPlayer,
    private val assetsManager: GameAssetManager,
    private val gamePlayScreen: GamePlayScreen
) : Disposable {

    lateinit var coinsIcon: Image
    private lateinit var table: Table
    private lateinit var categoryLabel: Label
    private lateinit var topPartTable: Table
    private lateinit var topPartTexture: Texture

    /**
     * Displays the current coins the player has.
     */
    lateinit var coinsLabel: Label

    /**
     * Creates and adds the top bar table to the given stage.
     */
    fun addTopBar(
        assetsManager: GameAssetManager,
        gameModel: GameModel,
        gamePlayScreen: GamePlayScreen,
        stage: GameStage,
        dialogsManager: DialogsManager
    ) {
        table = Table()
        stage.addActor(table)
        addTopPart(stage, assetsManager, gamePlayScreen, gameModel, dialogsManager)
        addCategoryLabel(gameModel, assetsManager)
        table.setPosition(stage.width / 2F, stage.height - table.prefHeight / 2F)
        table.setDebug(DebugSettings.SHOW_UI_BORDERS, true)
    }

    private fun addCategoryLabel(
        gameModel: GameModel,
        assetsManager: GameAssetManager
    ) {
        categoryLabel = Label(
            gameModel.currentCategory,
            LabelStyle(assetsManager.getFont(FontsDefinitions.VARELA_80), Color.WHITE)
        )
        categoryLabel.setAlignment(Align.center)
        table.add(categoryLabel).size(ShubutzGame.RESOLUTION_WIDTH.toFloat(), categoryLabel.height)
    }

    private fun addTopPart(
        stage: GameStage,
        assetsManager: GameAssetManager,
        gamePlayScreen: GamePlayScreen,
        gameModel: GameModel,
        dialogsManager: DialogsManager
    ) {
        createTopPartTexture(stage)
        topPartTable = Table()
        topPartTable.background = TextureRegionDrawable(topPartTexture)
        topPartTable.debug = DebugSettings.SHOW_UI_BORDERS
        topPartTable.setSize(ShubutzGame.RESOLUTION_WIDTH.toFloat(), TOP_PART_HEIGHT.toFloat())
        addTopPartComponents(topPartTable, assetsManager, gamePlayScreen, gameModel, dialogsManager)
        table.add(topPartTable).row()
    }

    private fun addBackButton(
        table: Table,
        assetsManager: GameAssetManager,
        gamePlayScreen: GamePlayScreen
    ) {
        val texture = assetsManager.getTexture(BACK_BUTTON)
        val button = ImageButton(TextureRegionDrawable(texture))
        button.pad(10F, 80F, 10F, 40F)
        addClickListenerToButton(button, { gamePlayScreen.onClickedBackButton() }, assetsManager)
        table.add(button).left()
    }

    private fun addClickListenerToButton(
        button: Button,
        runnable: Runnable,
        assetsManager: GameAssetManager
    ) {
        button.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                runnable.run()
                soundPlayer.playSound(assetsManager.getSound(SoundsDefinitions.BUTTON))
            }
        })
    }

    private fun addTopPartComponents(
        table: Table,
        assetsManager: GameAssetManager,
        gamePlayScreen: GamePlayScreen,
        gameModel: GameModel,
        dialogsManager: DialogsManager
    ) {
        val leftSideTable = Table()
        leftSideTable.debug = DebugSettings.SHOW_UI_BORDERS
        addBackButton(leftSideTable, assetsManager, gamePlayScreen)
        addBuyCoinsButton(leftSideTable, assetsManager, dialogsManager)
        table.add(leftSideTable).expandX().left()
        val font80 = assetsManager.getFont(FontsDefinitions.VARELA_80)
        addCoinsLabel(gameModel, font80, table, assetsManager)
    }

    private fun addBuyCoinsButton(
        table: Table,
        assetsManager: GameAssetManager,
        dialogsManager: DialogsManager,
    ) {
        val coinsButton = createBuyCoinsButton(assetsManager, table, dialogsManager)
        table.add(coinsButton).pad(
            COINS_BUTTON_PAD_TOP,
            COINS_BUTTON_PAD_LEFT,
            COINS_BUTTON_PAD_BOTTOM,
            COINS_BUTTON_PAD_RIGHT
        )
    }

    private fun createBuyCoinsButton(
        assetsManager: GameAssetManager,
        table: Table,
        dialogsManager: DialogsManager,
    ): ImageButton {
        val coinsButton = ImageButton(
            TextureRegionDrawable(assetsManager.getTexture(COINS_BUTTON_UP)),
            TextureRegionDrawable(assetsManager.getTexture(COINS_BUTTON_DOWN))
        )
        addClickListenerToButton(
            coinsButton,
            {
                dialogsManager.openBuyCoinsDialog(
                    table.stage as GameStage,
                    assetsManager,
                    gamePlayScreen
                )
            },
            assetsManager
        )
        return coinsButton
    }


    private fun addCoinsLabel(
        gameModel: GameModel,
        font80: BitmapFont,
        table: Table,
        assetsManager: GameAssetManager
    ) {
        coinsLabel = Label(gameModel.coins.toString(), LabelStyle(font80, Color.WHITE))
        table.add(coinsLabel).pad(0F, 0F, 0F, COINS_LABEL_PADDING_RIGHT)
        coinsIcon = Image(assetsManager.getTexture(COINS_ICON))
        table.add(coinsIcon)
            .size(
                topPartTexture.height.toFloat(),
                topPartTexture.height.toFloat()
            ).pad(COINS_ICON_PAD, COINS_ICON_PAD, COINS_ICON_PAD, COINS_ICON_PAD_RIGHT).row()
    }

    private fun createTopPartTexture(stage: GameStage) {
        val pixmap = Pixmap(stage.width.toInt(), TOP_PART_HEIGHT, Pixmap.Format.RGBA8888)
        val color = Color.valueOf(TOP_BAR_COLOR)
        color.a /= 2F
        pixmap.setColor(color)
        pixmap.fill()
        topPartTexture = Texture(pixmap)
        pixmap.dispose()
    }

    override fun dispose() {
        topPartTexture.dispose()
    }

    /**
     * Removes the top-bar's table from the stage.
     */
    fun clear() {
        table.remove()
    }

    fun setCategoryLabelText(currentCategory: String) {
        categoryLabel.setText(currentCategory.reversed())
        categoryLabel.toFront()
    }

    /**
     * Creates a label flying off the coins label to show the player won coins.
     */
    fun applyWinCoinEffect(coinsAmount: Int) {
        if (coinsAmount > 0) {
            addCoinValueChangedLabel(coinsAmount)
        }
    }

    private fun addCoinValueChangedLabel(
        coinsAmount: Int,
    ) {
        val winCoinLabel = Label(
            "${if (coinsAmount > 0) "+" else ""}$coinsAmount",
            LabelStyle(
                assetsManager.getFont(FontsDefinitions.VARELA_80),
                if (coinsAmount > 0) Color.GOLD else Color.RED
            )
        )
        topPartTable.stage.addActor(winCoinLabel)
        val coinsLabelPos = coinsLabel.localToScreenCoordinates(auxVector.setZero())
        winCoinLabel.setPosition(coinsLabelPos.x, topPartTable.stage.height - coinsLabelPos.y)

        winCoinLabel.addAction(
            Actions.sequence(
                Actions.moveBy(
                    0F,
                    -100F,
                    WIN_COIN_LABEL_ANIMATION_DURATION,
                    Interpolation.smooth2
                ),
                Actions.removeActor()
            )
        )
    }

    fun onLetterRevealed(gameModel: GameModel, cost: Int) {
        addCoinValueChangedLabel(-cost)
        coinsLabel.setText(gameModel.coins)
    }

    companion object {
        private const val TOP_PART_HEIGHT = 150
        private val auxVector = Vector2()
        private const val TOP_BAR_COLOR = "#85adb0"
        private const val COINS_LABEL_PADDING_RIGHT = 40F
        private const val WIN_COIN_LABEL_ANIMATION_DURATION = 4F
        private const val COINS_BUTTON_PAD_TOP = 60F
        private const val COINS_BUTTON_PAD_RIGHT = 20F
        private const val COINS_BUTTON_PAD_LEFT = 20F
        private const val COINS_BUTTON_PAD_BOTTOM = 20F
        private const val COINS_ICON_PAD_RIGHT = 40F
        private const val COINS_ICON_PAD = 20F

    }
}
