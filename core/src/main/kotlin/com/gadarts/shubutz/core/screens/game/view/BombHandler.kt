package com.gadarts.shubutz.core.screens.game.view

import com.badlogic.gdx.math.Interpolation.*
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.gadarts.shubutz.core.model.GameModel
import com.gadarts.shubutz.core.model.assets.FontsDefinitions
import com.gadarts.shubutz.core.model.assets.GameAssetManager
import com.gadarts.shubutz.core.model.assets.ParticleEffectsDefinitions
import com.gadarts.shubutz.core.model.assets.TexturesDefinitions
import com.gadarts.shubutz.core.screens.menu.view.stage.GameStage

class BombHandler {
    private lateinit var bomb: Bomb
    private lateinit var fireParticleEffectActor: ParticleEffectActor
    private lateinit var explosionParticleEffectActor: ParticleEffectActor

    fun addBomb(
        assetsManager: GameAssetManager,
        stage: GameStage,
        uiTable: Table,
        gameModel: GameModel
    ) {
        fireParticleEffectActor = ParticleEffectActor(
            assetsManager.getParticleEffect(
                ParticleEffectsDefinitions.FIRE
            )
        )
        createBomb(assetsManager, gameModel)
        stage.addActor(fireParticleEffectActor)
        val bombTexture = assetsManager.getTexture(TexturesDefinitions.BOMB)
        uiTable.add(bomb).size(bombTexture.width.toFloat(), bombTexture.height.toFloat())
            .pad(BOMB_PADDING).row()
    }

    private fun createBomb(assetsManager: GameAssetManager, gameModel: GameModel) {
        val bombTexture = assetsManager.getTexture(TexturesDefinitions.BOMB)
        bomb = Bomb(
            bombTexture,
            fireParticleEffectActor,
            assetsManager.getFont(FontsDefinitions.VARELA_320),
            gameModel.triesLeft
        )
        bomb.addAction(
            Actions.forever(
                Actions.sequence(
                    Actions.moveBy(
                        0F, BOMB_IDLE_ANIMATION_DISTANCE / 2F, 2.5F,
                        exp10Out
                    ),
                    Actions.moveBy(
                        0F, -BOMB_IDLE_ANIMATION_DISTANCE, 5F,
                        exp10
                    ),
                    Actions.moveBy(
                        0F, BOMB_IDLE_ANIMATION_DISTANCE / 2F, 2.5F,
                        exp10In
                    )
                )
            )
        )
        bomb.setOrigin(bombTexture.width / 2F, bombTexture.height / 2F)
        bomb.toBack()
    }

    fun onGameWinAnimation() {
        fireParticleEffectActor.stop()
    }

    fun updateLabel(gameModel: GameModel) {
        bomb.updateLabel(gameModel.triesLeft)
    }

    fun onLetterFail() {
        if (!fireParticleEffectActor.started) {
            bomb.startFire()
        }
    }

    fun onGameOverAnimation(
        assetsManager: GameAssetManager,
        stage: GameStage
    ) {
        fireParticleEffectActor.stop()
        val particleEffect = assetsManager.getParticleEffect(ParticleEffectsDefinitions.EXP)
        val bombPosition = bomb.localToScreenCoordinates(auxVector.setZero())
        explosionParticleEffectActor = ParticleEffectActor(particleEffect)
        explosionParticleEffectActor.setPosition(
            bombPosition.x + bomb.width / 2F,
            stage.height - bombPosition.y + bomb.height / 5F
        )

        bomb.addAction(
            Actions.parallel(
                Actions.sequence(
                    Actions.sizeTo(0F, 0F, BOMB_GAME_OVER_ANIMATION_DURATION, linear),
                    Actions.removeActor()
                ),
                Actions.moveBy(
                    bomb.width / 2F,
                    bomb.height / 2F,
                    BOMB_GAME_OVER_ANIMATION_DURATION,
                    linear
                )
            )
        )

        stage.addActor(explosionParticleEffectActor)
        explosionParticleEffectActor.start()
        bomb.hideLabel()
    }

    fun onScreenClear(postAction: Runnable) {
        bomb.addAction(
            Actions.sequence(
                Actions.fadeOut(1F, swingIn),
                Actions.run { postAction.run() },
                Actions.run { bomb.remove() }
            )
        )
    }

    companion object {
        private const val BOMB_PADDING = 20F
        private const val BOMB_GAME_OVER_ANIMATION_DURATION = 0.5F
        private const val BOMB_IDLE_ANIMATION_DISTANCE = 40F
        private val auxVector = Vector2()
    }
}
