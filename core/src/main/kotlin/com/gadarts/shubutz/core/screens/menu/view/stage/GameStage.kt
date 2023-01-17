package com.gadarts.shubutz.core.screens.menu.view.stage

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.viewport.FitViewport
import com.gadarts.shubutz.core.GameStage
import com.gadarts.shubutz.core.GeneralUtils
import com.gadarts.shubutz.core.model.assets.GameAssetManager
import com.gadarts.shubutz.core.model.assets.TexturesDefinitions

class GameStage(fitViewport: FitViewport, assetsManager: GameAssetManager) :
    GameStage<MenuStageEventsSubscriber>(
        fitViewport,
        SpriteBatch(),
        assetsManager
    ) {

    val openDialogs = HashMap<String, Table>()
    override val subscribers = HashSet<MenuStageEventsSubscriber>()

    init {
        addCloud(assetsManager, TexturesDefinitions.CLOUD_1, 0)
        addCloud(assetsManager, TexturesDefinitions.CLOUD_2, 1)
        addCloud(assetsManager, TexturesDefinitions.CLOUD_3, 2)
        addCloud(assetsManager, TexturesDefinitions.CLOUD_4, 3)
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

    companion object {
        val BACKGROUND_COLOR: Color = Color.valueOf("B5EAEA")
        const val NUMBER_OF_CLOUDS = 4
        const val CLOUDS_SCALE = 0.1F
        const val CLOUDS_SCALE_DURATION_MIN = 15F
        const val CLOUDS_SCALE_DURATION_MAX = 25F
        const val CLOUDS_MOVEMENT_DUR_MIN = 25F
        const val CLOUDS_MOVEMENT_DUR_MAX = 35F
        private const val COINS_DIALOG_PADDING = 64
    }
}