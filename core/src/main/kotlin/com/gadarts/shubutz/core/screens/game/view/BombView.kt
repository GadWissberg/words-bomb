package com.gadarts.shubutz.core.screens.game.view

import com.badlogic.gdx.math.Interpolation.*
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.gadarts.shubutz.core.model.GameModel
import com.gadarts.shubutz.core.model.assets.*
import com.gadarts.shubutz.core.model.assets.definitions.FontsDefinitions
import com.gadarts.shubutz.core.model.assets.definitions.ParticleEffectsDefinitions
import com.gadarts.shubutz.core.model.assets.definitions.SoundsDefinitions
import com.gadarts.shubutz.core.model.assets.definitions.TexturesDefinitions
import com.gadarts.shubutz.core.screens.game.GlobalHandlers
import com.gadarts.shubutz.core.screens.menu.view.stage.GameStage

class BombView(
    private val globalHandlers: GlobalHandlers,
) {
    lateinit var bombComponent: BombComponent
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
        stage.addActor(fireParticleEffectActor)
        createBomb(assetsManager, gameModel)
        val bombTexture = assetsManager.getTexture(TexturesDefinitions.BOMB)
        uiTable.add(bombComponent).size(bombTexture.width.toFloat(), bombTexture.height.toFloat())
            .pad(BOMB_PADDING).row()
    }

    private fun createBomb(assetsManager: GameAssetManager, gameModel: GameModel) {
        val bombTexture = assetsManager.getTexture(TexturesDefinitions.BOMB)
        bombComponent = BombComponent(
            bombTexture,
            fireParticleEffectActor,
            assetsManager.getFont(FontsDefinitions.VARELA_320),
            gameModel.triesLeft
        )
        bombComponent.addAction(
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
        bombComponent.setOrigin(bombTexture.width / 2F, bombTexture.height / 2F)
        bombComponent.toBack()
    }

    fun stopFire() {
        fireParticleEffectActor.stop()
    }

    fun updateLabel(gameModel: GameModel) {
        bombComponent.updateLabel(gameModel.triesLeft)
    }

    fun onIncorrectGuess(gameModel: GameModel) {
        if (!fireParticleEffectActor.started) {
            globalHandlers.soundPlayer.playSound(
                globalHandlers.assetsManager.getSound(
                    SoundsDefinitions.IGNITE
                )
            )
            bombComponent.startFire()
        }
        bombComponent.onIncorrectGuess(gameModel)
    }

    fun onGameOverAnimation(
        assetsManager: GameAssetManager,
        stage: GameStage
    ) {
        fireParticleEffectActor.stop()
        val particleEffect = assetsManager.getParticleEffect(ParticleEffectsDefinitions.EXP)
        val bombPosition = bombComponent.localToStageCoordinates(auxVector.setZero())
        explosionParticleEffectActor = ParticleEffectActor(particleEffect)
        explosionParticleEffectActor.setPosition(
            bombPosition.x + bombComponent.width / 2F,
            stage.height - bombPosition.y + bombComponent.height / 5F
        )

        bombComponent.addAction(
            Actions.parallel(
                Actions.sequence(
                    Actions.sizeTo(0F, 0F, BOMB_GAME_OVER_ANIMATION_DURATION, linear),
                    Actions.removeActor()
                ),
                Actions.moveBy(
                    bombComponent.width / 2F,
                    bombComponent.height / 2F,
                    BOMB_GAME_OVER_ANIMATION_DURATION,
                    linear
                )
            )
        )

        stage.addActor(explosionParticleEffectActor)
        globalHandlers.soundPlayer.playSound(assetsManager.getSound(SoundsDefinitions.EXPLOSION))
        explosionParticleEffectActor.start()
        bombComponent.hideLabel()
    }

    fun animateBombVanish(postAction: Runnable) {
        bombComponent.addAction(
            Actions.sequence(
                Actions.fadeOut(1F, swingIn),
                Actions.run { postAction.run() },
                Actions.run { bombComponent.remove() }
            )
        )
    }

    fun clear() {
        fireParticleEffectActor.remove()
    }

    companion object {
        private const val BOMB_PADDING = 20F
        private const val BOMB_GAME_OVER_ANIMATION_DURATION = 0.5F
        private const val BOMB_IDLE_ANIMATION_DISTANCE = 40F
        private val auxVector = Vector2()
    }
}
