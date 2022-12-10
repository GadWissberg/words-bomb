package com.gadarts.shubutz.core.screens.menu.view.stage

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.utils.viewport.FitViewport
import com.gadarts.shubutz.core.GameStage
import com.gadarts.shubutz.core.GeneralUtils
import com.gadarts.shubutz.core.model.assets.GameAssetManager
import com.gadarts.shubutz.core.model.assets.TexturesDefinitions

class GameStage(fitViewport: FitViewport, assetsManager: GameAssetManager) :
    GameStage<MenuStageEventsSubscriber>(
        fitViewport,
        SpriteBatch(),
        assetsManager
    ) {
    init {
        addCloud(assetsManager, TexturesDefinitions.CLOUD_1, 0)
        addCloud(assetsManager, TexturesDefinitions.CLOUD_2, 1)
        addCloud(assetsManager, TexturesDefinitions.CLOUD_3, 2)
        addCloud(assetsManager, TexturesDefinitions.CLOUD_4, 3)
    }

    private fun addCloud(assetsManager: GameAssetManager, texture: TexturesDefinitions, i: Int) {
        val cloud = Image(assetsManager.getTexture(texture))
        val screenWidth = Gdx.graphics.width.toFloat()
        val x = MathUtils.random(0F, screenWidth - cloud.width)
        val heightDivision = Gdx.graphics.height.toFloat() / NUMBER_OF_CLOUDS.toFloat()
        val minY = heightDivision * i
        val maxY = minY + heightDivision - cloud.height
        val y = MathUtils.random(minY, maxY)
        cloud.setPosition(x, y)
        addActor(cloud)
        cloud.addAction(
            Actions.parallel(
                Actions.forever(
                    Actions.sequence(
                        Actions.scaleBy(
                            CLOUDS_SCALE,
                            CLOUDS_SCALE,
                            MathUtils.random(CLOUDS_SCALE_DURATION_MIN, CLOUDS_SCALE_DURATION_MAX),
                            Interpolation.bounce
                        ),
                        Actions.scaleBy(
                            -CLOUDS_SCALE,
                            -CLOUDS_SCALE,
                            MathUtils.random(CLOUDS_SCALE_DURATION_MIN, CLOUDS_SCALE_DURATION_MAX),
                            Interpolation.bounce
                        ),
                    )
                ),
                Actions.forever(
                    Actions.sequence(
                        Actions.moveTo(
                            screenWidth - cloud.width,
                            cloud.y,
                            MathUtils.random(CLOUDS_MOVEMENT_DUR_MIN, CLOUDS_MOVEMENT_DUR_MAX),
                            Interpolation.bounceIn
                        ),
                        Actions.moveTo(
                            0F,
                            cloud.y,
                            MathUtils.random(CLOUDS_MOVEMENT_DUR_MIN, CLOUDS_MOVEMENT_DUR_MAX),
                            Interpolation.bounceIn
                        )
                    )
                )
            )
        )
    }

    override val subscribers = HashSet<MenuStageEventsSubscriber>()

    override fun draw() {
        GeneralUtils.resetDisplay(BACKGROUND_COLOR)
        super.draw()
    }

    companion object {
        val BACKGROUND_COLOR: Color = Color.valueOf("B5EAEA")
        const val NUMBER_OF_CLOUDS = 4
        const val CLOUDS_SCALE = 0.2F
        const val CLOUDS_SCALE_DURATION_MIN = 10F
        const val CLOUDS_SCALE_DURATION_MAX = 20F
        const val CLOUDS_MOVEMENT_DUR_MIN = 30F
        const val CLOUDS_MOVEMENT_DUR_MAX = 40F
    }
}