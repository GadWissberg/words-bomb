package com.gadarts.wordsbomb.core.view.board

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.ui.Image

/**
 * The stage actor that represents the board cell.
 */
open class BoardCell(val row: Int, val col: Int, cellTexture: Texture) : Image(cellTexture)