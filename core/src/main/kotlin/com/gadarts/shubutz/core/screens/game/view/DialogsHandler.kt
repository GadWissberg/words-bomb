package com.gadarts.shubutz.core.screens.game.view

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.gadarts.shubutz.core.AndroidInterface
import com.gadarts.shubutz.core.GeneralUtils
import com.gadarts.shubutz.core.SoundPlayer
import com.gadarts.shubutz.core.model.assets.GameAssetManager
import com.gadarts.shubutz.core.model.assets.definitions.FontsDefinitions
import com.gadarts.shubutz.core.model.assets.definitions.SoundsDefinitions
import com.gadarts.shubutz.core.screens.game.GamePlayScreen
import com.gadarts.shubutz.core.screens.menu.view.stage.GameStage

class DialogsHandler(
    private val assetsManager: GameAssetManager,
    private val stage: GameStage,
    private val soundPlayer: SoundPlayer,
    private val androidInterface: AndroidInterface
) {

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
        val text =
            GameLabel(GeneralUtils.fixHebrewDescription(description), style, androidInterface)
        text.setAlignment(Align.right)
        dialog.add(text).pad(topPadding, 0F, bottomPadding, 0F).colspan(colSpan)
            .row()
    }


    private fun addHeaderToDialog(
        assetsManager: GameAssetManager,
        popup: Table,
        text: CharSequence,
        colSpan: Int = 1
    ) {
        val font = assetsManager.getFont(FontsDefinitions.VARELA_80)
        val headerStyle = Label.LabelStyle(font, Color.WHITE)
        popup.add(Label(text.reversed(), headerStyle))
            .pad(0F, 0F, DIALOG_HEADER_PADDING_BOTTOM, 0F)
            .colspan(colSpan)
            .row()
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

    private fun createDialogLayout(
        assetsManager: GameAssetManager,
        header: CharSequence,
        description: String?
    ): Table {
        val layout = Table()
        addHeaderAndDescription(assetsManager, layout, header, description)
        return layout
    }

    private fun addHeaderAndDescription(
        assetsManager: GameAssetManager,
        layout: Table,
        header: CharSequence,
        description: String?
    ) {
        addHeaderToDialog(assetsManager, layout, header, 2)
        if (description != null) {
            addDialogText(assetsManager, layout, description, 2)
        }
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

    fun openDialog(
        header: CharSequence,
        description: String? = null,
        onYes: () -> Unit,
        onNo: () -> Unit,
        okButtonText: String = DIALOG_BUTTON_YES,
        noButtonText: String = DIALOG_BUTTON_NO,
        dialogName: String,
    ) {
        val dialogView = createDialogLayout(
            assetsManager,
            header,
            description
        )
        addDialogButton(
            dialogLayout = dialogView,
            onClick = onYes,
            text = okButtonText,
            dialogName = dialogName,
            width = DIALOG_BUTTON_WIDTH,
            newRowAfter = false
        )
        addDialogButton(
            dialogLayout = dialogView,
            onClick = onNo,
            text = noButtonText,
            dialogName = dialogName,
            width = DIALOG_BUTTON_WIDTH
        )
        finalizeDialog(dialogView, dialogName, onNo)
    }

    companion object {
        private const val COINS_PURCHASED_DIALOG_HEADER = "נהנים!"
        private const val COINS_PURCHASED_DIALOG_DESCRIPTION = "רכישה של %s מטבעות\nבוצעה בהצלחה!"
        private const val COINS_PURCHASED_DIALOG_NAME = "coins_purchased"
        private const val COINS_PURCHASED_DIALOG_BUTTON_OK = "מעולה"
        private const val EXIT_DIALOG_NAME = "exit"
        private const val EXIT_DIALOG_HEADER = "חכה!"
        private const val HELP_DIALOG_HEADER = "איך משחקים?"
        private const val HELP_DIALOG_DESCRIPTION =
            "בכל שלב תופיע מילה עם\nאותיות חסרות, עליכם לגלות את\nהמילה לפני שיגמרו מס' הניסיונות."
        private const val HELP_DIALOG_CREDITS =
            "גד וייסברג - תוכן, תכנות ועיצוב\nמעוז שחם - איסוף מילים עבור רמת ילדים\nכל הזכויות שמורות - 3202"
        private const val DIALOG_BUTTON_OK = "סבבה"
        private const val HELP_DIALOG_NAME = "help"
        private const val EXIT_DIALOG_DESCRIPTION = "אתה בטוח שאתה רוצה\nלסיים את המשחק?"
        private const val EXIT_DIALOG_BUTTON_OK = "כן"
        private const val EXIT_DIALOG_BUTTON_NO = "לא"
        private const val DIALOG_BUTTON_WIDTH = 200F
        private const val DIALOG_HEADER_PADDING_BOTTOM = 64F
        private const val COINS_DIALOG_NAME = "coins"
        private const val DIALOG_DESCRIPTION_PADDING_BOTTOM = 64F
        private const val DIALOG_BUTTON_PADDING = 32F
        private const val DIALOG_BUTTON_NO = "לא תודה"
        private const val DIALOG_BUTTON_YES = "סבבה"
    }


}
