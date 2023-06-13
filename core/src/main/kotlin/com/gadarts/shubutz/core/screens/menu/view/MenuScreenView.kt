package com.gadarts.shubutz.core.screens.menu.view

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Disposable
import com.gadarts.shubutz.core.AndroidInterface
import com.gadarts.shubutz.core.DebugSettings
import com.gadarts.shubutz.core.model.Difficulties
import com.gadarts.shubutz.core.model.assets.SharedPreferencesKeys.SOUND_ENABLED
import com.gadarts.shubutz.core.model.assets.definitions.AtlasesDefinitions
import com.gadarts.shubutz.core.model.assets.definitions.FontsDefinitions
import com.gadarts.shubutz.core.model.assets.definitions.SoundsDefinitions
import com.gadarts.shubutz.core.model.assets.definitions.TexturesDefinitions
import com.gadarts.shubutz.core.screens.game.GlobalHandlers
import com.gadarts.shubutz.core.screens.game.view.DialogsHandler
import com.gadarts.shubutz.core.screens.menu.BeginGameAction
import com.gadarts.shubutz.core.screens.menu.LoadingAnimationHandler
import com.gadarts.shubutz.core.screens.menu.MenuScreen
import com.gadarts.shubutz.core.screens.menu.view.stage.GameStage
import com.gadarts.shubutz.core.screens.menu.view.stage.GameStage.Companion.BUTTON_PADDING

