package com.gadarts.shubutz.core.screens.menu.view

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Stack
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.TimeUtils
import com.gadarts.shubutz.core.AndroidInterface
import com.gadarts.shubutz.core.ShubutzGame.Companion.lastChampionsFetch
import com.gadarts.shubutz.core.model.GameModes
import com.gadarts.shubutz.core.screens.game.view.LoadingAnimation

class ChampionsView(
    font: BitmapFont,
    private val androidInterface: AndroidInterface,
    highscoresIconTexture: Texture,
    loadingAtlas: TextureAtlas,
    buttonArrowTextureUp: Texture,
    buttonArrowTextureDown: Texture
) : Table() {
    private var rightCup: Image
    private var leftCup: Image
    private var rightArrow: Button
    private var leftArrow: Button
    private var currentDisplayed = randomDifficulty()
    private val labels = ChampionsViewLabels()

    init {
        val stack = Stack()
        val loadingAnimation = LoadingAnimation(loadingAtlas.regions)
        stack.add(loadingAnimation)
        val viewTable = Table()
        leftArrow =
            addArrowButton(buttonArrowTextureUp, buttonArrowTextureDown, viewTable, stack, true)
        add(stack)
        leftCup = addCup(highscoresIconTexture, viewTable)
        val labelsTable = labels.addLabels(font, viewTable, androidInterface)
        rightCup = addCup(highscoresIconTexture, viewTable)
        rightArrow = addArrowButton(buttonArrowTextureUp, buttonArrowTextureDown, viewTable, stack)
        stack.add(viewTable)
        if (shouldUpdateCache()) {
            updateView(loadingAnimation, labelsTable, leftCup, rightCup, stack)
        } else {
            viewReady(loadingAnimation, labelsTable, leftCup, rightCup, stack)
        }
    }

    private fun updateView(
        loadingAnimation: LoadingAnimation,
        labelsTable: Table,
        leftCup: Image,
        rightCup: Image,
        stack: Stack
    ) {
        lastChampionsFetch = TimeUtils.millis()
        GameModes.values().forEach {
            androidInterface.fetchChampion(it, object : OnChampionFetched {
                override fun run(champion: Champion?) {
                    if (parent != null && champion != null) {
                        champions[it] = champion
                        viewReady(loadingAnimation, labelsTable, leftCup, rightCup, stack)
                    }
                }
            })
        }
    }

    private fun addArrowButton(
        arrowTexUp: Texture,
        arrowTexDown: Texture,
        labelsTable: Table,
        viewStack: Stack,
        horizontalFlip: Boolean = false
    ): Button {
        val arrowButton =
            Button(TextureRegionDrawable(arrowTexUp), TextureRegionDrawable(arrowTexDown))
        arrowButton.isTransform = true
        if (horizontalFlip) {
            flipArrow(arrowButton, arrowTexUp)
        }
        arrowButton.isVisible = false
        add(arrowButton)
        arrowButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                viewStack.clearActions()
                viewStack.color.a = 1F
                val difficulties = GameModes.values()
                val indexOf = difficulties.indexOf(GameModes.valueOf(currentDisplayed.name))
                currentDisplayed = difficulties[if (horizontalFlip && indexOf == 0) {
                    difficulties.size - 1
                } else {
                    (indexOf + (if (horizontalFlip) -1 else 1)) % (champions.size)
                }]
                displayCurrent(labelsTable, leftCup, rightCup)
            }
        })
        return arrowButton
    }

    private fun flipArrow(
        arrowButton: Button,
        arrowTexUp: Texture
    ) {
        arrowButton.setScale(-1F, 1F)
        arrowButton.setOrigin(arrowTexUp.width.toFloat() / 2F, arrowButton.originY)
    }

    private fun shouldUpdateCache() =
        champions.size < GameModes.values().size || TimeUtils.timeSinceMillis(lastChampionsFetch) >= CACHE_TTL

    private fun viewReady(
        loadingAnimation: LoadingAnimation,
        labelsTable: Table,
        leftCup: Image,
        rightCup: Image,
        viewStack: Stack
    ) {
        if (champions.size == GameModes.values().filter { it.leaderboardsId != null }.size) {
            loadingAnimation.remove()
            displayCurrent(
                labelsTable,
                leftCup,
                rightCup,
            )
            beginCycle(viewStack, labelsTable)
        }
    }

    private fun beginCycle(
        viewStack: Stack,
        labelsTable: Table
    ) {
        viewStack.clearActions()
        viewStack.addAction(
            Actions.forever(
                Actions.sequence(
                    Actions.delay(INTERVAL),
                    cycleDifficultySequence(Actions.run {
                        currentDisplayed = randomDifficulty()
                        displayCurrent(labelsTable, leftCup, rightCup)
                    })
                )
            )
        )
    }

    private fun cycleDifficultySequence(
        difficultySelection: RunnableAction,
    ): SequenceAction? = Actions.sequence(
        Actions.fadeOut(1F, Interpolation.smooth2),
        difficultySelection,
        Actions.fadeIn(1F, Interpolation.smooth2)
    )

    private fun randomDifficulty(): GameModes {
        val difficulties = GameModes.values()
        return difficulties[MathUtils.random(difficulties.size - 1)]
    }


    private fun displayCurrent(
        labelsTable: Table,
        leftCup: Image,
        rightCup: Image,
    ) {
        if (!champions.containsKey(currentDisplayed)) return

        val champion = champions[currentDisplayed]
        labels.updateTexts(champion)
        labelsTable.isVisible = true
        leftCup.isVisible = true
        rightCup.isVisible = true
        rightArrow.isVisible = true
        leftArrow.isVisible = true
        if (listeners.isEmpty) {
            labelsTable.addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    super.clicked(event, x, y)
                    currentDisplayed.leaderboardsId?.let { androidInterface.displayLeaderboard(it) }
                }
            }
            )
        }
    }


    private fun addCup(highscoresIconTexture: Texture, table: Table): Image {
        val cup = Image(highscoresIconTexture)
        addCupAnimation(cup)
        table.add(cup)
        cup.isVisible = false
        return cup
    }

    private fun addCupAnimation(cup: Image) {
        cup.addAction(
            Actions.forever(
                Actions.sequence(
                    Actions.moveBy(
                        0F,
                        CUP_STEP_SIZE,
                        2F,
                        Interpolation.sineOut
                    ), Actions.moveBy(
                        0F,
                        -CUP_STEP_SIZE * 2F,
                        4F,
                        Interpolation.sine
                    ), Actions.moveBy(
                        0F,
                        CUP_STEP_SIZE,
                        2F,
                        Interpolation.sineIn
                    )
                )
            )
        )
    }

    companion object {
        private const val CUP_STEP_SIZE = 32F
        private const val INTERVAL = 5F
        private const val CACHE_TTL = 10 * 60 * 1000
        private val champions = HashMap<GameModes, Champion>()
    }
}

