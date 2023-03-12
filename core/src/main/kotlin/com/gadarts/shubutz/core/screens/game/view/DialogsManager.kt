package com.gadarts.shubutz.core.screens.game.view

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.ui.Stack
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.gadarts.shubutz.core.SoundPlayer
import com.gadarts.shubutz.core.model.InAppProducts
import com.gadarts.shubutz.core.model.Product
import com.gadarts.shubutz.core.model.assets.GameAssetManager
import com.gadarts.shubutz.core.model.assets.definitions.AtlasesDefinitions
import com.gadarts.shubutz.core.model.assets.definitions.FontsDefinitions
import com.gadarts.shubutz.core.model.assets.definitions.SoundsDefinitions
import com.gadarts.shubutz.core.model.assets.definitions.TexturesDefinitions
import com.gadarts.shubutz.core.screens.game.GamePlayScreen
import com.gadarts.shubutz.core.screens.menu.view.stage.GameStage
import java.util.*

class DialogsManager(private val soundPlayer: SoundPlayer) {
    private fun addCoinsDialogDescription(assetsManager: GameAssetManager, dialog: Table) {
        val style = Label.LabelStyle(assetsManager.getFont(FontsDefinitions.VARELA_40), Color.WHITE)
        val text = Label(fixHebrewDescription(COINS_DIALOG_DESCRIPTION), style)
        text.setAlignment(Align.right)
        dialog.add(text).pad(0F, 0F, COINS_DIALOG_DESCRIPTION_PADDING_BOTTOM, 0F).row()
    }

    private fun fixHebrewDescription(text: String): CharSequence {
        val reversed = text.reversed()
        val result = java.lang.StringBuilder()
        reversed.split("\n").forEach { result.insert(0, "\n").insert(0, it) }
        return result.toString()
    }

    private fun addHeaderToCoinsWindow(
        assetsManager: GameAssetManager,
        popup: Table
    ) {
        val font = assetsManager.getFont(FontsDefinitions.VARELA_80)
        val headerStyle = Label.LabelStyle(font, Color.WHITE)
        popup.add(Label(COINS_POPUP_HEADER.reversed(), headerStyle))
            .pad(0F, 0F, COINS_POPUP_HEADER_PADDING_BOTTOM, 0F)
            .row()
    }

    private fun addPackButton(
        popup: Table,
        product: Product?,
        stage: GameStage,
        definition: InAppProducts,
        gameAssetManager: GameAssetManager,
        gamePlayScreen: GamePlayScreen,
    ) {
        val button = createPackButton(definition, product, stage, gameAssetManager, gamePlayScreen)
        val stack = Stack()
        button.add(stack)
        addFlashEffect(definition, stack, gameAssetManager)
        val image = addPurchaseIcon(definition, stack, gameAssetManager)
        popup.add(button).pad(COINS_POPUP_BUTTON_PADDING).row()

        if (definition.applyAnimation) {
            image.addAction(
                Actions.forever(
                    Actions.sequence(
                        Actions.delay(MathUtils.random(3F, 5F)),
                        Actions.sizeBy(
                            20F,
                            20F,
                            1F,
                            Interpolation.swingIn
                        ),
                        Actions.sizeBy(
                            -20F,
                            -20F,
                            1F,
                            Interpolation.swingIn
                        ),
                        Actions.delay(2F)
                    ),
                )
            )
        }
    }

    private fun addPurchaseIcon(
        definition: InAppProducts,
        stack: Stack,
        gameAssetManager: GameAssetManager
    ): Image {
        val image = Image(gameAssetManager.getTexture(definition.icon))
        image.setScaling(Scaling.none)
        stack.add(image)
        return image
    }

