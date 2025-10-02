package com.gadarts.shubutz.core.screens.game.view

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Disposable
import com.gadarts.shubutz.core.DebugSettings
import com.gadarts.shubutz.core.ShubutzGame
import com.gadarts.shubutz.core.model.GameModel
import com.gadarts.shubutz.core.model.assets.GameAssetManager
import com.gadarts.shubutz.core.model.assets.definitions.FontsDefinitions
import com.gadarts.shubutz.core.model.assets.definitions.SoundsDefinitions
import com.gadarts.shubutz.core.model.assets.definitions.TexturesDefinitions.BACK_BUTTON
import com.gadarts.shubutz.core.screens.game.GamePlayScreen
import com.gadarts.shubutz.core.screens.game.GlobalHandlers
import com.gadarts.shubutz.core.screens.menu.view.stage.GameStage

class TopBarView(
    private val globalHandlers: GlobalHandlers,
) : Table(), Disposable {
    private var letterGlyphLayout: GlyphLayout =
        GlyphLayout(globalHandlers.assetsManager.getFont(FontsDefinitions.VARELA_80), "א")
    private lateinit var categoryLabel: Label
    private lateinit var topPartTable: Table
    private lateinit var topPartTexture: Texture
    private lateinit var categoryBackgroundTexture: Texture

    override fun dispose() {
        topPartTexture.dispose()
        categoryBackgroundTexture.dispose()
    }

    fun setCategoryLabelText(currentCategory: String) {
        categoryLabel.setText(currentCategory.reversed())
        categoryLabel.toFront()
    }

    fun addTopBar(
        assetsManager: GameAssetManager,
        gameModel: GameModel,
        gamePlayScreen: GamePlayScreen,
        stage: GameStage,
        dialogsHandler: DialogsHandler,
    ) {
        stage.addActor(this)
        addTopPart(stage, assetsManager, gamePlayScreen, dialogsHandler)
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
        categoryLabel = GameLabel(
            gameModel.currentTargetData.currentCategory,
            labelStyle,
            globalHandlers.androidInterface
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
        dialogs: DialogsHandler,
    ) {
        topPartTexture = createTopPartTexture(stage, TOP_BAR_COLOR)
        categoryBackgroundTexture = createTopPartTexture(stage, CATEGORY_BACKGROUND_COLOR)
        topPartTable = Table()
        topPartTable.background = TextureRegionDrawable(topPartTexture)
        topPartTable.debug = DebugSettings.SHOW_UI_BORDERS
        topPartTable.setSize(ShubutzGame.RESOLUTION_WIDTH.toFloat(), TOP_PART_HEIGHT.toFloat())
        addTopPartComponents(topPartTable, am, gamePlayScreen, dialogs)
        add(topPartTable).row()
    }

    private fun addExitButton(
        table: Table,
        assetsManager: GameAssetManager,
        gamePlayScreen: GamePlayScreen,
        dialogsHandler: DialogsHandler
    ) {
        val texture = assetsManager.getTexture(BACK_BUTTON)
        val button = ImageButton(TextureRegionDrawable(texture))
        button.pad(10F, 80F, 10F, 40F)
        addClickListenerToButton(
            button,
            assetsManager
        ) { dialogsHandler.openExitDialog(gamePlayScreen) }
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
        dialogsHandler: DialogsHandler,
    ) {
        val leftSideTable = Table()
        leftSideTable.debug = DebugSettings.SHOW_UI_BORDERS
        addExitButton(leftSideTable, assetsManager, gamePlayScreen, dialogsHandler)
        table.add(leftSideTable).expandX().left()
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
    }
}
