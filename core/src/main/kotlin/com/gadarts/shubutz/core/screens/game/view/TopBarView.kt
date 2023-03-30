package com.gadarts.shubutz.core.screens.game.view

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.Queue
import com.badlogic.gdx.utils.TimeUtils
import com.gadarts.shubutz.core.DebugSettings
import com.gadarts.shubutz.core.ShubutzGame
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
    private val gamePlayScreen: GamePlayScreen
) : Table(), Disposable {

    private var letterGlyphLayout: GlyphLayout =
        GlyphLayout(globalHandlers.assetsManager.getFont(FontsDefinitions.VARELA_80), "א")
    private var lastCoinsValueChangeLabelDequeue: Long = 0
    private val coinsValueChangeLabels = Queue<Int>()
    lateinit var coinsIcon: Image
    private lateinit var categoryLabel: Label
    private lateinit var topPartTable: Table
    private lateinit var topPartTexture: Texture
    private lateinit var categoryBackgroundTexture: Texture
    lateinit var coinsLabel: Label

    override fun act(delta: Float) {
        super.act(delta)
        if (coinsValueChangeLabels.notEmpty()) {
            if (TimeUtils.timeSinceMillis(lastCoinsValueChangeLabelDequeue) > COINS_VALUE_CHANGE_LABEL_INTERVAL) {
                applyNextCoinsValueChangeLabel(coinsValueChangeLabels.removeFirst())
                lastCoinsValueChangeLabelDequeue = TimeUtils.millis()
            }
        }
    }

    private fun applyNextCoinsValueChangeLabel(coinsAmount: Int) {
        val winCoinLabel = Label(
            "${if (coinsAmount > 0) "+" else ""}$coinsAmount",
            LabelStyle(
                globalHandlers.assetsManager.getFont(FontsDefinitions.VARELA_80),
                if (coinsAmount > 0) Color.GOLD else Color.RED
            )
        )
        topPartTable.stage.addActor(winCoinLabel)
        val coinsLabelPos = coinsLabel.localToStageCoordinates(auxVector.setZero())
        winCoinLabel.setPosition(coinsLabelPos.x, coinsLabelPos.y)

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

    fun addTopBar(
        assetsManager: GameAssetManager,
        gameModel: GameModel,
        gamePlayScreen: GamePlayScreen,
        stage: GameStage,
        dialogsManager: DialogsManager
    ) {
        stage.addActor(this)
        addTopPart(stage, assetsManager, gamePlayScreen, gameModel, dialogsManager)
        addCategoryLabel(gameModel, assetsManager)
        setPosition(stage.width / 2F, stage.height - prefHeight / 2F)
        setDebug(DebugSettings.SHOW_UI_BORDERS, true)
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
        assetsManager: GameAssetManager,
        gamePlayScreen: GamePlayScreen,
        gameModel: GameModel,
        dialogsManager: DialogsManager
    ) {
        topPartTexture = createTopPartTexture(stage, TOP_BAR_COLOR)
        categoryBackgroundTexture = createTopPartTexture(stage, CATEGORY_BACKGROUND_COLOR)
        topPartTable = Table()
        topPartTable.background = TextureRegionDrawable(topPartTexture)
        topPartTable.debug = DebugSettings.SHOW_UI_BORDERS
        topPartTable.setSize(ShubutzGame.RESOLUTION_WIDTH.toFloat(), TOP_PART_HEIGHT.toFloat())
        addTopPartComponents(topPartTable, assetsManager, gamePlayScreen, gameModel, dialogsManager)
        add(topPartTable).row()
    }

    private fun addBackButton(
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
            { dialogsManager.openExitDialog(stage as GameStage, assetsManager, gamePlayScreen) },
            assetsManager
        )
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
                globalHandlers.soundPlayer.playSound(assetsManager.getSound(SoundsDefinitions.BUTTON))
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
        addBackButton(leftSideTable, assetsManager, gamePlayScreen, dialogsManager)
        addBuyCoinsButton(leftSideTable, dialogsManager)
        table.add(leftSideTable).expandX().left()
        val font80 = assetsManager.getFont(FontsDefinitions.VARELA_80)
        addCoinsLabel(gameModel, font80, table, assetsManager)
    }

    private fun addBuyCoinsButton(
        table: Table,
        dialogsManager: DialogsManager,
    ) {
        val coinsButton = createBuyCoinsButton(table, dialogsManager)
        table.add(coinsButton).pad(
            COINS_BUTTON_PAD_TOP,
            COINS_BUTTON_PAD_LEFT,
            COINS_BUTTON_PAD_BOTTOM,
            COINS_BUTTON_PAD_RIGHT
        )
    }

    private fun createBuyCoinsButton(
        table: Table,
        dialogsManager: DialogsManager,
    ): ImageButton {
        val coinsButton = ImageButton(
            TextureRegionDrawable(globalHandlers.assetsManager.getTexture(COINS_BUTTON_UP)),
            TextureRegionDrawable(globalHandlers.assetsManager.getTexture(COINS_BUTTON_DOWN))
        )
        addClickListenerToButton(
            coinsButton,
            {
                dialogsManager.openBuyCoinsDialog(table.stage as GameStage, gamePlayScreen)
            },
            globalHandlers.assetsManager
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

    private fun createTopPartTexture(stage: GameStage, backgroundColor: String): Texture {
        val pixmap = Pixmap(stage.width.toInt(), TOP_PART_HEIGHT, Pixmap.Format.RGBA8888)
        val color = Color.valueOf(backgroundColor)
        color.a /= 2F
        pixmap.setColor(color)
        pixmap.fill()
        val topPartTexture = Texture(pixmap)
        pixmap.dispose()
        return topPartTexture
    }

    override fun dispose() {
        topPartTexture.dispose()
        categoryBackgroundTexture.dispose()
    }

    fun setCategoryLabelText(currentCategory: String) {
        categoryLabel.setText(currentCategory.reversed())
        categoryLabel.toFront()
    }

    fun applyWinCoinEffect(coinsAmount: Int) {
        if (coinsAmount > 0) {
            addCoinValueChangedLabel(coinsAmount)
        }
    }

    private fun addCoinValueChangedLabel(
        coinsAmount: Int,
    ) {
        coinsValueChangeLabels.addFirst(coinsAmount)
    }

    fun onLetterRevealed(gameModel: GameModel, cost: Int) {
        addCoinValueChangedLabel(-cost)
        coinsLabel.setText(gameModel.coins)
    }

    companion object {
        private const val TOP_PART_HEIGHT = 150
        private val auxVector = Vector2()
        private const val TOP_BAR_COLOR = "#85adb0"
        private const val CATEGORY_BACKGROUND_COLOR = "#557d80"
        private const val COINS_LABEL_PADDING_RIGHT = 40F
        private const val WIN_COIN_LABEL_ANIMATION_DURATION = 4F
        private const val COINS_BUTTON_PAD_TOP = 60F
        private const val COINS_BUTTON_PAD_RIGHT = 20F
        private const val COINS_BUTTON_PAD_LEFT = 20F
        private const val COINS_BUTTON_PAD_BOTTOM = 20F
        private const val COINS_ICON_PAD_RIGHT = 40F
        private const val COINS_ICON_PAD = 20F
        private const val COINS_VALUE_CHANGE_LABEL_INTERVAL = 1000F
    }
}
