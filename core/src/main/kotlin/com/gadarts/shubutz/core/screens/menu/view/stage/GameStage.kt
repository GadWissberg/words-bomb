package com.gadarts.shubutz.core.screens.menu.view.stage

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.viewport.StretchViewport
import com.gadarts.shubutz.core.DebugSettings
import com.gadarts.shubutz.core.GeneralUtils
import com.gadarts.shubutz.core.ShubutzGame
import com.gadarts.shubutz.core.model.assets.GameAssetManager
import com.gadarts.shubutz.core.model.assets.definitions.FontsDefinitions
import com.gadarts.shubutz.core.model.assets.definitions.TexturesDefinitions

class GameStage(stretchViewport: StretchViewport, gameAssetManager: GameAssetManager) : Stage(
    stretchViewport,
    SpriteBatch(),
) {
    val openDialogs = HashMap<String, Table>()
    private lateinit var dialogButtonDown: NinePatchDrawable
    private lateinit var dialogButtonUp: NinePatchDrawable

    init {
        addCloud(gameAssetManager, TexturesDefinitions.CLOUD_1, 0)
        addCloud(gameAssetManager, TexturesDefinitions.CLOUD_2, 1)
        addCloud(gameAssetManager, TexturesDefinitions.CLOUD_3, 2)
        addCloud(gameAssetManager, TexturesDefinitions.CLOUD_4, 3)
    }

    init {
        createDialogNinePatches(gameAssetManager)
    }

    fun createNinePatchButtonStyle(assetsManager: GameAssetManager) =
        ImageTextButton.ImageTextButtonStyle(
            dialogButtonUp,
            dialogButtonDown,
            null,
            assetsManager.getFont(FontsDefinitions.VARELA_40)
        )

    override fun draw() {
        GeneralUtils.resetDisplay(BACKGROUND_COLOR)
        super.draw()
    }

    fun addDialog(
        dialogView: Table,
        name: String,
        assetsManager: GameAssetManager,
        onCloseButtonClick: (() -> Unit)? = null,
        onDialogReady: (() -> Unit)? = null,
    ) {
        if (openDialogs.containsKey(name)) return
        val dialog = Table()

        dialog.addAction(
            Actions.sequence(
                Actions.fadeOut(0F),
                Actions.fadeIn(0.5F, Interpolation.swingOut),
                if (onDialogReady != null) Actions.run(onDialogReady) else Actions.delay(0F)
            )
        )

        initDialog(assetsManager, dialog, name, onCloseButtonClick)
        dialog.add(dialogView).expand()
        dialog.pack()
        dialog.setPosition(width / 2F - dialog.width / 2F, height / 2F - dialog.height / 2F)
        addActor(dialog)
    }

    fun addButton(
        uiTable: Table,
        onClick: ClickListener,
        text: String?,
        newLineAfter: Boolean = true,
        span: Int = 1,
        up: Texture,
        down: Texture,
        disabled: Texture? = null,
        bitmapFont: BitmapFont,
        topPadding: Int,
        scale: Float = 1F,
        image: Texture? = null
    ): TextButton {
        val style = createButtonStyle(up, down, bitmapFont, disabled)
        val button = TextButton(text, style)
        if (image != null) {
            val imageComponent = Image(image)
            imageComponent.setScaling(Scaling.none)
            button.add(imageComponent)
        } else {
            button.labelCell.pad(BUTTON_LABEL_PADDING)
        }
        addButtonToTable(
            button,
            onClick,
            uiTable,
            newLineAfter,
            span = span,
            topPadding = topPadding.toFloat(),
            scale = scale
        )
        return button
    }

    fun closeDialog(dialogName: String) {
        openDialogs[dialogName]?.let { closeDialog(it) }
    }

    fun closeAllDialogs() {
        openDialogs.forEach { closeDialog(it.value) }
    }

    private fun createButtonStyle(
        up: Texture,
        down: Texture,
        bitmapFont: BitmapFont,
        disabled: Texture?
    ): TextButton.TextButtonStyle {
        val upDrawable = TextureRegionDrawable(up)
        val downDrawable = TextureRegionDrawable(down)
        val style = TextButton.TextButtonStyle(upDrawable, downDrawable, null, bitmapFont)
        style.fontColor = Color.WHITE
        if (disabled != null) {
            style.disabled = TextureRegionDrawable(disabled)
            style.disabledFontColor = BUTTON_FONT_COLOR_DISABLED
        }
        return style
    }

    private fun createDialogButtonNinePatch(
        am: GameAssetManager, texture: TexturesDefinitions
    ): NinePatch {
        return NinePatch(
            am.getTexture(texture),
            DLG_BUT_NP_PAD, DLG_BUT_NP_PAD,
            DLG_BUT_NP_PAD, DLG_BUT_NP_PAD
        )
    }

    private fun createDialogNinePatches(am: GameAssetManager) {
        dialogButtonUp = NinePatchDrawable(
            createDialogButtonNinePatch(
                am,
                TexturesDefinitions.POPUP_BUTTON_UP
            )
        )
        dialogButtonDown = NinePatchDrawable(
            createDialogButtonNinePatch(
                am,
                TexturesDefinitions.POPUP_BUTTON_DOWN
            )
        )
    }

    private fun addCloud(assetsManager: GameAssetManager, texture: TexturesDefinitions, i: Int) {
        val cloud = Image(assetsManager.getTexture(texture))
        val heightDivision = ShubutzGame.RESOLUTION_HEIGHT.toFloat() / NUMBER_OF_CLOUDS.toFloat()
        val screenWidth = ShubutzGame.RESOLUTION_WIDTH.toFloat()
        val x = MathUtils.random(0F, screenWidth - cloud.width)
        val minY = heightDivision * i
        val maxY = minY + heightDivision - cloud.height
        val y = MathUtils.random(minY, maxY)
        cloud.setPosition(x, y)
        addActor(cloud)
        animateCloud(cloud, screenWidth)
    }

    private fun animateCloud(cloud: Image, screenWidth: Float) {
        cloud.addAction(
            Actions.parallel(
                Actions.forever(
                    Actions.sequence(
                        Actions.scaleBy(
                            CLOUDS_SCALE,
                            CLOUDS_SCALE,
                            MathUtils.random(CLOUDS_SCALE_DURATION_MIN, CLOUDS_SCALE_DURATION_MAX),
                            Interpolation.bounce
                        ),
                        Actions.scaleBy(
                            -CLOUDS_SCALE,
                            -CLOUDS_SCALE,
                            MathUtils.random(CLOUDS_SCALE_DURATION_MIN, CLOUDS_SCALE_DURATION_MAX),
                            Interpolation.bounce
                        ),
                    )
                ),
                Actions.forever(
                    Actions.sequence(
                        Actions.moveTo(
                            screenWidth - cloud.width,
                            cloud.y,
                            MathUtils.random(CLOUDS_MOVEMENT_DUR_MIN, CLOUDS_MOVEMENT_DUR_MAX),
                            Interpolation.bounceIn
                        ),
                        Actions.moveTo(
                            0F,
                            cloud.y,
                            MathUtils.random(CLOUDS_MOVEMENT_DUR_MIN, CLOUDS_MOVEMENT_DUR_MAX),
                            Interpolation.bounceIn
                        )
                    )
                )
            )
        )
    }

    private fun applyDialogBackground(
        popupTexture: Texture,
        popup: Table
    ) {
        val ninePatch = NinePatch(
            popupTexture,
            COINS_DIALOG_PADDING,
            COINS_DIALOG_PADDING,
            COINS_DIALOG_PADDING,
            COINS_DIALOG_PADDING
        )
        popup.background = NinePatchDrawable(ninePatch)
    }

    private fun initDialog(
        assetsManager: GameAssetManager,
        dialog: Table,
        name: String,
        onCloseButtonClick: (() -> Unit)?,
    ) {
        dialog.name = name
        dialog.debug = DebugSettings.SHOW_UI_BORDERS
        addCloseButtonToDialog(assetsManager, dialog, onCloseButtonClick)
        val dialogTexture = assetsManager.getTexture(TexturesDefinitions.DIALOG)
        applyDialogBackground(dialogTexture, dialog)
        openDialogs[name] = dialog
    }

    private fun addCloseButtonToDialog(
        assetsManager: GameAssetManager,
        dialog: Table,
        onCloseButtonClick: (() -> Unit)?
    ) {
        val closeButtonTexture = assetsManager.getTexture(TexturesDefinitions.DIALOG_CLOSE_BUTTON)
        val closeButton = ImageButton(TextureRegionDrawable(closeButtonTexture))
        dialog.add(closeButton).expandX().top().right().row()
        closeButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                if (dialog.hasActions()) return
                closeDialog(dialog)
                onCloseButtonClick?.invoke()
            }
        })
    }

    private fun closeDialog(dialog: Table) {
        dialog.addAction(
            Actions.sequence(
                Actions.fadeOut(1F, Interpolation.swingOut),
                Actions.run {
                    openDialogs.remove(dialog.name)
                    dialog.remove()
                }
            ),
        )
    }

    private fun addButtonToTable(
        button: Button,
        onClick: ClickListener,
        uiTable: Table,
        newLineAfter: Boolean = true,
        span: Int = 1,
        topPadding: Float,
        scale: Float
    ) {
        button.addListener(onClick)
        val row = uiTable.add(button).pad(
            topPadding,
            BUTTON_PADDING.toFloat(),
            BUTTON_PADDING.toFloat(),
            BUTTON_PADDING.toFloat()
        ).colspan(span)
        if (scale != 1F) {
            row.size(button.width * scale, button.height * scale)
        }
        if (newLineAfter) {
            uiTable.row()
        }
    }

    companion object {
        const val BUTTON_PADDING = 20
        private val BACKGROUND_COLOR: Color = Color.valueOf("B5EAEA")
        private val BUTTON_FONT_COLOR_DISABLED = Color.LIGHT_GRAY
        private const val NUMBER_OF_CLOUDS = 4
        private const val BUTTON_LABEL_PADDING = 40F
        private const val CLOUDS_SCALE = 0.1F
        private const val CLOUDS_SCALE_DURATION_MIN = 15F
        private const val CLOUDS_SCALE_DURATION_MAX = 25F
        private const val CLOUDS_MOVEMENT_DUR_MIN = 25F
        private const val CLOUDS_MOVEMENT_DUR_MAX = 35F
        private const val COINS_DIALOG_PADDING = 64
        private const val DLG_BUT_NP_PAD = 25
    }
}