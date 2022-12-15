package com.gadarts.shubutz.core.screens.menu

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.utils.Align
import com.gadarts.shubutz.core.model.assets.FontsDefinitions
import com.gadarts.shubutz.core.model.assets.GameAssetManager
import com.gadarts.shubutz.core.model.assets.TexturesDefinitions
import com.gadarts.shubutz.core.screens.menu.view.MenuScreenView
import com.gadarts.shubutz.core.screens.menu.view.MenuScreenViewEventsSubscriber

class LoadingAnimationHandler {
    private var loadingReady: Boolean = false
    private var brick1: MenuScreenView.BrickAnimation? = null
    private var brick2: MenuScreenView.BrickAnimation? = null
    private var brick3: MenuScreenView.BrickAnimation? = null
    private var brick4: MenuScreenView.BrickAnimation? = null

    fun addLoadingAnimation(
        assetsManager: GameAssetManager,
        stage: Stage
    ) {
        val texture = assetsManager.getTexture(TexturesDefinitions.BRICK)
        val font = assetsManager.getFont(FontsDefinitions.VARELA_80)
        val style = Label.LabelStyle(font, Color.WHITE)
        val halfWidth = texture.width / 2F
        brick1 = addLoadingBrick(stage, texture, style, "ט", halfWidth + texture.width)
        brick2 = addLoadingBrick(stage, texture, style, "ו", halfWidth)
        brick3 = addLoadingBrick(stage, texture, style, "ע", -halfWidth)
        brick4 = addLoadingBrick(stage, texture, style, "ן", -halfWidth - texture.width)
    }

    private fun flyOutBrick(brickAnimation: MenuScreenView.BrickAnimation) {
        val yDelta = Gdx.graphics.height.toFloat()
        brickAnimation.addAction(
            Actions.sequence(
                Actions.moveBy(0F, yDelta, 2F, Interpolation.exp10),
                Actions.hide()
            )
        )
    }

    fun render(subscribers: HashSet<MenuScreenViewEventsSubscriber>) {
        if (oneOfBricksMissing()) return
        if (!loadingReady && brick1!!.ready && brick2!!.ready && brick3!!.ready && brick4!!.ready) {
            loadingReady = true
            subscribers.forEach { it.onLoadingAnimationReady() }
        }
    }

    fun onLoadingAnimationReady() {
        if (oneOfBricksMissing()) return
        flyOutBrick(brick1!!)
        flyOutBrick(brick2!!)
        flyOutBrick(brick3!!)
        flyOutBrick(brick4!!)
    }

    private fun oneOfBricksMissing() =
        brick1 == null || brick2 == null || brick3 == null || brick4 == null

    private fun addLoadingBrick(
        stage: Stage,
        brickTexture: Texture,
        style: Label.LabelStyle,
        letter: String,
        relativeX: Float
    ): MenuScreenView.BrickAnimation {
        val brickTable = createLoadingAnimationBrick(letter, style, brickTexture)
        stage.addActor(brickTable)
        val x = Gdx.graphics.width / 2F + relativeX
        val startY = Gdx.graphics.height.toFloat() + brickTexture.height.toFloat()
        brickTable.setPosition(x, startY)
        brickTable.isTransform = true
        addActionsToLoadingAnimationBrick(brickTable, x)
        return brickTable
    }

    private fun addActionsToLoadingAnimationBrick(
        brickTable: MenuScreenView.BrickAnimation,
        x: Float,
    ) {
        brickTable.addAction(
            Actions.sequence(
                Actions.delay(MathUtils.random(0F, 2F)),
                Actions.parallel(
                    Actions.sequence(
                        Actions.moveTo(
                            x,
                            Gdx.graphics.height / 2F,
                            MathUtils.random(FALL_MIN_DURATION, FALL_MAX_DURATION),
                            Interpolation.bounce
                        ),
                        MenuScreenView.LetterArrivedAction(brickTable),
                        Actions.forever(
                            Actions.sequence(
                                Actions.delay(2F),
                                Actions.moveBy(0F, 20F, 1F, Interpolation.elastic),
                                Actions.moveBy(0F, -20F, 1F, Interpolation.elastic),
                            )
                        )
                    ),
                    Actions.forever(
                        Actions.sequence(
                            Actions.rotateBy(10F, 1F, Interpolation.circle),
                            Actions.rotateBy(-20F, 1F, Interpolation.circle),
                            Actions.rotateBy(10F, 1F, Interpolation.circle),
                            Actions.delay(MathUtils.random(2F, 4F))
                        )
                    )
                )
            )
        )
    }

    private fun createLoadingAnimationBrick(
        letter: String,
        style: Label.LabelStyle,
        brickTexture: Texture
    ): MenuScreenView.BrickAnimation {
        val brickTable = MenuScreenView.BrickAnimation()
        val letterView = Label(letter, style)
        letterView.setAlignment(Align.center)
        brickTable.stack(Image(brickTexture), letterView)
            .center()
            .size(brickTexture.width.toFloat(), brickTexture.height.toFloat())
        return brickTable
    }

    companion object {
        const val FALL_MIN_DURATION = 2F
        const val FALL_MAX_DURATION = 5F
    }
}