class MenuScreenView(
    private val globalHandlers: GlobalHandlers,
    private val versionName: String,
    private val stage: GameStage,
    private val menuScreen: MenuScreen,
    private val androidInterface: AndroidInterface,
    private val dialogsHandler: DialogsHandler
) : Disposable {


    private lateinit var soundButton: ImageButton
    private lateinit var helpButton: ImageButton
    private lateinit var logoTable: Table
    private lateinit var versionLabel: Label
    private var mainMenuTable = Table()
    private var difficultySelectionTable = Table()
    var loadingAnimationRenderer = LoadingAnimationHandler()

    private fun addSoundButton() {
        val imageButton = addRoundButton(
            TexturesDefinitions.ICON_SOUND_OFF,
            stage.width - globalHandlers.assetsManager.getTexture(TexturesDefinitions.BUTTON_CIRCLE_UP).width - ROUND_BUTTON_PADDING_HOR,
            ROUND_BUTTON_PADDING_VER,
            object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    super.clicked(event, x, y)
                    soundToggle()
                }
            },
            TexturesDefinitions.ICON_SOUND_ON,
        )
        imageButton.isChecked = globalHandlers.soundPlayer.enabled
        soundButton = imageButton
    }

    private fun addRoundButton(
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

    private fun soundToggle() {
        globalHandlers.soundPlayer.enabled = !globalHandlers.soundPlayer.enabled
        globalHandlers.soundPlayer.playSound(globalHandlers.assetsManager.getSound(SoundsDefinitions.BUTTON))
        androidInterface.saveSharedPreferencesBooleanValue(
            SOUND_ENABLED,
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

    fun onShow(loadingDone: Boolean, goToPlayScreenOnClick: BeginGameAction) {
        if (!loadingDone) {
            loadingAnimationRenderer.addLoadingAnimation(globalHandlers.assetsManager, stage)
        } else {
            finishLoadingAnimationAndDisplayMenu(goToPlayScreenOnClick)
        }
    }

    fun render(delta: Float) {
        stage.act(delta)
        stage.draw()
        loadingAnimationRenderer.render(menuScreen)
    }

    fun finishLoadingAnimationAndDisplayMenu(beginGameAction: BeginGameAction) {
        loadingAnimationRenderer.flyOutBricks(
            globalHandlers.assetsManager.getSound(SoundsDefinitions.FLYBY),
            globalHandlers.soundPlayer
        )
        Gdx.app.postRunnable {
            addUserInterface(beginGameAction)
            stage.addActor(mainMenuTable)
            difficultySelectionTable.isVisible = false
            stage.addActor(difficultySelectionTable)
        }
    }

    private fun addHelpButton() {
        helpButton = addRoundButton(TexturesDefinitions.ICON_HELP, ROUND_BUTTON_PADDING_HOR,
            ROUND_BUTTON_PADDING_VER, object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    super.clicked(event, x, y)
                    dialogsHandler.openHelpDialog(stage, globalHandlers.assetsManager)
                }
            })
    }

    override fun dispose() {

    }

    fun clearScreen() {
        mainMenuTable.clear()
        mainMenuTable.remove()
        difficultySelectionTable.remove()
        versionLabel.remove()
        soundButton.remove()
        helpButton.remove()
    }

    private fun addUserInterface(beginGameAction: BeginGameAction) {
        addSoundButton()
        addHelpButton()
        addMainMenuTable(beginGameAction)
        addDifficultySelectionTable(beginGameAction)
        versionLabel = Label(
            "v$versionName",
            Label.LabelStyle(
                globalHandlers.assetsManager.getFont(FontsDefinitions.VARELA_35),
                Color.BLACK
            )
        )
        stage.addActor(versionLabel)
    }

    private fun addDifficultySelectionTable(beginGameAction: BeginGameAction) {
        initMenuTable(difficultySelectionTable)
        addDifficultySelectionLabel()
        Difficulties.values().filter { it != Difficulties.KIDS }.forEach {
            addButton(
                difficultySelectionTable,
                it.displayName,
            ) { beginGameAction.begin(it) }
        }
        addBackButton()
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

    private fun addDifficultySelectionLabel() {
        difficultySelectionTable.add(
            Label(
                LABEL_DIFFICULTY_SELECT.reversed(),
                Label.LabelStyle(
                    globalHandlers.assetsManager.getFont(FontsDefinitions.VARELA_80),
                    Color.WHITE
                )
            )
        ).pad(20F).row()
    }

    private fun addMainMenuTable(beginGameAction: BeginGameAction) {
        initMenuTable(mainMenuTable)
        addLogo()
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
        addChampionsView()
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

    private fun addChampionsView() {
        if (!androidInterface.isConnected()) return
        mainMenuTable.add(
            ChampionsView(
                globalHandlers.assetsManager.getFont(FontsDefinitions.VARELA_35),
                androidInterface,
                globalHandlers.assetsManager.getTexture(TexturesDefinitions.ICON_HIGHSCORES),
                globalHandlers.assetsManager.getAtlas(AtlasesDefinitions.LOADING)
            )
        ).pad(CHAMPIONS_VIEW_PADDING).expandX().center()
    }

    private fun initMenuTable(table: Table) {
        table.debug = DebugSettings.SHOW_UI_BORDERS
        table.setFillParent(true)
        table.touchable = Touchable.childrenOnly
    }

    private fun addButton(
        table: Table,
        label: String?,
        topPadding: Int = BUTTON_PADDING,
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

    private fun addLogoLetter(
        logoTable: Table,
        index: Int,
        textureDefinition: TexturesDefinitions
    ) {
        val texture = globalHandlers.assetsManager.getTexture(textureDefinition)
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
                    globalHandlers.soundPlayer.playSound(
                        globalHandlers.assetsManager.getSound(
                            SoundsDefinitions.BUBBLE
                        ), true
                    )
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

    class BrickAnimation : Table() {
        var ready: Boolean = false
    }

    companion object {
        private const val LABEL_BEGIN_GAME = "התחל משחק"
        private const val LABEL_DIFFICULTY_SELECT = "בחר רמת קושי:"
        private const val LABEL_BACK = "חזרה"
        private const val LOGO_PADDING_TOP = 300F
        private const val LOGO_PADDING_BOTTOM = 75F
        private const val ROUND_BUTTON_PADDING_HOR = 30F
        private const val ROUND_BUTTON_PADDING_VER = 60F
        private const val CHAMPIONS_VIEW_PADDING = 64F
        private const val FADE_ANIMATION_DURATION = 0.3F
    }

}