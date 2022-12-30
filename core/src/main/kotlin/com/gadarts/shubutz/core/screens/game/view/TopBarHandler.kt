package com.gadarts.shubutz.core.screens.game.view

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Disposable
import com.gadarts.shubutz.core.DebugSettings
import com.gadarts.shubutz.core.model.GameModel
import com.gadarts.shubutz.core.model.assets.GameAssetManager
import com.gadarts.shubutz.core.model.assets.TexturesDefinitions
import com.gadarts.shubutz.core.screens.game.GamePlayScreen
import com.gadarts.shubutz.core.screens.menu.view.stage.GameStage

class TopBarHandler : Disposable {

    private lateinit var topBarTable: Table
    private lateinit var topBarTexture: Texture
    lateinit var coinsLabel: Label

    fun addTopBar(
        assetsManager: GameAssetManager,
        gameModel: GameModel,
        gamePlayScreen: GamePlayScreen,
        font80: BitmapFont,
        stage: GameStage
    ) {
        createTopBarTexture(stage)
        topBarTable = Table()
        topBarTable.background = TextureRegionDrawable(topBarTexture)
        topBarTable.setSize(stage.width, TOP_BAR_HEIGHT.toFloat())
        topBarTable.debug = DebugSettings.SHOW_UI_BORDERS
        stage.addActor(topBarTable)
        topBarTable.setPosition(0F, stage.height - topBarTable.height)
        addTopBarComponents(topBarTable, assetsManager, gamePlayScreen, gameModel, font80)
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
            }
        })
        table.add(button).expandX().pad(40F).left()
    }

    private fun addTopBarComponents(
        table: Table,
        assetsManager: GameAssetManager,
        gamePlayScreen: GamePlayScreen,
        gameModel: GameModel,
        font80: BitmapFont
    ) {
        addBackButton(table, assetsManager, gamePlayScreen)
        coinsLabel = Label(gameModel.coins.toString(), Label.LabelStyle(font80, Color.WHITE))
        table.add(coinsLabel).pad(0F, 0F, 0F, COINS_LABEL_PADDING_RIGHT)
        table.add(Image(assetsManager.getTexture(TexturesDefinitions.COINS_ICON)))
            .size(
                topBarTexture.height.toFloat(),
                topBarTexture.height.toFloat()
            ).pad(40F)
    }

    private fun createTopBarTexture(stage: GameStage) {
        val pixmap =
            Pixmap(stage.width.toInt(), TOP_BAR_HEIGHT, Pixmap.Format.RGBA8888)
        val color = Color.valueOf(TOP_BAR_COLOR)
        color.a /= 2F
        pixmap.setColor(color)
        pixmap.fill()
        topBarTexture = Texture(pixmap)
        pixmap.dispose()
    }

    override fun dispose() {
        topBarTexture.dispose()
    }

    fun onHide() {
        topBarTable.remove()
    }

    companion object {
        private const val TOP_BAR_HEIGHT = 150
        private const val TOP_BAR_COLOR = "#85adb0"
        private const val COINS_LABEL_PADDING_RIGHT = 40F
    }
}
