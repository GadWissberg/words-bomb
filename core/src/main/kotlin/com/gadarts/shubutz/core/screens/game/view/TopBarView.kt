package com.gadarts.shubutz.core.screens.game.view

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
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
            Label.LabelStyle(assetsManager.getFont(FontsDefinitions.VARELA_80), Color.WHITE)
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
        val texture = assetsManager.getTexture(TexturesDefinitions.BACK_BUTTON)
        val button = ImageButton(TextureRegionDrawable(texture))
        button.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                gamePlayScreen.onClickedBackButton()
                soundPlayer.playSound(assetsManager.getSound(SoundsDefinitions.BUTTON))
            }
        })
        table.add(button).expandX().pad(10F, 80F, 10F, 20F).left()
    }

    private fun addTopPartComponents(
        table: Table,
        assetsManager: GameAssetManager,
        gamePlayScreen: GamePlayScreen,
        gameModel: GameModel
    ) {
        addBackButton(table, assetsManager, gamePlayScreen)
        val font80 = assetsManager.getFont(FontsDefinitions.VARELA_80)
        addCoinsLabel(gameModel, font80, table, assetsManager)
    }

    private fun addCoinsLabel(
        gameModel: GameModel,
        font80: BitmapFont,
        table: Table,
        assetsManager: GameAssetManager
    ) {
        coinsLabel = Label(gameModel.coins.toString(), Label.LabelStyle(font80, Color.WHITE))
        table.add(coinsLabel).pad(0F, 0F, 0F, COINS_LABEL_PADDING_RIGHT)
        table.add(Image(assetsManager.getTexture(TexturesDefinitions.COINS_ICON)))
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
            Label.LabelStyle(assetsManager.getFont(FontsDefinitions.VARELA_80), Color.GOLD)
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
    }
}
