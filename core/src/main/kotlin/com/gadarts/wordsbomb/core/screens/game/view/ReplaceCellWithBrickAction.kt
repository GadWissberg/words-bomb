package com.gadarts.wordsbomb.core.screens.game.view

import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.ui.Cell
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.gadarts.wordsbomb.core.model.view.Brick

class ReplaceCellWithBrickAction(
    private val table: Table,
    private val brick: Brick,
    private val index: Int
) :
    Action() {
    override fun act(delta: Float): Boolean {
        val cell = table.cells[index] as Cell
        cell.setActor(brick)
        return true
    }

}
