package com.gadarts.shubutz.core.screens.menu.view.stage

import com.badlogic.gdx.Gdx
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
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.viewport.FitViewport
import com.gadarts.shubutz.core.GeneralUtils
import com.gadarts.shubutz.core.model.assets.GameAssetManager
import com.gadarts.shubutz.core.model.assets.definitions.TexturesDefinitions

class GameStage(fitViewport: FitViewport, assetsManager: GameAssetManager) : Stage(
    fitViewport,
    SpriteBatch(),
) {
    lateinit var dialogButtonDown: NinePatchDrawable
    lateinit var dialogButtonUp: NinePatchDrawable

    val openDialogs = HashMap<String, Table>()

    init {
        addCloud(assetsManager, TexturesDefinitions.CLOUD_1, 0)
        addCloud(assetsManager, TexturesDefinitions.CLOUD_2, 1)
        addCloud(assetsManager, TexturesDefinitions.CLOUD_3, 2)
        addCloud(assetsManager, TexturesDefinitions.CLOUD_4, 3)
    }

    init {
        createDialogNinePatches(assetsManager)
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
        val screenWidth = Gdx.graphics.width.toFloat()
        val x = MathUtils.random(0F, screenWidth - cloud.width)
        val heightDivision = Gdx.graphics.height.toFloat() / NUMBER_OF_CLOUDS.toFloat()
        val minY = heightDivision * i
        val maxY = minY + heightDivision - cloud.height
        val y = MathUtils.random(minY, maxY)
        cloud.setPosition(x, y)
        addActor(cloud)
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

    override fun draw() {
        GeneralUtils.resetDisplay(BACKGROUND_COLOR)
        super.draw()
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
    ) {
        dialog.name = name
        addCloseButtonToDialog(assetsManager, dialog)
        val dialogTexture = assetsManager.getTexture(TexturesDefinitions.DIALOG)
        applyDialogBackground(dialogTexture, dialog)
        openDialogs[name] = dialog
    }

    fun addDialog(dialogView: Table, name: String, assetsManager: GameAssetManager) {
        if (openDialogs.containsKey(name)) return
        val dialog = Table()

        dialog.addAction(
            Actions.sequence(
                Actions.fadeOut(0F),
                Actions.fadeIn(1F, Interpolation.swingOut)
            )
        )

        initDialog(assetsManager, dialog, name)
        dialog.add(dialogView)
        dialog.pack()
        dialog.setPosition(width / 2F - dialog.width / 2F, height / 2F - dialog.height / 2F)
        addActor(dialog)
    }

    fun closeAllDialogs() {
        openDialogs.forEach { closeDialog(it.value) }
    }

    private fun addCloseButtonToDialog(
        assetsManager: GameAssetManager,
        dialog: Table
    ) {
        val closeButtonTexture = assetsManager.getTexture(TexturesDefinitions.DIALOG_CLOSE_BUTTON)
        val closeButton = ImageButton(TextureRegionDrawable(closeButtonTexture))
        dialog.add(closeButton).expandX().right().row()
        closeButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                if (dialog.hasActions()) return
                closeDialog(dialog)
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

    fun addButton(
        uiTable: Table,
        onClick: ClickListener,
        text: String,
        newLineAfter: Boolean = true,
        span: Int = 1,
        up: Texture,
        down: Texture,
        disabled: Texture? = null,
        bitmapFont: BitmapFont,
        topPadding: Int,
        scale: Float = 1F
    ): TextButton {
        val upDrawable = TextureRegionDrawable(up)
        val downDrawable = TextureRegionDrawable(down)
        val style = TextButton.TextButtonStyle(upDrawable, downDrawable, null, bitmapFont)
        style.fontColor = Color.WHITE
        if (disabled != null) {
            val disabledDrawable = TextureRegionDrawable(disabled)
            style.disabled = disabledDrawable
            style.disabledFontColor = BUTTON_FONT_COLOR_DISABLED
        }
        val button = TextButton(text, style)
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
        val BACKGROUND_COLOR: Color = Color.valueOf("B5EAEA")
        const val NUMBER_OF_CLOUDS = 4
        private val BUTTON_FONT_COLOR_DISABLED = Color.LIGHT_GRAY
        const val BUTTON_PADDING = 20

        const val CLOUDS_SCALE = 0.1F
        const val CLOUDS_SCALE_DURATION_MIN = 15F
        const val CLOUDS_SCALE_DURATION_MAX = 25F
        const val CLOUDS_MOVEMENT_DUR_MIN = 25F
        const val CLOUDS_MOVEMENT_DUR_MAX = 35F
        private const val COINS_DIALOG_PADDING = 64
        private const val DLG_BUT_NP_PAD = 25
    }
}