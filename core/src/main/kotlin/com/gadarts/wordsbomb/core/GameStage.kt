package com.gadarts.wordsbomb.core

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton.ImageTextButtonStyle
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.viewport.ScalingViewport
import com.gadarts.wordsbomb.core.model.assets.FontsDefinitions
import com.gadarts.wordsbomb.core.model.assets.GameAssetManager
import com.gadarts.wordsbomb.core.model.assets.TexturesDefinitions
import com.gadarts.wordsbomb.core.model.assets.TexturesDefinitions.*
import com.gadarts.wordsbomb.core.screens.menu.view.MenuScreenView
import com.gadarts.wordsbomb.core.view.hud.EventsSubscriber

/**
 * Common code for the stages.
 */
abstract class GameStage<T>(
    viewport: ScalingViewport,
    spriteBatch: SpriteBatch,
    assetsManager: GameAssetManager
) :
    Notifier<T>, Stage(viewport, spriteBatch) where T : EventsSubscriber {

    private var font: BitmapFont
    private lateinit var dialogButtonDown: NinePatchDrawable
    private lateinit var dialogButtonUp: NinePatchDrawable
    private lateinit var dialogBackground: NinePatchDrawable
    private var currentDialog: Table? = null

    init {
        createDialogNinePatches(assetsManager)
        font = assetsManager.getFont(FontsDefinitions.VARELA_35)
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
        topPadding: Float
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
            topPadding = topPadding
        )
        return button
    }

    fun addButtonToTable(
        button: Button,
        onClick: ClickListener,
        uiTable: Table,
        newLineAfter: Boolean = true,
        size: Float = 0F,
        span: Int = 1,
        topPadding: Float
    ) {
        button.addListener(onClick)
        val add = uiTable.add(button).pad(
            topPadding,
            MenuScreenView.BUTTON_PADDING,
            MenuScreenView.BUTTON_PADDING,
            MenuScreenView.BUTTON_PADDING
        ).colspan(span)
        if (size > 0) {
            add.size(size)
        }
        if (newLineAfter) {
            uiTable.row()
        }
    }

    private fun createDialogNinePatches(am: GameAssetManager) {
        val bgNinePatch = NinePatch(am.getTexture(POPUP), DLG_PAD, DLG_PAD, DLG_PAD, DLG_PAD)
        dialogBackground = NinePatchDrawable(bgNinePatch)
        dialogButtonUp = NinePatchDrawable(createDialogButtonNinePatch(am, POPUP_BUTTON_UP))
        dialogButtonDown = NinePatchDrawable(createDialogButtonNinePatch(am, POPUP_BUTTON_DOWN))
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

    /**
     * Displays a dialog.
     */
    fun showDialog(
        message: String,
        mainButtonEvent: ClickListener? = null,
        mainButtonText: String = DIALOG_BUTTON_OK_TEXT,
        secondaryButtonText: String? = null,
        secondaryButtonEvent: ClickListener? = null
    ) {
        if (currentDialog != null) return
        createDialog()
        val label = Label(message.reversed(), Label.LabelStyle(font, Color.WHITE))
        label.wrap = true
        label.width = currentDialog!!.width
        currentDialog!!.add(label).width(DIALOG_WIDTH * 2F / 3F).colspan(2).row()
        currentDialog!!.setPosition(
            Gdx.graphics.width / 2F - currentDialog!!.width / 2F,
            Gdx.graphics.height / 2F - currentDialog!!.height / 2F
        )
        addButtonToDialog(mainButtonText, mainButtonEvent)
        addSecondaryButtonToDialog(secondaryButtonText, secondaryButtonEvent)
        applyDialogIntroEffect()
    }

    private fun addSecondaryButtonToDialog(
        secondaryButtonText: String?,
        secondaryButtonEvent: ClickListener?
    ) {
        if (secondaryButtonText != null) {
            addButtonToDialog(secondaryButtonText, secondaryButtonEvent)
        }
    }

    private fun addButtonToDialog(buttonText: String, buttonEvent: ClickListener?) {
        val buttonStyle = ImageTextButtonStyle(dialogButtonUp, dialogButtonDown, null, font)
        val imageButton = ImageTextButton(buttonText.reversed(), buttonStyle)
        currentDialog!!.add(imageButton).pad(DLG_BUT_PAD, DLG_BUT_PAD, DLG_BUT_PAD, DLG_BUT_PAD)
        if (buttonEvent != null) {
            imageButton.addListener(buttonEvent)
        }
        addDefaultButtonEvent(imageButton)
    }

    private fun addDefaultButtonEvent(imageButton: ImageTextButton) {
        imageButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                if (currentDialog != null) {
                    if (!currentDialog!!.hasActions()) {
                        currentDialog!!.addAction(
                            Actions.sequence(
                                Actions.fadeOut(
                                    DIALOG_FADE_DURATION,
                                    Interpolation.circle
                                ), RemoveMeAction()
                            )
                        )
                    }
                }
                currentDialog = null
            }
        })
    }

    private fun createDialog() {
        currentDialog = Table()
        currentDialog!!.debug = DefaultGameSettings.SHOW_UI_BORDERS
        currentDialog!!.setSize(DIALOG_WIDTH, DIALOG_MIN_HEIGHT)
        currentDialog!!.background(dialogBackground)
        addActor(currentDialog)
    }

    private fun applyDialogIntroEffect() {
        currentDialog?.addAction(
            Actions.sequence(
                Actions.alpha(0F),
                Actions.fadeIn(DIALOG_FADE_DURATION, Interpolation.exp10)
            )
        )
    }

    /**
     * Used to remove dialog at the end of the actions sequence.
     */
    private class RemoveMeAction : Action() {
        override fun act(delta: Float): Boolean {
            actor.remove()
            return true
        }

    }

    companion object {
        private const val DIALOG_WIDTH = 720F
        private const val DIALOG_MIN_HEIGHT = 480F
        private const val DLG_PAD = 100
        private const val DLG_BUT_NP_PAD = 25
        private const val DLG_BUT_PAD = 50F
        private const val DIALOG_FADE_DURATION = 1F
        private const val DIALOG_BUTTON_OK_TEXT = "סבבה"
        private val BUTTON_FONT_COLOR_DISABLED = Color.LIGHT_GRAY
    }

}

