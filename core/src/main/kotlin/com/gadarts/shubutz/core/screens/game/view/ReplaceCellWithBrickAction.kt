package com.gadarts.shubutz.core.screens.game.view

import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.ui.Cell
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.gadarts.shubutz.core.model.view.Brick

class ReplaceCellWithBrickAction(
    private val table: Table,
    private val brick: Brick,
    private val index: Int,
    private val maxBricksPerLine: Int
) :
    Action() {
    override fun act(delta: Float): Boolean {
        val cell = table.cells[index % maxBricksPerLine] as Cell
        cell.setActor(brick)
        return true
    }

}
