package com.gadarts.shubutz.core.screens.menu.view

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Disposable
import com.gadarts.shubutz.core.DebugSettings
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
    private var mainMenuScreenButtons =
        MainMenuScreenButtons(mainMenuTable, difficultySelectionTable, globalHandlers)
    private lateinit var logoTable: Table
    private var versionLabel: GameLabel? = null
    var loadingAnimationRenderer = LoadingAnimationHandler(globalHandlers.androidInterface)


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
        mainMenuScreenButtons.fillMainMenuTable(beginGameAction)
    }

    private fun addDifficultySelectionLabel() {
        difficultySelectionTable.add(
            GameLabel(
                LABEL_DIFFICULTY_SELECT.reversed(),
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
        addDifficultySelectionLabel()
        mainMenuScreenButtons.fillDifficultySelectionTable(beginGameAction)
    }

    fun finishLoadingAnimationAndDisplayMenu(beginGameAction: BeginGameAction) {
        loadingAnimationRenderer.flyOutBricks(
            globalHandlers.assetsManager.getSound(SoundsDefinitions.FLYBY),
            globalHandlers.soundPlayer
        )
        Gdx.app.postRunnable {
            mainMenuScreenButtons.addSpecialButtons()
            addMainMenuTable(beginGameAction)
            addDifficultySelectionTable(beginGameAction)
            versionLabel = GameLabel(
                "v$versionName",
                Label.LabelStyle(
                    globalHandlers.assetsManager.getFont(FontsDefinitions.VARELA_35),
                    Color.BLACK
                ),
                globalHandlers.androidInterface
            )
            stage.addActor(versionLabel)
            stage.addActor(mainMenuTable)
            difficultySelectionTable.isVisible = false
            stage.addActor(difficultySelectionTable)
        }
    }

    override fun dispose() {

    }

    fun clearScreen() {
        mainMenuTable.clear()
        mainMenuTable.remove()
        difficultySelectionTable.remove()
        mainMenuScreenButtons.clear()
        versionLabel?.remove()
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
        private const val LABEL_DIFFICULTY_SELECT = "בחר רמת קושי:"
        private const val LOGO_PADDING_TOP = 300F
        private const val LOGO_PADDING_BOTTOM = 75F
    }

}