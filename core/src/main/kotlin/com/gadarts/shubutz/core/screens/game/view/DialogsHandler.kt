package com.gadarts.shubutz.core.screens.game.view

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Stack
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Scaling
import com.gadarts.shubutz.core.AndroidInterface
import com.gadarts.shubutz.core.DebugSettings
import com.gadarts.shubutz.core.SoundPlayer
import com.gadarts.shubutz.core.model.GameModel
import com.gadarts.shubutz.core.model.InAppProducts
import com.gadarts.shubutz.core.model.Product
import com.gadarts.shubutz.core.model.assets.GameAssetManager
import com.gadarts.shubutz.core.model.assets.definitions.AtlasesDefinitions
import com.gadarts.shubutz.core.model.assets.definitions.FontsDefinitions
import com.gadarts.shubutz.core.model.assets.definitions.SoundsDefinitions
import com.gadarts.shubutz.core.model.assets.definitions.TexturesDefinitions
import com.gadarts.shubutz.core.screens.game.GamePlayScreen
import com.gadarts.shubutz.core.screens.menu.view.stage.GameStage
import java.util.Locale

class DialogsHandler(
    private val assetsManager: GameAssetManager,
    private val effectsHandler: EffectsHandler,
    private val stage: GameStage,
    private val soundPlayer: SoundPlayer,
    private val androidInterface: AndroidInterface
) {
    fun openBuyCoinsDialog(
        gamePlayScreen: GamePlayScreen,
    ) {
        val loadingAnimation = createLoadingAnimation()
        val layout = Table()
        layout.add(loadingAnimation).row()
        stage.addDialog(layout, COINS_DIALOG_LOADING_NAME, assetsManager) {
            gamePlayScreen.onOpenProductsMenu({
                loadingAnimation.remove()
                layout.remove()
                stage.addDialog(layout, COINS_DIALOG_NAME, assetsManager)
                if (it.isNotEmpty()) {
                    addCoinsDialogComponents(layout, it, gamePlayScreen) {
                        effectsHandler.applyPartyEffect(
                            assetsManager,
                            soundPlayer,
                            stage
                        )
                    }
                }
                layout.pack()
                stage.closeDialog(COINS_DIALOG_LOADING_NAME)
            }, {
                loadingAnimation.remove()
                layout.add(ViewUtils.createDialogLabel(it, assetsManager, androidInterface))
                layout.pack()
            })
        }
    }

    fun openCoinsPurchasedSuccessfully(
        assetsManager: GameAssetManager,
        stage: GameStage,
        amount: Int
    ) {
        val dialogView =
            createCoinsPurchasedSuccessfullyDialog(assetsManager, amount)
        finalizeDialog(dialogView, COINS_PURCHASED_DIALOG_NAME)
        stage.closeDialog(COINS_DIALOG_NAME)
    }

    fun openExitDialog(
        gamePlayScreen: GamePlayScreen
    ) {
        val dialogView = createDialogLayout(
            assetsManager,
            EXIT_DIALOG_HEADER,
            EXIT_DIALOG_DESCRIPTION
        )
        addExitDialogButtons(gamePlayScreen, dialogView)
        finalizeDialog(dialogView, EXIT_DIALOG_NAME)
    }

    private fun addDialogText(
        assetsManager: GameAssetManager,
        dialog: Table,
        description: String,
        colSpan: Int = 1,
        topPadding: Float = 0F,
        bottomPadding: Float = DIALOG_DESCRIPTION_PADDING_BOTTOM
    ) {
        val style = Label.LabelStyle(assetsManager.getFont(FontsDefinitions.VARELA_40), Color.WHITE)
        val text = GameLabel(fixHebrewDescription(description), style, androidInterface)
        text.setAlignment(Align.right)
        dialog.add(text).pad(topPadding, 0F, bottomPadding, 0F).colspan(colSpan)
            .row()
    }

    private fun fixHebrewDescription(text: String): CharSequence {
        val reversed = text.reversed()
        val result = java.lang.StringBuilder()
        reversed.split("\n").forEach { result.insert(0, "\n").insert(0, it) }
        return result.toString()
    }

    private fun addHeaderToDialog(
        assetsManager: GameAssetManager,
        popup: Table,
        text: String,
        colSpan: Int = 1
    ) {
        val font = assetsManager.getFont(FontsDefinitions.VARELA_80)
        val headerStyle = Label.LabelStyle(font, Color.WHITE)
        popup.add(Label(text.reversed(), headerStyle))
            .pad(0F, 0F, DIALOG_HEADER_PADDING_BOTTOM, 0F)
            .colspan(colSpan)
            .row()
    }

    private fun addPackButton(
        dialog: Table,
        product: Product?,
        definition: InAppProducts,
        gameAssetManager: GameAssetManager,
        gamePlayScreen: GamePlayScreen,
    ) {
        val button = addDialogButton(
            dialogLayout = dialog,
            onClick = {
                if (product != null) {
                    gamePlayScreen.onPackPurchaseButtonClicked(product)
                }
            },
            text = definition.label.format(definition.amount.toString().reversed()),
            dialogName = COINS_DIALOG_NAME
        )
        button.debug = DebugSettings.SHOW_UI_BORDERS
        val stack = Stack()
        button.add(stack).row()
        addLabelToPackButton(product, button)
        addFlashEffect(definition, stack, gameAssetManager)
        animatePackButton(definition, addPurchaseIcon(definition, stack, gameAssetManager))
    }

    private fun addLabelToPackButton(
        product: Product?,
        button: ImageTextButton
    ) {
        val label = GameLabel(
            product?.formattedPrice ?: "",
            Label.LabelStyle(
                assetsManager.getFont(FontsDefinitions.VARELA_35),
                Color.WHITE
            ),
            androidInterface
        )
        label.setAlignment(Align.center)
        button.add(
            label
        ).colspan(2).center()
    }

    private fun animatePackButton(
        definition: InAppProducts,
        image: Image
    ) {
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

    private fun addDialogButton(
        dialogLayout: Table,
        onClick: (() -> Unit)? = null,
        text: String,
        newRowAfter: Boolean = true,
        dialogName: String,
        topPadding: Float = DIALOG_BUTTON_PADDING,
        width: Float? = null
    ): ImageTextButton {
        val clickListener = onClick ?: { stage.closeDialog(dialogName) }
        val button = createDialogButton(stage, text.reversed(), clickListener)
        val cell = dialogLayout.add(button)
            .pad(topPadding, DIALOG_BUTTON_PADDING, DIALOG_BUTTON_PADDING, DIALOG_BUTTON_PADDING)
        width?.let { cell.width(it) }
        if (newRowAfter) {
            cell.row()
        }
        return button
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


    private fun createDialogButton(
        stage: GameStage,
        text: String,
        onClick: (() -> Unit)
    ): ImageTextButton {
        val button =
            ImageTextButton(text, stage.createNinePatchButtonStyle(assetsManager))
        addClickListenerToButton(button, onClick)
        return button
    }

    private fun addClickListenerToButton(
        button: Button,
        runnable: Runnable,
    ) {
        button.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                runnable.run()
                soundPlayer.playSound(
                    assetsManager.getSound(
                        SoundsDefinitions.BUTTON
                    )
                )
            }
        })
    }

    private fun addCoinsDialogComponents(
        layout: Table,
        products: Map<String, Product>,
        gamePlayScreen: GamePlayScreen,
        onVideoDone: () -> Unit,
    ) {
        addHeaderToDialog(assetsManager, layout, COINS_DIALOG_HEADER)
        addDialogText(assetsManager, layout, COINS_DIALOG_DESCRIPTION)
        InAppProducts.values().forEach {
            val id = it.name.lowercase(Locale.ROOT)
            if (products.containsKey(id)) {
                addPackButton(
                    dialog = layout,
                    product = products[id],
                    definition = it,
                    gameAssetManager = assetsManager,
                    gamePlayScreen = gamePlayScreen
                )
            }
        }
        addVideoSection(layout, onVideoDone, gamePlayScreen)
        placeDialogInTheMiddle(layout)
    }

    private fun addVideoSection(
        layout: Table,
        onVideoDone: () -> Unit,
        gamePlayScreen: GamePlayScreen,
    ) {
        val stack = Stack()
        val table = Table()
        addDialogText(
            assetsManager = assetsManager,
            dialog = table,
            description = COINS_DIALOG_DESCRIPTION_2,
            topPadding = COINS_DIALOG_DESCRIPTION_2_TOP_PADDING,
            bottomPadding = 0F
        )
        addVideoButton(table, onVideoDone, gamePlayScreen)
        table.isVisible = false
        stack.add(table)
        val loadingAnimation = createLoadingAnimation()
        stack.add(loadingAnimation)
        layout.add(stack)
        gamePlayScreen.onBuyCoinsDialogOpened {
            table.isVisible = true
            loadingAnimation.isVisible = false
        }
    }

    private fun addVideoButton(
        layout: Table,
        onAdCompleted: () -> Unit,
        gamePlayScreen: GamePlayScreen,
    ) {
        val button = addDialogButton(
            dialogLayout = layout,
            text = COINS_DIALOG_BUTTON_VIDEO,
            dialogName = COINS_DIALOG_NAME,
            topPadding = 0F,
            onClick = {
                gamePlayScreen.onShowVideoAdClicked({
                    onAdCompleted.invoke()
                    layout.isVisible = false
                }, {
                    layout.isVisible = false
                })
            }
        )
        val image = Image(assetsManager.getTexture(TexturesDefinitions.POPCORN))
        image.setScaling(Scaling.none)
        val cell = button.add(image).pad(COINS_DIALOG_BUTTON_VIDEO_ICON_PADDING)
        cell.size(cell.prefWidth, COINS_DIALOG_BUTTON_VIDEO_PADDING)
    }

    private fun createLoadingAnimation(): LoadingAnimation {
        val keyFrames = assetsManager.getAtlas(AtlasesDefinitions.LOADING).regions
        return LoadingAnimation(keyFrames)
    }

    private fun createDialogLayout(
        assetsManager: GameAssetManager,
        header: String,
        description: String
    ): Table {
        val layout = Table()
        addHeaderAndDescription(assetsManager, layout, header, description)
        return layout
    }

    private fun addHeaderAndDescription(
        assetsManager: GameAssetManager,
        layout: Table,
        header: String,
        description: String
    ) {
        addHeaderToDialog(assetsManager, layout, header, 2)
        addDialogText(assetsManager, layout, description, 2)
    }

    private fun addExitDialogButtons(
        gamePlayScreen: GamePlayScreen,
        layout: Table
    ) {
        val onClick = { gamePlayScreen.onQuitSession() }
        addDialogButton(
            dialogLayout = layout,
            onClick = onClick,
            text = EXIT_DIALOG_BUTTON_OK,
            newRowAfter = false,
            dialogName = EXIT_DIALOG_NAME,
            width = DIALOG_BUTTON_WIDTH
        )
        addDialogButton(
            dialogLayout = layout,
            text = EXIT_DIALOG_BUTTON_NO,
            dialogName = EXIT_DIALOG_NAME,
            width = DIALOG_BUTTON_WIDTH
        )
    }

    private fun placeDialogInTheMiddle(layout: Table) {
        if (layout.parent == null) return

        layout.pack()
        (layout.parent as Table).pack()
        (layout.parent as Table).setPosition(
            (layout.parent as Table).stage.width / 2F - (layout.parent as Table).prefWidth / 2F,
            (layout.parent as Table).stage.height / 2F - (layout.parent as Table).prefHeight / 2F
        )
    }

    private fun createCoinsPurchasedSuccessfullyDialog(
        assetsManager: GameAssetManager,
        amount: Int
    ): Table {
        val dialogView = createDialogLayout(
            assetsManager,
            COINS_PURCHASED_DIALOG_HEADER,
            COINS_PURCHASED_DIALOG_DESCRIPTION.format(amount.toString().reversed())
        )
        addDialogButton(
            dialogLayout = dialogView,
            text = COINS_PURCHASED_DIALOG_BUTTON_OK,
            dialogName = COINS_PURCHASED_DIALOG_NAME,
        )
        return dialogView
    }

    fun openHelpDialog() {
        val dialogView = createDialogLayout(
            assetsManager,
            HELP_DIALOG_HEADER,
            HELP_DIALOG_DESCRIPTION
        )
        addDialogText(assetsManager, dialogView, HELP_DIALOG_CREDITS)
        addDialogButton(
            dialogLayout = dialogView,
            text = DIALOG_BUTTON_OK,
            dialogName = HELP_DIALOG_NAME,
            width = DIALOG_BUTTON_WIDTH
        )
        finalizeDialog(dialogView, HELP_DIALOG_NAME)
    }

    private fun finalizeDialog(
        dialogView: Table,
        dialogName: String,
        onCloseButtonClick: (() -> Unit)? = null
    ) {
        stage.addDialog(
            dialogView,
            dialogName,
            assetsManager,
            onCloseButtonClick = onCloseButtonClick,
            null,
        )
        placeDialogInTheMiddle(dialogView)
    }

    fun openRevealWordDialog(onYes: () -> Unit, onNo: () -> Unit) {
        val dialogView = createDialogLayout(
            assetsManager,
            REVEAL_WORD_DIALOG_HEADER,
            REVEAL_WORD_DIALOG_DESCRIPTION.format(GameModel.DISPLAY_TARGET_COST)
        )
        addDialogButton(
            dialogLayout = dialogView,
            onClick = onYes,
            text = DIALOG_BUTTON_OK,
            dialogName = REVEAL_WORD_DIALOG_NAME,
            width = DIALOG_BUTTON_WIDTH,
            newRowAfter = false
        )
        addDialogButton(
            dialogLayout = dialogView,
            onClick = onNo,
            text = DIALOG_BUTTON_NO,
            dialogName = REVEAL_WORD_DIALOG_NAME,
            width = DIALOG_BUTTON_WIDTH
        )
        finalizeDialog(dialogView, REVEAL_WORD_DIALOG_NAME, onNo)
    }

    companion object {
        private const val COINS_PURCHASED_DIALOG_HEADER = "נהנים!"
        private const val COINS_PURCHASED_DIALOG_DESCRIPTION = "רכישה של %s מטבעות\nבוצעה בהצלחה!"
        private const val COINS_PURCHASED_DIALOG_NAME = "coins_purchased"
        private const val COINS_PURCHASED_DIALOG_BUTTON_OK = "מעולה"
        private const val EXIT_DIALOG_NAME = "exit"
        private const val EXIT_DIALOG_HEADER = "חכה!"
        private const val REVEAL_WORD_DIALOG_HEADER = "רוצה לדעת את המילה?"
        private const val REVEAL_WORD_DIALOG_DESCRIPTION = "ניתן להציג את המילה עבור %s מטבעות"
        private const val REVEAL_WORD_DIALOG_NAME = "help"
        private const val HELP_DIALOG_HEADER = "איך משחקים?"
        private const val HELP_DIALOG_DESCRIPTION =
            "בכל שלב תופיע מילה עם\nאותיות חסרות, עליכם לגלות את\nהמילה לפני שיגמרו מס' הניסיונות."
        private const val HELP_DIALOG_CREDITS =
            "גד וייסברג - תוכן, תכנות ועיצוב\nמעוז שחם - איסוף מילים עבור רמת ילדים\nכל הזכויות שמורות - 3202"
        private const val DIALOG_BUTTON_OK = "סבבה"
        private const val DIALOG_BUTTON_NO = "לא תודה"
        private const val HELP_DIALOG_NAME = "help"
        private const val EXIT_DIALOG_DESCRIPTION = "אתה בטוח שאתה רוצה\nלסיים את המשחק?"
        private const val EXIT_DIALOG_BUTTON_OK = "כן"
        private const val EXIT_DIALOG_BUTTON_NO = "לא"
        private const val DIALOG_BUTTON_WIDTH = 200F
        private const val DIALOG_HEADER_PADDING_BOTTOM = 64F
        private const val COINS_DIALOG_NAME = "coins"
        private const val COINS_DIALOG_LOADING_NAME = "loading_coins"
        private const val COINS_DIALOG_HEADER = "קבל עוד מטבעות"
        private const val COINS_DIALOG_DESCRIPTION =
            "לרשותך מס' אפשרויות להשיג\nעוד מטבעות.\nכל רכישה תסיר את הפרסומות למשך שבוע!"
        private const val COINS_DIALOG_DESCRIPTION_2 = "או צפייה בסרטון פרסומת עבור 4 מטבעות:"
        private const val COINS_DIALOG_DESCRIPTION_2_TOP_PADDING = 40F
        private const val COINS_DIALOG_BUTTON_VIDEO = "לחץ לצפייה בסרטון פרסומת"
        private const val COINS_DIALOG_BUTTON_VIDEO_PADDING = 128F
        private const val COINS_DIALOG_BUTTON_VIDEO_ICON_PADDING = 20F
        private const val DIALOG_DESCRIPTION_PADDING_BOTTOM = 64F
        private const val DIALOG_BUTTON_PADDING = 32F
        private const val FLASH_EFFECT_DURATION = 4F
    }


}
