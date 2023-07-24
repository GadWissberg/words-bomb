package com.gadarts.shubutz.core.screens.menu.view

import com.badlogic.gdx.graphics.Color
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
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Stack
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.TimeUtils
import com.gadarts.shubutz.core.AndroidInterface
import com.gadarts.shubutz.core.ShubutzGame.Companion.lastChampionsFetch
import com.gadarts.shubutz.core.model.Difficulties
import com.gadarts.shubutz.core.screens.game.view.GameLabel
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
    private var stopAutoCycle: Boolean = false
    private var rightArrow: Button
    private var leftArrow: Button
    private var currentDisplayed = randomDifficulty()
    private lateinit var scoreLabel: Label
    private lateinit var nameLabel: Label
    private lateinit var headerLabel: Label

    init {
        val stack = Stack()
        val loadingAnimation = LoadingAnimation(loadingAtlas.regions)
        stack.add(loadingAnimation)
        val viewTable = Table()
        leftArrow =
            addArrowButton(buttonArrowTextureUp, buttonArrowTextureDown, viewTable, stack, true)
        leftCup = addCup(highscoresIconTexture, viewTable)
        val labelsTable = addLabels(font, viewTable)
        rightCup = addCup(highscoresIconTexture, viewTable)
        rightArrow = addArrowButton(
            buttonArrowTextureUp,
            buttonArrowTextureDown,
            viewTable,
            stack,
        )
        stack.add(viewTable)
        add(stack)
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
        Difficulties.values().forEach {
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
        viewTable: Table,
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
        viewTable.add(arrowButton)
        arrowButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                super.clicked(event, x, y)
                if ((!stopAutoCycle && viewStack.hasActions()) || stopAutoCycle) {
                    viewStack.clearActions()
                    stopAutoCycle = true
                    arrowButton.addAction(cycleDifficultySequence(Actions.run {
                        currentDisplayed = Difficulties.values()[(Difficulties.values()
                            .indexOf(Difficulties.valueOf(currentDisplayed.name)) + 1) % (champions.size)]
                        displayCurrent(viewTable, leftCup, rightCup)
                    }))
                }
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
        champions.size < Difficulties.values().size || TimeUtils.timeSinceMillis(lastChampionsFetch) >= CACHE_TTL

    private fun viewReady(
        loadingAnimation: LoadingAnimation,
        labelsTable: Table,
        leftCup: Image,
        rightCup: Image,
        viewStack: Stack
    ) {
        if (champions.size == Difficulties.values().size) {
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

    private fun randomDifficulty(): Difficulties {
        val difficulties = Difficulties.values()
        return difficulties[MathUtils.random(difficulties.size - 1)]
    }


    private fun displayCurrent(
        labelsTable: Table,
        leftCup: Image,
        rightCup: Image,
    ) {
        if (!champions.containsKey(currentDisplayed)) return

        val champion = champions[currentDisplayed]
        headerLabel.setText(
            TEXT_HEADER.format(champion!!.difficulty.displayName).reversed()
        )
        nameLabel.setText(champion.name)
        scoreLabel.setText(
            TEXT_SCORE.format(champion.score.toString().reversed()).reversed()
        )
        labelsTable.isVisible = true
        leftCup.isVisible = true
        rightCup.isVisible = true
        rightArrow.isVisible = true
        leftArrow.isVisible = true
        if (listeners.isEmpty) {
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    super.clicked(event, x, y)
                    androidInterface.displayLeaderboard(currentDisplayed.leaderboardsId)
                }
            }
            )
        }
    }

    private fun addLabels(font: BitmapFont, viewTable: Table): Table {
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
        private const val TEXT_HEADER = "אלוף ברמת %s:"
        private const val TEXT_SCORE = "עם %s נקודות!"
        private const val TEXT_CLICK_FOR_MORE = "לחצו לטבלה המלאה..."
        private const val CUP_STEP_SIZE = 32F
        private const val INTERVAL = 5F
        private const val CACHE_TTL = 10 * 60 * 1000
        private val champions = HashMap<Difficulties, Champion>()
    }
}

