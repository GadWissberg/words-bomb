package com.gadarts.shubutz.core.screens.menu.view

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.gadarts.shubutz.core.AndroidInterface
import com.gadarts.shubutz.core.model.Difficulties
import com.gadarts.shubutz.core.model.assets.SharedPreferencesKeys
import com.gadarts.shubutz.core.model.assets.definitions.FontsDefinitions
import com.gadarts.shubutz.core.model.assets.definitions.SoundsDefinitions
import com.gadarts.shubutz.core.model.assets.definitions.TexturesDefinitions
import com.gadarts.shubutz.core.screens.game.GlobalHandlers
import com.gadarts.shubutz.core.screens.menu.BeginGameAction
import com.gadarts.shubutz.core.screens.menu.view.stage.GameStage

class MainMenuScreenButtons(
    private val mainMenuTable: Table,
    private val difficultySelectionTable: Table,
    private val globalHandlers: GlobalHandlers,
    private val stage: GameStage
) {

    private lateinit var soundButton: ImageButton
    private lateinit var helpButton: ImageButton

    private fun addRoundButton(
        stage: GameStage,
        up: TexturesDefinitions,
        x: Float,
        y: Float,
        clickListener: ClickListener,
        checked: TexturesDefinitions? = null,
    ): ImageButton {
        val imageButton = createButton(up, checked)
        imageButton.setPosition(x, y)
        imageButton.addListener(clickListener)
        stage.addActor(imageButton)
        return imageButton
    }

    private fun soundToggle(globalHandlers: GlobalHandlers, androidInterface: AndroidInterface) {
        globalHandlers.soundPlayer.enabled = !globalHandlers.soundPlayer.enabled
        globalHandlers.soundPlayer.playSound(globalHandlers.assetsManager.getSound(SoundsDefinitions.BUTTON))
        androidInterface.saveSharedPreferencesBooleanValue(
            SharedPreferencesKeys.SOUND_ENABLED,
            globalHandlers.soundPlayer.enabled
        )
    }

    private fun createButton(
        imageUp: TexturesDefinitions,
        imageChecked: TexturesDefinitions? = null
    ): ImageButton {
        val texture = globalHandlers.assetsManager.getTexture(TexturesDefinitions.BUTTON_CIRCLE_UP)
        val style = ImageButton.ImageButtonStyle(
            TextureRegionDrawable(texture),
            TextureRegionDrawable(globalHandlers.assetsManager.getTexture(TexturesDefinitions.BUTTON_CIRCLE_DOWN)),
            null,
            TextureRegionDrawable(globalHandlers.assetsManager.getTexture(imageUp)),
            null,
            if (imageChecked != null) TextureRegionDrawable(
                globalHandlers.assetsManager.getTexture(
                    imageChecked
                )
            ) else null,
        )
        return ImageButton(style)
    }

    private fun fadeInTable(table: Table) {
        table.addAction(
            Actions.sequence(
                Actions.alpha(0F),
                Actions.run { table.isVisible = true },
                Actions.fadeIn(FADE_ANIMATION_DURATION)
            )
        )
    }

    private fun fadeOutTable(table: Table) {
        table.addAction(
            Actions.sequence(
                Actions.fadeOut(FADE_ANIMATION_DURATION),
                Actions.run { table.isVisible = false })
        )
    }

    private fun addButton(
        table: Table,
        label: String?,
        topPadding: Int = GameStage.BUTTON_PADDING,
        font: BitmapFont = globalHandlers.assetsManager.getFont(FontsDefinitions.VARELA_80),
        scale: Float = 1F,
        image: Texture? = null,
        onClick: Runnable
    ) {
        stage.addButton(
            table,
            object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    super.clicked(event, x, y)
                    onClick.run()
                    globalHandlers.soundPlayer.playSound(
                        globalHandlers.assetsManager.getSound(
                            SoundsDefinitions.BUTTON
                        )
                    )
                }
            },
            label?.reversed(),
            span = 2,
            up = globalHandlers.assetsManager.getTexture(TexturesDefinitions.BUTTON_UP),
            down = globalHandlers.assetsManager.getTexture(TexturesDefinitions.BUTTON_DOWN),
            bitmapFont = font,
            topPadding = topPadding,
            scale = scale,
            image = image
        )
    }

    private fun addSoundButton(
        stage: GameStage,
        globalHandlers: GlobalHandlers,
        androidInterface: AndroidInterface
    ) {
        val imageButton = addRoundButton(
            stage,
            TexturesDefinitions.ICON_SOUND_OFF,
            stage.width - globalHandlers.assetsManager.getTexture(TexturesDefinitions.BUTTON_CIRCLE_UP).width - ROUND_BUTTON_PADDING_HOR,
            ROUND_BUTTON_PADDING_VER,
            object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    super.clicked(event, x, y)
                    soundToggle(globalHandlers, androidInterface)
                }
            },
            TexturesDefinitions.ICON_SOUND_ON,
        )
        imageButton.isChecked = globalHandlers.soundPlayer.enabled
        soundButton = imageButton
    }

    private fun addHelpButton(stage: GameStage, globalHandlers: GlobalHandlers) {
        helpButton = addRoundButton(stage,
            TexturesDefinitions.ICON_HELP,
            ROUND_BUTTON_PADDING_HOR,
            ROUND_BUTTON_PADDING_VER,
            object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    super.clicked(event, x, y)
                    globalHandlers.dialogsHandler.openHelpDialog()
                }
            })
    }

    fun clear() {
        soundButton.remove()
        helpButton.remove()
    }


    private fun addBackButton() {
        addButton(
            difficultySelectionTable,
            LABEL_BACK,
            160,
            globalHandlers.assetsManager.getFont(FontsDefinitions.VARELA_40),
            scale = 0.5F
        ) {
            fadeOutTable(difficultySelectionTable)
            fadeInTable(mainMenuTable)
        }
    }


    fun onLoadingAnimationDone(
        stage: GameStage,
        globalHandlers: GlobalHandlers,
        androidInterface: AndroidInterface
    ) {
        addSoundButton(stage, globalHandlers, androidInterface)
        addHelpButton(stage, globalHandlers)
    }

    fun fillMainMenuTable(beginGameAction: BeginGameAction) {
        addButton(
            table = mainMenuTable,
            label = LABEL_BEGIN_GAME,
            font = globalHandlers.assetsManager.getFont(FontsDefinitions.VARELA_80)
        ) {
            fadeOutTable(mainMenuTable)
            fadeInTable(difficultySelectionTable)
        }
        addButton(
            table = mainMenuTable,
            label = null,
            font = globalHandlers.assetsManager.getFont(FontsDefinitions.VARELA_80),
            image = globalHandlers.assetsManager.getTexture(TexturesDefinitions.KIDS)
        ) {
            beginGameAction.begin(Difficulties.KIDS)
        }
    }

    fun fillDifficultySelectionTable(beginGameAction: BeginGameAction) {
        Difficulties.values().filter { it != Difficulties.KIDS }.forEach {
            addButton(
                difficultySelectionTable,
                it.displayName,
            ) { beginGameAction.begin(it) }
        }
        addBackButton()
    }

    companion object {
        private const val ROUND_BUTTON_PADDING_HOR = 30F
        private const val ROUND_BUTTON_PADDING_VER = 60F
        private const val FADE_ANIMATION_DURATION = 0.3F
        private const val LABEL_BACK = "חזרה"
        private const val LABEL_BEGIN_GAME = "התחל משחק"
    }
}
