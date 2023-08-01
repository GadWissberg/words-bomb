package com.gadarts.shubutz.core.screens.menu.view

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.gadarts.shubutz.core.AndroidInterface
import com.gadarts.shubutz.core.screens.game.view.GameLabel

class ChampionsViewLabels {

    private var scoreLabel: Label? = null
    private var nameLabel: Label? = null
    private var headerLabel: Label? = null

    fun addLabels(font: BitmapFont, viewTable: Table, androidInterface: AndroidInterface): Table {
        val labelsTable = Table()
        val labelStyle = Label.LabelStyle(font, Color.WHITE)
        headerLabel = GameLabel("רמה", labelStyle, androidInterface)
        labelsTable.add(headerLabel).row()
        nameLabel = GameLabel("שם", labelStyle, androidInterface)
        labelsTable.add(nameLabel).row()
        scoreLabel = GameLabel("10", labelStyle, androidInterface)
        labelsTable.add(scoreLabel).row()
        val fullTable = GameLabel(TEXT_CLICK_FOR_MORE.reversed(), labelStyle, androidInterface)
        labelsTable.add(fullTable)
        viewTable.add(labelsTable)
        labelsTable.isVisible = false
        return labelsTable
    }

    fun updateTexts(champion: Champion?) {
        if (champion == null) return

        headerLabel?.setText(
            TEXT_HEADER.format(champion.gameMode.displayName).reversed()
        )
        nameLabel?.setText(champion.name)
        scoreLabel?.setText(
            TEXT_SCORE.format(champion.score.toString().reversed()).reversed()
        )
    }

    companion object {
        private const val TEXT_HEADER = "אלוף ברמת %s:"
        private const val TEXT_SCORE = "עם %s נקודות!"
        private const val TEXT_CLICK_FOR_MORE = "לחצו לטבלה המלאה..."
    }
}
