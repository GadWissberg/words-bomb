package com.gadarts.wordsbomb.core.view.hud

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.Label

class HudStageLabels {

    lateinit var playersNamesLabels: java.util.ArrayList<Label>
    lateinit var cashierLabel: Label
    lateinit var scoreLabel: Label

    fun onGameBegin(fontData: HudStageFontData, playersNames: List<String>, name: String) {
        val labelStyle = Label.LabelStyle(fontData.font40, Color.WHITE)
        cashierLabel = Label(CASHIER_LABEL.reversed(), labelStyle)
        scoreLabel = Label(String.format("%d", 0), Label.LabelStyle(fontData.font80, Color.WHITE))
        playersNamesLabels = ArrayList()
        playersNames.forEach {
            playersNamesLabels.add(Label(if (it.isNotBlank()) it else name, labelStyle))
        }
    }

    companion object {
        const val CASHIER_LABEL = "אותיות בקופה: "
    }


}
