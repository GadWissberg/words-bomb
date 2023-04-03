package com.gadarts.shubutz.core.screens.game.view

import com.gadarts.shubutz.core.ShubutzGame
import com.gadarts.shubutz.core.model.assets.definitions.ParticleEffectsDefinitions
import com.gadarts.shubutz.core.model.assets.definitions.SoundsDefinitions
import com.gadarts.shubutz.core.screens.game.GlobalHandlers
import com.gadarts.shubutz.core.screens.menu.view.stage.GameStage

class EffectsHandler {
    fun applyPartyEffect(globalHandlers: GlobalHandlers, stage: GameStage) {
        val particleEffect =
            globalHandlers.assetsManager.getParticleEffect(ParticleEffectsDefinitions.PARTY)
        particleEffect.emitters.forEach {
            it.spawnWidth.highMin = ShubutzGame.RESOLUTION_WIDTH.toFloat()
        }
        val party = ParticleEffectActor(particleEffect)
        party.setPosition(0F, ShubutzGame.RESOLUTION_HEIGHT.toFloat())
        stage.addActor(party)
        party.start()
        globalHandlers.soundPlayer.playSound(globalHandlers.assetsManager.getSound(SoundsDefinitions.PURCHASED))
    }

}
