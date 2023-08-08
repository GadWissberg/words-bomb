package com.gadarts.shubutz.core.screens.menu.view

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldClickListener
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.Timer
import com.badlogic.gdx.utils.Timer.Task
import com.gadarts.shubutz.core.DebugSettings
import com.gadarts.shubutz.core.GeneralUtils
import com.gadarts.shubutz.core.model.GameModes
import com.gadarts.shubutz.core.model.assets.SharedPreferencesKeys
import com.gadarts.shubutz.core.model.assets.definitions.AtlasesDefinitions
import com.gadarts.shubutz.core.model.assets.definitions.FontsDefinitions
import com.gadarts.shubutz.core.model.assets.definitions.SoundsDefinitions
import com.gadarts.shubutz.core.model.assets.definitions.TexturesDefinitions
import com.gadarts.shubutz.core.screens.game.GlobalHandlers
import com.gadarts.shubutz.core.screens.game.view.GameLabel
import com.gadarts.shubutz.core.screens.menu.BeginGameAction
import com.gadarts.shubutz.core.screens.menu.LoadingAnimationHandler
import com.gadarts.shubutz.core.screens.menu.MenuScreen
import com.gadarts.shubutz.core.screens.menu.view.stage.GameStage

class MenuScreenView(
    private val globalHandlers: GlobalHandlers,
    private val versionName: String,
    private val stage: GameStage,
    private val menuScreen: MenuScreen,
) : Disposable {

    private var mainMenuTable = Table()
    private var difficultySelectionTable = Table()
    private var gptTable = Table()
    private lateinit var logoTable: Table
    private var versionLabel: GameLabel? = null
    var loadingAnimationRenderer = LoadingAnimationHandler(globalHandlers.androidInterface)

    private val regularGameModes = listOf(
        GameModes.BEGINNER,
        GameModes.INTERMEDIATE,
        GameModes.ADVANCED,
        GameModes.EXPERT
    )
    private var soundButton: ImageButton? = null
    private var helpButton: ImageButton? = null
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
        globalHandlers.stage.addActor(imageButton)
        return imageButton
    }

    private fun soundToggle(globalHandlers: GlobalHandlers) {
        globalHandlers.soundPlayer.enabled = !globalHandlers.soundPlayer.enabled
        globalHandlers.soundPlayer.playSound(globalHandlers.assetsManager.getSound(SoundsDefinitions.BUTTON))
        globalHandlers.androidInterface.saveSharedPreferencesBooleanValue(
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
        label: String? = null,
        topPadding: Int = GameStage.BUTTON_PADDING,
        font: BitmapFont = globalHandlers.assetsManager.getFont(FontsDefinitions.VARELA_80),
        scale: Float = 1F,
        image: Texture? = null,
        onClick: Runnable
    ) {
        globalHandlers.stage.addButton(
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

    private fun addSoundButton() {
        val imageButton = addRoundButton(
            TexturesDefinitions.ICON_SOUND_OFF,
            globalHandlers.stage.width - globalHandlers.assetsManager.getTexture(TexturesDefinitions.BUTTON_CIRCLE_UP).width - ROUND_BUTTON_PADDING_HOR,
            ROUND_BUTTON_PADDING_VER,
            object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    super.clicked(event, x, y)
                    soundToggle(globalHandlers)
                }
            },
            TexturesDefinitions.ICON_SOUND_ON,
        )
        imageButton.isChecked = globalHandlers.soundPlayer.enabled
        soundButton = imageButton
    }

    private fun addHelpButton() {
        helpButton = addRoundButton(TexturesDefinitions.ICON_HELP,
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
        soundButton?.remove()
        helpButton?.remove()
    }


    private fun addBackButton(table: Table) {
        addButton(
            table,
            LABEL_BACK,
            160,
            globalHandlers.assetsManager.getFont(FontsDefinitions.VARELA_40),
            scale = 0.5F
        ) {
            fadeOutTable(table)
            fadeInTable(mainMenuTable)
        }
    }


    private fun addSpecialButtons() {
        addSoundButton()
        addHelpButton()
    }

    private fun fillMainMenuTable(beginGameAction: BeginGameAction) {
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
            image = globalHandlers.assetsManager.getTexture(TexturesDefinitions.KIDS)
        ) {
            beginGameAction.begin(GameModes.KIDS)
        }
        addButton(
            table = mainMenuTable,
            image = globalHandlers.assetsManager.getTexture(TexturesDefinitions.GPT)
        ) {
            fadeOutTable(mainMenuTable)
            fadeInTable(gptTable)
        }
    }

    private fun fillDifficultySelectionTable(beginGameAction: BeginGameAction) {
        GameModes.values().filter { regularGameModes.contains(it) }.forEach {
            addButton(
                difficultySelectionTable,
                it.displayName,
            ) { beginGameAction.begin(it) }
        }
        addBackButton(difficultySelectionTable)
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

    private fun addMainMenuTable(beginGameAction: BeginGameAction) {
        initMenuTable(mainMenuTable)
        addLogo()
        fillMainMenuTable(beginGameAction)
        addChampionsView()
    }

    private fun addMenuLabel(table: Table, text: String) {
        table.add(
            GameLabel(
                text.reversed(),
                Label.LabelStyle(
                    globalHandlers.assetsManager.getFont(FontsDefinitions.VARELA_80),
                    Color.WHITE
                ),
                globalHandlers.androidInterface
            )
        ).pad(20F).row()
    }

    private fun addDifficultySelectionTable(beginGameAction: BeginGameAction) {
        initMenuTable(difficultySelectionTable)
        addMenuLabel(difficultySelectionTable, LABEL_DIFFICULTY_SELECT)
        fillDifficultySelectionTable(beginGameAction)
    }

    fun finishLoadingAnimationAndDisplayMenu(beginGameAction: BeginGameAction) {
        loadingAnimationRenderer.flyOutBricks(
            globalHandlers.assetsManager.getSound(SoundsDefinitions.FLYBY),
            globalHandlers.soundPlayer
        )
        Gdx.app.postRunnable {
            addSpecialButtons()
            addMainMenuTable(beginGameAction)
            addDifficultySelectionTable(beginGameAction)
            addGptTable()
            versionLabel = GameLabel(
                "v$versionName",
                Label.LabelStyle(
                    globalHandlers.assetsManager.getFont(FontsDefinitions.VARELA_35),
                    Color.BLACK
                ),
                globalHandlers.androidInterface
            )
            stage.addActor(versionLabel)
            addMenuTable(true, mainMenuTable)
            addMenuTable(false, difficultySelectionTable)
            addMenuTable(false, gptTable)
        }
    }

    private fun addMenuTable(visible: Boolean, table: Table) {
        stage.addActor(table)
        table.isVisible = visible
    }

    private fun addGptTable() {
        initMenuTable(gptTable)
        addMenuLabel(gptTable, LABEL_GPT_ENTER)
        val textField = GameTextField(createTextFieldStyle())
        textField.alignment = Align.center
        textField.setTextFieldListener { _, c ->
            if (c != 8.toChar()) {
                if (!c.toString().matches(hebrewRegex)) {
                    textField.text = textField.text.filter { it.toString().matches(hebrewRegex) }
                }
            }
            textField.refreshHebrew()
        }
        gptTable.add(textField)
            .expandX()
            .width(stage.width - GPT_TEXT_FIELD_PADDING * 2F)
            .pad(GPT_TEXT_FIELD_PADDING)
            .row()
        addBackButton(gptTable)
    }

    private fun createTextFieldStyle() = TextField.TextFieldStyle(
        globalHandlers.assetsManager.getFont(FontsDefinitions.VARELA_40),
        Color.WHITE,
        TextureRegionDrawable(globalHandlers.assetsManager.getTexture(TexturesDefinitions.TEXT_CURSOR)),
        null,
        NinePatchDrawable(
            NinePatch(
                globalHandlers.assetsManager.getTexture(TexturesDefinitions.POPUP_BUTTON_DOWN),
                25, 25, 25, 25
            )
        )
    )

    override fun dispose() {

    }

    fun clearScreen() {
        mainMenuTable.clear()
        mainMenuTable.remove()
        difficultySelectionTable.remove()
        clear()
        versionLabel?.remove()
    }


    private fun addChampionsView() {
        if (globalHandlers.androidInterface.isConnected()) {
            mainMenuTable.add(
                ChampionsView(
                    globalHandlers.assetsManager.getFont(FontsDefinitions.VARELA_35),
                    globalHandlers.androidInterface,
                    globalHandlers.assetsManager.getTexture(TexturesDefinitions.ICON_HIGHSCORES),
                    globalHandlers.assetsManager.getAtlas(AtlasesDefinitions.LOADING),
                    globalHandlers.assetsManager.getTexture(TexturesDefinitions.BUTTON_ARROW_UP),
                    globalHandlers.assetsManager.getTexture(TexturesDefinitions.BUTTON_ARROW_DOWN)
                )
            ).pad(CHAMPIONS_VIEW_PADDING).expandX().center()
        } else {
            addLoginButton()
        }
    }

    private fun addLoginButton() {
        val font = globalHandlers.assetsManager.getFont(FontsDefinitions.VARELA_40)
        val style = Label.LabelStyle(font, Color.WHITE)
        val text = GeneralUtils.fixHebrewDescription(LABEL_LOGIN)
        val label = GameLabel(text, style, globalHandlers.androidInterface)
        val loginButton =
            Image(globalHandlers.assetsManager.getTexture(TexturesDefinitions.GOOGLE_PLAY))
        val clickListener = object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                globalHandlers.androidInterface.login()
                menuScreen.onLoginClick()
                Timer.schedule(object : Task() {
                    override fun run() {
                        menuScreen.restart()
                    }
                }, 1F)
            }
        }
        loginButton.addListener(clickListener)
        mainMenuTable.add(loginButton).pad(CHAMPIONS_VIEW_PADDING).expandX().center().row()
        label.setAlignment(Align.center)
        label.addListener(clickListener)
        mainMenuTable.add(label)
    }

    private fun initMenuTable(table: Table) {
        table.debug = DebugSettings.SHOW_UI_BORDERS
        table.setFillParent(true)
        table.touchable = Touchable.childrenOnly
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
        private const val LABEL_DIFFICULTY_SELECT = "בחרו רמת קושי:"
        private const val LABEL_GPT_ENTER = "הקישו כל קטגוריה:"
        private const val LABEL_LOGIN = "להשתתפות בטבלת האלופים\nהתחברו כאן!"
        private const val LOGO_PADDING_TOP = 300F
        private const val LOGO_PADDING_BOTTOM = 75F
        private const val CHAMPIONS_VIEW_PADDING = 64F
        private const val ROUND_BUTTON_PADDING_HOR = 30F
        private const val ROUND_BUTTON_PADDING_VER = 60F
        private const val FADE_ANIMATION_DURATION = 0.3F
        private const val LABEL_BACK = "חזרה"
        private const val LABEL_BEGIN_GAME = "משחק חדש"
        private const val GPT_TEXT_FIELD_PADDING = 40F
        private val hebrewRegex = Regex("[א-ת ]+")

    }

}