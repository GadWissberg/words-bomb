package com.gadarts.shubutz.core.screens.game.view

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton.ImageTextButtonStyle
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Disposable
import com.gadarts.shubutz.core.DebugSettings
import com.gadarts.shubutz.core.SoundPlayer
import com.gadarts.shubutz.core.model.GameModel
import com.gadarts.shubutz.core.model.assets.FontsDefinitions
import com.gadarts.shubutz.core.model.assets.GameAssetManager
import com.gadarts.shubutz.core.model.assets.SoundsDefinitions
import com.gadarts.shubutz.core.model.assets.TexturesDefinitions
import com.gadarts.shubutz.core.model.assets.TexturesDefinitions.*
import com.gadarts.shubutz.core.screens.game.GamePlayScreen
import com.gadarts.shubutz.core.screens.menu.view.stage.GameStage

/**
 * Handle the in-game UI top-bar's view.
 */
class TopBarView(private val soundPlayer: SoundPlayer) : Disposable {

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
        stage: GameStage
    ) {
        table = Table()
        stage.addActor(table)
        addTopPart(stage, assetsManager, gamePlayScreen, gameModel)
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
        table.add(categoryLabel).size(Gdx.graphics.width.toFloat(), categoryLabel.height)
    }

    private fun addTopPart(
        stage: GameStage,
        assetsManager: GameAssetManager,
        gamePlayScreen: GamePlayScreen,
        gameModel: GameModel
    ) {
        createTopPartTexture(stage)
        topPartTable = Table()
        topPartTable.background = TextureRegionDrawable(topPartTexture)
        topPartTable.debug = DebugSettings.SHOW_UI_BORDERS
        topPartTable.setSize(Gdx.graphics.width.toFloat(), TOP_PART_HEIGHT.toFloat())
        addTopPartComponents(topPartTable, assetsManager, gamePlayScreen, gameModel)
        table.add(topPartTable).row()
    }

    private fun addBackButton(
        table: Table,
        assetsManager: GameAssetManager,
        gamePlayScreen: GamePlayScreen
    ) {
        val texture = assetsManager.getTexture(BACK_BUTTON)
        val button = ImageButton(TextureRegionDrawable(texture))
        addClickListenerToButton(button, { gamePlayScreen.onClickedBackButton() }, assetsManager)
        table.add(button).pad(10F, 80F, 10F, 40F).left()
    }

    private fun addClickListenerToButton(
        button: ImageButton,
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
        gameModel: GameModel
    ) {
        val leftSideTable = Table()
        leftSideTable.debug = DebugSettings.SHOW_UI_BORDERS
        addBackButton(leftSideTable, assetsManager, gamePlayScreen)
        addBuyCoinsButton(leftSideTable, assetsManager)
        table.add(leftSideTable).expandX().left()
        val font80 = assetsManager.getFont(FontsDefinitions.VARELA_80)
        addCoinsLabel(gameModel, font80, table, assetsManager)
    }

    private fun addBuyCoinsButton(table: Table, assetsManager: GameAssetManager) {
        val coinsButton = createBuyCoinsButton(assetsManager, table)
        table.add(coinsButton).pad(
            COINS_BUTTON_PAD_TOP,
            COINS_BUTTON_PAD_LEFT,
            COINS_BUTTON_PAD_BOTTOM,
            COINS_BUTTON_PAD_RIGHT
        )
    }

    private fun createBuyCoinsButton(
        assetsManager: GameAssetManager,
        table: Table
    ): ImageButton {
        val coinsButton = ImageButton(
            TextureRegionDrawable(assetsManager.getTexture(COINS_BUTTON_UP)),
            TextureRegionDrawable(assetsManager.getTexture(COINS_BUTTON_DOWN))
        )
        addClickListenerToButton(
            coinsButton,
            {
                (table.stage as GameStage).addPopup(
                    addCoinsPopup(assetsManager, table),
                    COINS_POPUP_NAME
                )
            },
            assetsManager
        )
        return coinsButton
    }

    private fun addCoinsPopup(
        assets: GameAssetManager,
        table: Table
    ): Table {
        val popup = Table()
        addCoinsWindowComponents(assets, popup)
        initCoinsWindow(assets, popup, table)
        return popup
    }

    private fun addCoinsWindowComponents(
        assets: GameAssetManager,
        popup: Table
    ) {
        addHeaderToCoinsWindow(assets, popup)
        addCoinsWindowDescription(assets, popup)
        addPackButton(assets, popup, FIRST_PACK_LABEL, ICON_PACK_1, GameModel.AMOUNT_PACK_FIRST)
        addPackButton(assets, popup, SECOND_PACK_LABEL, ICON_PACK_2, GameModel.AMOUNT_PACK_SECOND)
        addPackButton(assets, popup, THIRD_PACK_LABEL, ICON_PACK_3, GameModel.AMOUNT_PACK_THIRD)
    }

    private fun addCoinsWindowDescription(assetsManager: GameAssetManager, popup: Table) {
        val style = LabelStyle(assetsManager.getFont(FontsDefinitions.VARELA_40), Color.WHITE)
        val text = Label(COINS_POPUP_DESCRIPTION.reversed(), style)
        popup.add(text).pad(0F, 0F, COINS_POPUP_DESCRIPTION_PADDING_BOTTOM, 0F).row()
    }

    private fun addPackButton(
        assetsManager: GameAssetManager,
        popup: Table,
        label: String,
        texturesDefinition: TexturesDefinitions,
        amount: Int
    ) {
        val packButton = ImageTextButton(
            label.format(amount.toString().reversed()).reversed(), ImageTextButtonStyle(
                TextureRegionDrawable(assetsManager.getTexture(POPUP_BUTTON_UP)),
                TextureRegionDrawable(assetsManager.getTexture(POPUP_BUTTON_DOWN)),
                null,
                assetsManager.getFont(FontsDefinitions.VARELA_40)
            )
        )
        packButton.add(Image(assetsManager.getTexture(texturesDefinition)))
        popup.add(packButton).pad(COINS_POPUP_BUTTON_PADDING).row()
    }

    private fun addHeaderToCoinsWindow(
        assetsManager: GameAssetManager,
        popup: Table
    ) {
        val font = assetsManager.getFont(FontsDefinitions.VARELA_80)
        val headerStyle = LabelStyle(font, Color.WHITE)
        popup.add(Label(COINS_POPUP_HEADER.reversed(), headerStyle))
            .pad(0F, 0F, COINS_POPUP_HEADER_PADDING_BOTTOM, 0F)
            .row()
    }

    private fun initCoinsWindow(
        assetsManager: GameAssetManager,
        popup: Table,
        table: Table
    ) {
        val popupTexture = assetsManager.getTexture(POPUP)
        applyCoinsWindowBackground(popupTexture, popup)
        popup.pack()
        popup.setPosition(
            table.stage.width / 2F - popup.width / 2F,
            table.stage.height / 2F - popup.height / 2F
        )
    }

    private fun applyCoinsWindowBackground(
        popupTexture: Texture,
        popup: Table
    ) {
        val ninePatch = NinePatch(
            popupTexture,
            COINS_POPUP_PADDING,
            COINS_POPUP_PADDING,
            COINS_POPUP_PADDING,
            COINS_POPUP_PADDING
        )
        popup.background = NinePatchDrawable(ninePatch)
    }

    private fun addCoinsLabel(
        gameModel: GameModel,
        font80: BitmapFont,
        table: Table,
        assetsManager: GameAssetManager
    ) {
        coinsLabel = Label(gameModel.coins.toString(), LabelStyle(font80, Color.WHITE))
        table.add(coinsLabel).pad(0F, 0F, 0F, COINS_LABEL_PADDING_RIGHT)
        table.add(Image(assetsManager.getTexture(COINS_ICON)))
            .size(
                topPartTexture.height.toFloat(),
                topPartTexture.height.toFloat()
            ).pad(10F).row()
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
    }

    /**
     * Creates a label flying off the coins label to show the player won coins.
     */
    fun applyWinCoinEffect(coinsAmount: Int, assetsManager: GameAssetManager) {
        if (coinsAmount > 0) {
            val winCoinLabel = addWinCoinLabel(coinsAmount, assetsManager)
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
    }

    private fun addWinCoinLabel(
        coinsAmount: Int,
        assetsManager: GameAssetManager
    ): Label {
        val winCoinLabel = Label(
            "+$coinsAmount",
            LabelStyle(assetsManager.getFont(FontsDefinitions.VARELA_80), Color.GOLD)
        )
        topPartTable.stage.addActor(winCoinLabel)
        val coinsLabelPos = coinsLabel.localToScreenCoordinates(auxVector.setZero())
        winCoinLabel.setPosition(coinsLabelPos.x, topPartTable.stage.height - coinsLabelPos.y)
        return winCoinLabel
    }

    companion object {
        private const val TOP_PART_HEIGHT = 150
        private val auxVector = Vector2()
        private const val TOP_BAR_COLOR = "#85adb0"
        private const val COINS_LABEL_PADDING_RIGHT = 40F
        private const val WIN_COIN_LABEL_ANIMATION_DURATION = 4F
        private const val COINS_POPUP_HEADER = "קבל עוד מטבעות"
        private const val COINS_POPUP_PADDING = 64
        private const val COINS_POPUP_HEADER_PADDING_BOTTOM = 64F
        private const val COINS_POPUP_BUTTON_PADDING = 32F
        private const val COINS_POPUP_DESCRIPTION = "כל רכישה תסיר את כל הפרסומות!"
        private const val COINS_POPUP_DESCRIPTION_PADDING_BOTTOM = 64F
        private const val COINS_POPUP_NAME = "coins"
        private const val FIRST_PACK_LABEL = "%s מטבעות"
        private const val SECOND_PACK_LABEL = "שק של %s מטבעות"
        private const val THIRD_PACK_LABEL = "תיבה של %s מטבעות"
        private const val COINS_BUTTON_PAD_TOP = 60F
        private const val COINS_BUTTON_PAD_RIGHT = 20F
        private const val COINS_BUTTON_PAD_LEFT = 20F
        private const val COINS_BUTTON_PAD_BOTTOM = 20F
    }
}
