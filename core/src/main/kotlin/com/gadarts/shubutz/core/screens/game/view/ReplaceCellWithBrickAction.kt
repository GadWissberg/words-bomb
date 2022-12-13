package com.gadarts.shubutz.core.screens.game.view

import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Cell
import com.gadarts.shubutz.core.screens.game.view.actors.Brick

class ReplaceCellWithBrickAction(
    private val cell: Cell<Actor>,
    private val brick: Brick,
) :
    Action() {
    override fun act(delta: Float): Boolean {
        cell.setActor(brick)
        return true
    }

}
