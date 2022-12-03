package com.gadarts.wordsbomb.core.view.hud

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Cell
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.gadarts.wordsbomb.core.view.Brick

class DeckTable : Table() {

    override fun removeActor(actor: Actor?, unfocus: Boolean): Boolean {
        val cell = getCell(actor)
        cell.pad(0F)
        return super.removeActor(actor, unfocus)
    }

    fun insertBrick(brick: Brick) {
        (cells.first { it.actor == null } as Cell<*>)
            .setActor((brick as Actor))
            .pad(HudStage.LETTER_BRICKS_PADDING)
    }
}
