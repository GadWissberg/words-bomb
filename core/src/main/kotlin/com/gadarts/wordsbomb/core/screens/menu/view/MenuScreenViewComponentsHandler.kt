package com.gadarts.wordsbomb.core.screens.menu.view

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Gdx.graphics
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.math.Interpolation.*
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.Table.Debug
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.viewport.FitViewport
import com.gadarts.wordsbomb.core.DefaultGameSettings
import com.gadarts.wordsbomb.core.model.assets.FontsDefinitions
import com.gadarts.wordsbomb.core.model.assets.GameAssetManager
import com.gadarts.wordsbomb.core.model.assets.TexturesDefinitions
import com.gadarts.wordsbomb.core.screens.menu.view.stage.GameStage

class MenuScreenViewComponentsHandler(private val assetsManager: GameAssetManager) : Disposable {


    private lateinit var listLabelStyle: Label.LabelStyle
    private var uiTable: Table? = null
    lateinit var stage: GameStage
    private var bitmapFont: BitmapFont = assetsManager.getFont(FontsDefinitions.VARELA_80)

    private fun addStage(assetsManager: GameAssetManager) {
        stage = GameStage(
            FitViewport(graphics.width.toFloat(), graphics.height.toFloat()),
            assetsManager
        )
        val multiplexer = Gdx.input.inputProcessor as InputMultiplexer
        multiplexer.addProcessor(stage)
    }

    private fun addUserInterface(beginGameAction: ClickListener) {
        uiTable = Table()
        uiTable!!.debug = DefaultGameSettings.SHOW_UI_BORDERS
        uiTable!!.setFillParent(true)
        addLogo()
        addButtons(uiTable!!, beginGameAction)
        uiTable!!.touchable = Touchable.childrenOnly
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
        val logoTable = Table()
        logoTable.debug(if (DefaultGameSettings.SHOW_UI_BORDERS) Debug.all else Debug.none)
        uiTable!!.add(logoTable).pad(LOGO_PADDING_TOP, 0F, LOGO_PADDING_BOTTOM, 0F).colspan(2).row()
        return logoTable
    }

    private fun addLogoLetter(
        logoTable: Table,
        index: Int,
        textureDefinition: TexturesDefinitions
    ) {
        val texture = assetsManager.getTexture(textureDefinition)
        val image = Image(texture)
        logoTable.add(image).size(texture.width.toFloat(), texture.height.toFloat())
        addLogoLetterAnimation(image, index)
    }

    private fun addLogoLetterAnimation(letterActor: Image, index: Int) {
        letterActor.addAction(
            Actions.sequence(
                Actions.scaleTo(0F, 0F),
                Actions.delay(0.2F * index),
                Actions.scaleTo(1F, 1F, 0.5F, exp10),
                Actions.forever(
                    Actions.sequence(
                        Actions.moveBy(0F, 40F, MathUtils.random(3F, 6F), bounceIn),
                        Actions.moveBy(0F, -80F, MathUtils.random(3F, 6F), bounce),
                        Actions.moveBy(0F, 40F, MathUtils.random(3F, 6F), exp10)
                    )
                )
            )
        )
    }

    private fun addButtons(
        table: Table,
        beginGameAction: ClickListener,
    ) {
        val buttonUpTex = assetsManager.getTexture(TexturesDefinitions.BUTTON_UP)
        val buttonDownTex = assetsManager.getTexture(TexturesDefinitions.BUTTON_DOWN)
        stage.addButton(
            table,
            beginGameAction,
            MenuScreenView.LABEL_OPEN_ROOM.reversed(),
            span = 2,
            up = buttonUpTex,
            down = buttonDownTex,
            bitmapFont = bitmapFont,
            topPadding = MenuScreenView.BUTTON_PADDING
        )
    }

    fun init(assetsManager: GameAssetManager) {
        addStage(assetsManager)
        val font = assetsManager.getFont(FontsDefinitions.VARELA_80)
        listLabelStyle = Label.LabelStyle(font, Color.WHITE)
    }

    fun render(delta: Float) {
        stage.act(delta)
        stage.draw()
    }

    override fun dispose() {
        stage.dispose()
    }

    fun onHide() {
        val multiplexer = Gdx.input.inputProcessor as InputMultiplexer
        multiplexer.removeProcessor(stage)
    }

    fun onLoadingAnimationReady(beginGameAction: ClickListener) {
        Gdx.app.postRunnable {
            if (uiTable == null) {
                addUserInterface(beginGameAction)
                stage.addActor(uiTable)
            }
        }
    }

    companion object {
        const val LOGO_PADDING_TOP = 300F
        const val LOGO_PADDING_BOTTOM = 75F
    }
}