    private fun addFlashEffect(
        definition: InAppProducts,
        stack: Stack,
        gameAssetManager: GameAssetManager
    ) {
        if (definition.flashEffect) {
            val texture = gameAssetManager.getTexture(TexturesDefinitions.FLASH)
            val flash = FlashEffect(texture)
            stack.add(flash)
            flash.setOrigin(texture.width / 2F, texture.height / 2F)
            flash.addAction(
                Actions.forever(
                    Actions.rotateBy(
                        360F,
                        FLASH_EFFECT_DURATION
                    )
                )
            )
        }
    }

    private fun createPackButton(
        definition: InAppProducts,
        product: Product?,
        stage: GameStage,
        gameAssetManager: GameAssetManager,
        gamePlayScreen: GamePlayScreen
    ): ImageTextButton {
        val button = ImageTextButton(
            definition.label.format(
                definition.amount.toString().reversed()
            ).reversed(), stage.createNinePatchButtonStyle(gameAssetManager)
        )
        addClickListenerToButton(button, {
            if (product != null) {
                gamePlayScreen.onPackPurchaseButtonClicked(product)
            }
        }, gameAssetManager)
        return button
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


    private fun addCoinsDialogComponents(
        gameAssetManager: GameAssetManager,
        layout: Table,
        products: Map<String, Product>,
        gamePlayScreen: GamePlayScreen,
        stage: GameStage,
    ) {
        addHeaderToCoinsWindow(gameAssetManager, layout)
        addCoinsDialogDescription(gameAssetManager, layout)
        InAppProducts.values().forEach {
            val id = it.name.lowercase(Locale.ROOT)
            if (products.containsKey(id)) {
                addPackButton(
                    layout,
                    products[id],
                    stage = stage,
                    definition = it,
                    gameAssetManager,
                    gamePlayScreen
                )
            }
        }
        layout.pack()
        (layout.parent as Table).pack()
        (layout.parent as Table).setPosition(
            (layout.parent as Table).stage.width / 2F - (layout.parent as Table).prefWidth / 2F,
            (layout.parent as Table).stage.height / 2F - (layout.parent as Table).prefHeight / 2F
        )
    }

    fun openBuyCoinsDialog(
        stage: GameStage,
        assetsManager: GameAssetManager,
        gamePlayScreen: GamePlayScreen,
    ) {
        stage.addDialog(
            addCoinsDialog(assetsManager, gamePlayScreen, stage),
            COINS_DIALOG_NAME,
            assetsManager
        )
    }

    private fun addCoinsDialog(
        gameAssetManager: GameAssetManager,
        gamePlayScreen: GamePlayScreen,
        stage: GameStage,
    ): Table {
        val dialogLayout = Table()
        val keyFrames = gameAssetManager.getAtlas(AtlasesDefinitions.LOADING).regions
        val loadingAnimation = LoadingAnimation(keyFrames)
        dialogLayout.add(loadingAnimation).row()
        gamePlayScreen.onOpenProductsMenu({
            loadingAnimation.remove()
            dialogLayout.pack()
            if (it.isNotEmpty()) {
                addCoinsDialogComponents(gameAssetManager, dialogLayout, it, gamePlayScreen, stage)
            }
        }, {
            loadingAnimation.remove()
            dialogLayout.add(ViewUtils.createDialogLabel(it, gameAssetManager))
            dialogLayout.pack()
        })
        return dialogLayout
    }

    companion object {
        private const val COINS_DIALOG_NAME = "coins"
        private const val COINS_POPUP_HEADER = "קבל עוד מטבעות"
        private const val COINS_POPUP_HEADER_PADDING_BOTTOM = 64F
        private const val COINS_DIALOG_DESCRIPTION =
            "לרשותך מס' אפשרויות להשיג\nעוד מטבעות.\nכל רכישה תסיר את כל הפרסומות!"
        private const val COINS_DIALOG_DESCRIPTION_PADDING_BOTTOM = 64F
        private const val COINS_POPUP_BUTTON_PADDING = 32F
        private const val FLASH_EFFECT_DURATION = 4F
    }


}
