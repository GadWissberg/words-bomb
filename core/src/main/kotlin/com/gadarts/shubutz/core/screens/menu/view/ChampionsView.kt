package com.gadarts.shubutz.core.screens.menu.view

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.gadarts.shubutz.core.AndroidInterface

class ChampionsView(font: BitmapFont, androidInterface: AndroidInterface) : Table() {
    init {
        androidInterface.fetchChampions(object : OnChampionsFetched {

            override fun run(champion: Champion) {
                add(
                    Label(
                        "${champion.difficulty}-${champion.name}-${champion.score}",
                        Label.LabelStyle(font, Color.WHITE)
                    )
                )
            }
        })
    }
}

