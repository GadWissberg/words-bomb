package com.gadarts.shubutz.core.screens.game.view

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Queue
import com.badlogic.gdx.utils.TimeUtils
import com.gadarts.shubutz.core.model.Difficulties
import com.gadarts.shubutz.core.model.GameModel
import com.gadarts.shubutz.core.model.assets.GameAssetManager
import com.gadarts.shubutz.core.model.assets.definitions.FontsDefinitions
import com.gadarts.shubutz.core.model.assets.definitions.ParticleEffectsDefinitions
import com.gadarts.shubutz.core.model.assets.definitions.TexturesDefinitions
import com.gadarts.shubutz.core.screens.game.GlobalHandlers

class CoinsLabelHandler {
    lateinit var coinsIcon: Image
    private lateinit var coinsLabel: Label
    private var applyCoinsChangeAnimation = false
    private var lastCoinsLabelChange = 0L
    private var lastCoinsValueChangeLabelDequeue: Long = 0
    private val coinsValueChangeLabels = Queue<Int>()

    private fun addCoinValueChangedLabel(
        coinsAmount: Int,
    ) {
        coinsValueChangeLabels.addFirst(coinsAmount)
    }

    private fun applyCoinsChange(cost: Int) {
        addCoinValueChangedLabel(cost)
        applyCoinsChangeAnimation = true
    }

    private fun addStarsEffectToCoinsLabel(globalHandlers: GlobalHandlers, stage: Stage) {
        val particleEffect =
            globalHandlers.assetsManager.getParticleEffect(ParticleEffectsDefinitions.STARS)
        val particleEffectActor = ParticleEffectActor(particleEffect)
        stage.addActor(particleEffectActor)
        particleEffectActor.start()
        val coords = coinsLabel.localToStageCoordinates(auxVector.setZero())
        particleEffectActor.setPosition(
            coords.x + coinsLabel.width / 2F,
            coords.y + coinsLabel.height / 2F
        )
    }

    fun addCoinsLabel(
        gameModel: GameModel,
        font80: BitmapFont,
        table: Table,
        assetsManager: GameAssetManager,
        topPartTexture: Texture,
    ) {
        coinsLabel = Label(gameModel.coins.toString(), Label.LabelStyle(font80, Color.WHITE))
        table.add(coinsLabel).pad(0F, 0F, 0F, COINS_LABEL_PADDING_RIGHT)
        val icon =
            if (gameModel.selectedDifficulty != Difficulties.KIDS) TexturesDefinitions.COINS_ICON else TexturesDefinitions.CANDY
        coinsIcon = Image(assetsManager.getTexture(icon))
        table.add(coinsIcon)
            .size(
                topPartTexture.height.toFloat(),
                topPartTexture.height.toFloat()
            ).pad(
                COINS_ICON_PAD,
                COINS_ICON_PAD,
                COINS_ICON_PAD,
                COINS_ICON_PAD_RIGHT
            ).row()
    }

    fun act(
        topPartTable: Table,
        gameModel: GameModel,
        globalHandlers: GlobalHandlers
    ) {
        if (coinsValueChangeLabels.notEmpty()) {
            if (TimeUtils.timeSinceMillis(lastCoinsValueChangeLabelDequeue) > COINS_VALUE_CHANGE_LABEL_INTERVAL) {
                applyNextCoinsValueChangeLabel(
                    coinsValueChangeLabels.removeFirst(),
                    topPartTable,
                    globalHandlers
                )
                lastCoinsValueChangeLabelDequeue = TimeUtils.millis()
            }
        }
        handleCoinsLabelChangeAnimation(gameModel)
    }

    private fun handleCoinsLabelChangeAnimation(gameModel: GameModel) {
        if (applyCoinsChangeAnimation && TimeUtils.timeSinceMillis(lastCoinsLabelChange) >= COINS_LABEL_CHANGE_INCREMENT_INTERVAL) {
            val displayedValue = Integer.parseInt(coinsLabel.text.toString())
            if (displayedValue != gameModel.coins) {
                coinsLabel.setText(displayedValue + (if (displayedValue > gameModel.coins) -1 else 1))
                lastCoinsLabelChange = TimeUtils.millis()
            } else {
                applyCoinsChangeAnimation = false
            }
        }
    }

    private fun createDeltaCoinsLabel(coinsAmount: Int, globalHandlers: GlobalHandlers) = Label(
        "${if (coinsAmount > 0) "+" else ""}$coinsAmount",
        Label.LabelStyle(
            globalHandlers.assetsManager.getFont(FontsDefinitions.VARELA_80),
            if (coinsAmount > 0) Color.GOLD else Color.RED
        )
    )

    private fun applyNextCoinsValueChangeLabel(
        coinsAmount: Int,
        topPartTable: Table,
        globalHandlers: GlobalHandlers
    ) {
        val deltaCoinLabel = createDeltaCoinsLabel(coinsAmount, globalHandlers)
        topPartTable.stage.addActor(deltaCoinLabel)
        val coinsLabelPos = coinsLabel.localToStageCoordinates(auxVector.setZero())
        deltaCoinLabel.setPosition(
            coinsLabelPos.x,
            coinsLabelPos.y - COINS_VALUE_CHANGE_LABEL_Y_OFFSET
        )

        deltaCoinLabel.addAction(
            Actions.sequence(
                Actions.moveBy(
                    0F,
                    -100F,
                    WIN_COIN_LABEL_ANIMATION_DURATION,
                    Interpolation.smooth2
                ),
                Actions.removeActor()
            )
        )
    }

    fun onCorrectGuess(coinsAmount: Int) {
        if (coinsAmount > 0) {
            applyCoinsChange(coinsAmount)
        }
    }

    fun onLetterRevealed(cost: Int) {
        applyCoinsChange(-cost)
    }

    fun onRewardForVideoAd(rewardAmount: Int) {
        applyCoinsChange(rewardAmount)
    }

    fun onPurchasedCoins(amount: Int) {
        applyCoinsChange(amount)
    }

    fun onGameWin(globalHandlers: GlobalHandlers, stage: Stage) {
        addStarsEffectToCoinsLabel(globalHandlers, stage)
    }

    companion object {
        private val auxVector = Vector2()
        private const val WIN_COIN_LABEL_ANIMATION_DURATION = 4F
        private const val COINS_VALUE_CHANGE_LABEL_INTERVAL = 1000F
        private const val COINS_LABEL_CHANGE_INCREMENT_INTERVAL = 128F
        private const val COINS_VALUE_CHANGE_LABEL_Y_OFFSET = 50F
        private const val COINS_LABEL_PADDING_RIGHT = 40F
        private const val COINS_ICON_PAD_RIGHT = 40F
        private const val COINS_ICON_PAD = 20F
    }
}
