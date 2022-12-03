package com.gadarts.wordsbomb.core.view.board

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.ui.Cell
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.gadarts.wordsbomb.core.model.assets.GameAssetManager
import com.gadarts.wordsbomb.core.model.assets.TexturesDefinitions
import com.gadarts.wordsbomb.core.model.Room

class GameBoard {

    lateinit var boardMatrix: java.util.ArrayList<java.util.ArrayList<Cell<BoardCell>>>
    lateinit var boardTable: Table

    fun init(assetsManager: GameAssetManager) {
        boardMatrix = ArrayList()
        boardTable = Table()
        val cellTexture = assetsManager.getTexture(TexturesDefinitions.CELL)
        fillBoardTable(cellTexture)
    }

    private fun addCellsToRow(
        currentRow: ArrayList<Cell<BoardCell>>,
        row: Int,
        cellTexture: Texture
    ) {
        for (col in 0 until Room.NUMBER_OF_CELLS_IN_ROW) {
            currentRow.add(
                boardTable.add(BoardCell(row, col, cellTexture)).pad(CELL_PADDING)
                    .width(cellTexture.width.toFloat())
                    .height(cellTexture.height.toFloat())
            )
        }
    }

    private fun fillBoardTable(cellTexture: Texture) {
        for (row in 0 until Room.NUMBER_OF_CELLS_IN_ROW) {
            val currentRow = ArrayList<Cell<BoardCell>>()
            boardMatrix.add(currentRow)
            addCellsToRow(currentRow, row, cellTexture)
            boardTable.row()
        }
    }

    companion object {
        private const val CELL_PADDING = 10F

    }

}
