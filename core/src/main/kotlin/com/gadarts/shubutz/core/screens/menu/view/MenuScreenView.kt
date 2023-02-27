package com.gadarts.shubutz.core.screens.menu.view

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Disposable
import com.gadarts.shubutz.core.DebugSettings
import com.gadarts.shubutz.core.Notifier
import com.gadarts.shubutz.core.SoundPlayer
import com.gadarts.shubutz.core.model.Difficulties
import com.gadarts.shubutz.core.model.assets.definitions.FontsDefinitions
import com.gadarts.shubutz.core.model.assets.GameAssetManager
import com.gadarts.shubutz.core.model.assets.definitions.SoundsDefinitions
import com.gadarts.shubutz.core.model.assets.definitions.TexturesDefinitions
import com.gadarts.shubutz.core.screens.menu.BeginGameAction
import com.gadarts.shubutz.core.screens.menu.LoadingAnimationHandler
import com.gadarts.shubutz.core.screens.menu.view.stage.GameStage
import com.gadarts.shubutz.core.screens.menu.view.stage.GameStage.Companion.BUTTON_PADDING

/**
 * Handles the menu's display.
 */
class MenuScreenView(
    private val assetsManager: GameAssetManager,
    private val versionName: String,
    private val stage: GameStage,
    private val soundPlayer: SoundPlayer
) : Disposable, Notifier<MenuScreenViewEventsSubscriber> {


    private lateinit var logoTable: Table
    private lateinit var versionLabel: Label
    private var mainMenuTable = Table()
    private var difficultySelectionTable = Table()
    private var loadingAnimationRenderer = LoadingAnimationHandler()
    override val subscribers = HashSet<MenuScreenViewEventsSubscriber>()

    /**
     * Adding loading animation.
     */
    fun onShow(loadingDone: Boolean, goToPlayScreenOnClick: BeginGameAction) {
        if (!loadingDone) {
            loadingAnimationRenderer.addLoadingAnimation(assetsManager, stage)
        } else {
            finishLoadingAnimationAndDisplayMenu(goToPlayScreenOnClick)
        }
    }

    /**
     * Renders the stage and the loading animation.
     */
    fun render(delta: Float) {
        stage.act(delta)
        stage.draw()
        loadingAnimationRenderer.render(subscribers)
    }

    /**
     * Handles loading animation finish and calls to add an interface.
     */
    fun finishLoadingAnimationAndDisplayMenu(beginGameAction: BeginGameAction) {
        loadingAnimationRenderer.flyOutBricks(
            assetsManager.getSound(SoundsDefinitions.FLYBY),
            soundPlayer
        )
        Gdx.app.postRunnable {
            addUserInterface(beginGameAction)
            stage.addActor(mainMenuTable)
            difficultySelectionTable.isVisible = false
            stage.addActor(difficultySelectionTable)
        }
    }

    private fun addUserInterface(beginGameAction: BeginGameAction) {
        addMainMenuTable()
        addDifficultySelectionTable(beginGameAction)
        versionLabel = Label(
            "v$versionName",
            Label.LabelStyle(assetsManager.getFont(FontsDefinitions.VARELA_35), Color.BLACK)
        )
        stage.addActor(versionLabel)
    }

    private fun addDifficultySelectionTable(beginGameAction: BeginGameAction) {
        initMenuTable(difficultySelectionTable)
        addDifficultySelectionLabel()
        Difficulties.values().forEach {
            addButton(
                difficultySelectionTable,
                { beginGameAction.begin(it) },
                it.displayName,
            )
        }
        addBackButton()
    }

    private fun addBackButton() {
        addButton(
            difficultySelectionTable,
            {
                difficultySelectionTable.isVisible = false
                mainMenuTable.isVisible = true
            },
            LABEL_BACK,
            160,
            assetsManager.getFont(FontsDefinitions.VARELA_40),
            scale = 0.5F
        )
    }

    private fun addDifficultySelectionLabel() {
        difficultySelectionTable.add(
            Label(
                LABEL_DIFFICULTY_SELECT.reversed(),
                Label.LabelStyle(assetsManager.getFont(FontsDefinitions.VARELA_80), Color.WHITE)
            )
        ).pad(20F).row()
    }

    private fun addMainMenuTable() {
        initMenuTable(mainMenuTable)
        addLogo()
        addButton(mainMenuTable, {
            mainMenuTable.isVisible = false
            difficultySelectionTable.isVisible = true
        }, LABEL_BEGIN_GAME, font = assetsManager.getFont(FontsDefinitions.VARELA_80))
    }

    private fun initMenuTable(table: Table) {
        table.debug = DebugSettings.SHOW_UI_BORDERS
        table.setFillParent(true)
        table.touchable = Touchable.childrenOnly
    }

    private fun addButton(
        table: Table,
        onClick: Runnable,
        label: String,
        topPadding: Int = BUTTON_PADDING,
        font: BitmapFont = assetsManager.getFont(FontsDefinitions.VARELA_80),
        scale: Float = 1F
    ) {
        stage.addButton(
            table,
            object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    super.clicked(event, x, y)
                    onClick.run()
                    soundPlayer.playSound(assetsManager.getSound(SoundsDefinitions.BUTTON))
                }
            },
            label.reversed(),
            span = 2,
            up = assetsManager.getTexture(TexturesDefinitions.BUTTON_UP),
            down = assetsManager.getTexture(TexturesDefinitions.BUTTON_DOWN),
            bitmapFont = font,
            topPadding = topPadding,
            scale = scale
        )
    }

    private fun addLogoLetter(
        logoTable: Table,
        index: Int,
        textureDefinition: TexturesDefinitions
    ) {
        val texture = assetsManager.getTexture(textureDefinition)
        val image = Image(texture)
        logoTable.add(image).size(
            texture.width.toFloat(),
            texture.height.toFloat()
        )
        addLogoLetterAnimation(image, index)
    }

    private fun addLogoLetterAnimation(letterActor: Image, index: Int) {
        letterActor.addAction(
            Actions.sequence(
                Actions.scaleTo(0F, 0F),
                Actions.delay(0.2F * index),
                Actions.run {
                    soundPlayer.playSound(assetsManager.getSound(SoundsDefinitions.BUBBLE), true)
                },
                Actions.scaleTo(1F, 1F, 0.5F, Interpolation.exp10),
                Actions.forever(
                    Actions.sequence(
                        Actions.moveBy(0F, 40F, MathUtils.random(3F, 6F), Interpolation.bounceIn),
                        Actions.moveBy(0F, -80F, MathUtils.random(3F, 6F), Interpolation.bounce),
                        Actions.moveBy(0F, 40F, MathUtils.random(3F, 6F), Interpolation.exp10)
                    )
                )
            )
        )
    }

    private fun addLogo() {
        val logoTable = addLogoTable()
        addLogoLetter(logoTable, 0, TexturesDefinitions.LOGO_LAST)
        addLogoLetter(logoTable, 1, TexturesDefinitions.LOGO_VAV2)
        addLogoLetter(logoTable, 2, TexturesDefinitions.LOGO_BET)
        addLogoLetter(logoTable, 3, TexturesDefinitions.LOGO_VAV1)
        addLogoLetter(logoTable, 4, TexturesDefinitions.LOGO_SHIN)
    }

    private fun addLogoTable(): Table {
        logoTable = Table()
        logoTable.debug(if (DebugSettings.SHOW_UI_BORDERS) Table.Debug.all else Table.Debug.none)
        mainMenuTable.add(logoTable).pad(
            LOGO_PADDING_TOP, 0F,
            LOGO_PADDING_BOTTOM, 0F
        ).colspan(2).row()
        return logoTable
    }

    override fun dispose() {

    }

    fun clearScreen() {
        mainMenuTable.remove()
        difficultySelectionTable.remove()
        versionLabel.remove()
    }

    /**
     * Represents the loading animation.
     */
    class BrickAnimation : Table() {
        var ready: Boolean = false
    }

    companion object {
        private const val LABEL_BEGIN_GAME = "התחל משחק"
        private const val LABEL_DIFFICULTY_SELECT = "בחר רמת קושי:"
        private const val LABEL_BACK = "חזרה"
        private const val LOGO_PADDING_TOP = 300F
        private const val LOGO_PADDING_BOTTOM = 75F
    }

}