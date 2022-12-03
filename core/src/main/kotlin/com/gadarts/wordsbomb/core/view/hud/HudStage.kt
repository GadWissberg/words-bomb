package com.gadarts.wordsbomb.core.view.hud

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Gdx.input
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions.*
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.Table.Debug
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.utils.viewport.FitViewport
import com.gadarts.wordsbomb.core.DefaultGameSettings
import com.gadarts.wordsbomb.core.GameStage
import com.gadarts.wordsbomb.core.model.assets.GameAssetManager
import com.gadarts.wordsbomb.core.model.assets.TexturesDefinitions
import com.gadarts.wordsbomb.core.controller.GameLogicManagerEventsSubscriber
import com.gadarts.wordsbomb.core.controller.TurnsManager
import com.gadarts.wordsbomb.core.model.Player
import com.gadarts.wordsbomb.core.view.Brick
import com.gadarts.wordsbomb.core.view.StagesCommon
import com.gadarts.wordsbomb.core.view.board.BoardCell
import com.gadarts.wordsbomb.core.view.board.BoardStageEventsSubscriber
import com.gadarts.wordsbomb.core.view.hud.HudStageLabels.Companion.CASHIER_LABEL

/**
 * Represents the HUD.
 */
class HudStage(
    private val assetsManager: GameAssetManager,
    private val turnsManager: TurnsManager,
    private val common: StagesCommon,
    spriteBatch: SpriteBatch,
) :
    GameStage<HudStageEventsSubscriber>(
        FitViewport(
            Gdx.graphics.width.toFloat(),
            Gdx.graphics.height.toFloat()
        ),
        spriteBatch,
        assetsManager
    ), BoardStageEventsSubscriber, GameLogicManagerEventsSubscriber {

    private lateinit var labels: HudStageLabels
    private val fontData = HudStageFontData()
    private lateinit var goButton: TextButton
    private lateinit var bottomHudImage: Image
    private lateinit var bottomHud: DeckTable
    override val subscribers = HashSet<HudStageEventsSubscriber>()

    private fun createLetterBrick(
        letter: String,
        brickTexture: Texture,
    ): Brick {
        val glyphLayout = fontData.glyphLayout
        val letterSize = Vector2(glyphLayout.width, glyphLayout.height)
        val brick = Brick(letter, brickTexture, letterSize, fontData.font80, fontData.font40)
        brick.touchable = Touchable.enabled
        brick.addListener(object : ClickListener() {
            override fun touchDown(
                event: InputEvent?,
                x: Float,
                y: Float,
                pointer: Int,
                button: Int
            ): Boolean {
                if (brick.ready) {
                    onBrickTouch(brick)
                }
                return super.touchDown(event, x, y, pointer, button)
            }
        })
        brick.setOrigin(brick.width / 2F, brick.height / 2F)
        return brick
    }

    private fun addLettersToBottomHudActor(
        assetsManager: GameAssetManager,
        player: Player,
        screenTable: Table
    ) {
        screenTable.debug = DefaultGameSettings.SHOW_UI_BORDERS
        addLettersToBottomHud(bottomHud, assetsManager, player)
    }

    private fun createDeckImage(texture: Texture?) {
        bottomHudImage = Image(
            NinePatchDrawable(
                NinePatch(
                    texture,
                    HUD_NINE_PATCH_PAD_SMALL,
                    HUD_NINE_PATCH_PAD_SMALL,
                    HUD_NINE_PATCH_PAD_TOP,
                    HUD_NINE_PATCH_PAD_SMALL
                )
            )
        )
    }

    private fun displayGoConfirmationDialog() {
        showDialog(
            MSG_ARE_YOU_SURE,
            object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    goRequestInitiated()
                }
            },
            DIALOG_BUTTON_YES,
            DIALOG_BUTTON_NO
        )
    }

    private fun goRequestInitiated() {
        goButton.isDisabled = true
        subscribers.forEach { it.onGoRequest() }
    }

    private fun createScreenTable(): Table {
        val screenTable = Table()
        screenTable.debug(if (DefaultGameSettings.SHOW_UI_BORDERS) Debug.all else Debug.none)
        screenTable.setFillParent(true)
        screenTable.setSize(width, height)
        return screenTable
    }

    private fun addDeck(
        screenTable: Table
    ) {
        bottomHud = DeckTable()
        bottomHud.debug = DefaultGameSettings.SHOW_UI_BORDERS
        bottomHud.setSize(width, bottomHud.prefHeight)
        bottomHud.touchable = Touchable.enabled
        bottomHud.addListener(object : ClickListener() {
            override fun touchDown(
                event: InputEvent?,
                x: Float,
                y: Float,
                pointer: Int,
                button: Int
            ): Boolean {
                return super.touchDown(event, x, y, pointer, button)
            }

            override fun touchDragged(event: InputEvent?, x: Float, y: Float, pointer: Int) {
                super.touchDragged(event, x, y, pointer)
            }
        })
        screenTable.add(Image()).grow().row()
        screenTable.add(bottomHud).padBottom(DECK_Y).row()
    }

    private fun addLettersToBottomHud(table: Table, am: GameAssetManager, player: Player) {
        val brickTexture = am.get(TexturesDefinitions.BRICK.getPath(), Texture::class.java)
        for (i in 0 until player.maxLetters) {
            addLetterToBottomHud(player, brickTexture, table, i)
        }
        bottomHud.row()
    }

    private fun addLetterToBottomHud(
        player: Player,
        brickTexture: Texture,
        table: Table,
        i: Int
    ) {
        val brick = createLetterBrick(player.letters[i].toString(), brickTexture)
        applyBrickAnimations(brick, i)
        table.add(brick).size(brick.prefWidth, brick.prefHeight).pad(LETTER_BRICKS_PADDING)
        brick.toFront()
    }

    private fun applyBrickAnimations(brick: Brick, index: Int) {
        brick.addAction(
            parallel(
                sequence(
                    scaleTo(0F, 0F, 0F),
                    delay(index * 0.5F),
                    parallel(
                        sequence(
                            scaleBy(2F, 2F, 1F, Interpolation.bounce),
                            scaleTo(1F, 1F, 1F, Interpolation.bounce)
                        ),
                        sequence(
                            delay(0.5F),
                            rotateBy(22F, 0.5F, Interpolation.circle),
                            rotateBy(-44F, 0.5F, Interpolation.circle),
                            rotateBy(22F, 0.5F, Interpolation.circle),
                        ),
                    ),
                    Brick.DisplayLetterAction(),
                ), createIdleAction()
            )
        )
    }

    private fun createIdleAction(): Action {
        return sequence(
            delay(5F),
            forever(
                sequence(
                    delay(MathUtils.random(10, 20).toFloat()),
                    scaleBy(0.1F, 0.1F, IDLE_ACTION_DURATION_ENLARGE, Interpolation.exp10),
                    scaleTo(1F, 1F, IDLE_ACTION_DURATION_SHRINK, Interpolation.bounce),
                )
            )
        )
    }

    /**
     * Sets the selected brick as cursor.
     */
    fun onBrickTouch(brick: Brick) {
        if (!turnsManager.myTurn) return
        common.brickCursor.brick = brick
    }

    override fun touchUp(x: Int, y: Int, pointer: Int, button: Int): Boolean {
        val actor = hit(x.toFloat(), height - y.toFloat(), true)
        val brick = common.brickCursor.brick
        if ((actor == bottomHud || actor is Brick || actor == bottomHudImage) && brick != null) {
            onPlacedBrickTakenOutOfBoard(brick)
        }
        if (brick != null) {
            subscribers.forEach { subscriber -> subscriber.onBrickDropped(x, y) }
            common.brickCursor.brick = null
        }
        return super.touchUp(x, y, pointer, button)
    }

    override fun draw() {
        super.draw()
        if (common.brickCursor.brick != null) {
            batch.begin()
            drawSelectedLetter()
            batch.end()
        }
    }

    private fun drawSelectedLetter() {
        val stageCoords = screenToStageCoordinates(
            auxVector.set(
                input.getX(0).toFloat(),
                input.getY(0).toFloat()
            )
        )
        val brickTexture =
            assetsManager.get(TexturesDefinitions.BRICK.getPath(), Texture::class.java)
        val x = stageCoords.x - brickTexture.width
        val y = stageCoords.y - brickTexture.height
        batch.setColor(batch.color.r, batch.color.g, batch.color.b, CURSOR_BRICK_OPACITY)
        batch.draw(brickTexture, x, y, brickTexture.width * 2F, brickTexture.height * 2F)
        common.brickCursor.brick?.drawLetter(batch, x, y, 2F, fontData.font80)
        batch.setColor(batch.color.r, batch.color.g, batch.color.b, 1F)
    }

    override fun onPlacedBrickTakenOutOfBoard(brickToReturn: Brick) {
        brickToReturn.remove()
        bottomHud.insertBrick(brickToReturn)
        common.draftBricks.remove(brickToReturn)
        brickToReturn.addAction(createIdleAction())
        subscribers.forEach { it.onPlacedBrickReturnedBackToDeck(brickToReturn) }
    }

    override fun onPlacedBrickTouched(brick: Brick) {
        common.brickCursor.brick = brick
    }

    override fun onBrickPlacedInBoard(brick: Brick) {
    }

    override fun onPlacedBrickMovedToAnotherCell(
        oldCell: BoardCell?,
        newCell: BoardCell,
        brick: Brick
    ) {

    }

    override fun onNewLettersFetched(newLetters: ArrayList<Char>) {
        val texture = assetsManager.get(TexturesDefinitions.BRICK.getPath(), Texture::class.java)
        newLetters.forEach {
            val createLetterBrick = createLetterBrick(it.toString(), texture)
            createLetterBrick.ready = true
            bottomHud.insertBrick(createLetterBrick)
        }
    }

    override fun onGameBegin(player: Player, playersNames: List<String>) {
        viewport.apply()
        fontData.init(assetsManager)
        val screenTable = createScreenTable()
        val topHudTable = Table()
        labels = HudStageLabels()
        labels.onGameBegin(fontData, playersNames, player.name)
        topHudTable.add(labels.scoreLabel)
            .pad(HUD_NINE_PATCH_PAD_SMALL.toFloat())
            .row()
        labels.playersNamesLabels.forEach {
            topHudTable.add(it)
                .pad(HUD_NINE_PATCH_PAD_SMALL.toFloat())
                .row()
        }
        screenTable.add(topHudTable).row()
        addDeck(screenTable)
        val texture = assetsManager.get(TexturesDefinitions.HUD.getPath(), Texture::class.java)
        createDeckImage(texture)
        bottomHudImage.setSize(Gdx.graphics.width.toFloat(), bottomHudImage.height)
        addActor(bottomHudImage)
        addLettersToBottomHudActor(assetsManager, player, screenTable)
        bottomHud.add(labels.cashierLabel)
            .colspan(player.maxLetters)
            .pad(LETTER_BRICKS_PADDING)
            .row()
        val up = assetsManager.getTexture(TexturesDefinitions.GO_BUTTON_UP)
        val down = assetsManager.getTexture(TexturesDefinitions.GO_BUTTON_DOWN)
        val disabled = assetsManager.getTexture(TexturesDefinitions.GO_BUTTON_DISABLED)
        goButton = addButton(
            bottomHud,
            object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    if (goButton.isDisabled) return
                    if (common.draftBricks.isEmpty()) {
                        displayGoConfirmationDialog()
                    } else {
                        goRequestInitiated()
                    }
                }
            },
            GO_BUTTON_LABEL.reversed(),
            false,
            player.maxLetters,
            up,
            down,
            disabled,
            fontData.font80,
            GO_BUTTON_PAD_TOP
        )
        goButton.isDisabled = !DefaultGameSettings.SKIP_MENU
        goButton.toBack()
        addActor(screenTable)
        subscribers.forEach { it.onHudStageInitializedForGame(bottomHudImage) }
    }

    override fun onPlayerFinishedTurn(
        draftBricks: HashSet<Brick>,
        success: Boolean,
    ) {
        if (success) return
        draftBricks.forEach {
            it.remove()
            bottomHud.insertBrick(it)
        }
    }

    override fun onRivalChangedBrickOnBoard(
        letter: Int,
        row: Int,
        col: Int,
        draftBricks: HashSet<Brick>
    ) {
    }

    override fun onMyTurn() {
        goButton.isDisabled = false
    }

    override fun onScoreUpdated(updatedScore: Int) {
        labels.scoreLabel.setText(updatedScore.toString())
    }

    override fun onCashierUpdated(cashierSize: Int) {
        labels.cashierLabel.setText(cashierSize.toString() + CASHIER_LABEL.reversed())
    }

    override fun onGameFinished() {
    }

    companion object {

        /**
         * Letter's padding inside the brick.
         */
        const val LETTER_BRICKS_PADDING = 5F
        private const val CURSOR_BRICK_OPACITY = 0.5F
        private val auxVector = Vector2()
        private const val DECK_Y = 40F
        private const val MSG_ARE_YOU_SURE = "לא שמת אף אות! לסיים בכל-זאת?"
        private const val DIALOG_BUTTON_YES = "כן"
        private const val DIALOG_BUTTON_NO = "לא"
        private const val HUD_NINE_PATCH_PAD_SMALL = 10
        private const val HUD_NINE_PATCH_PAD_TOP = 150
        private const val IDLE_ACTION_DURATION_SHRINK = 10F
        private const val IDLE_ACTION_DURATION_ENLARGE = 10F
        private const val GO_BUTTON_PAD_TOP = 30F
        private const val GO_BUTTON_LABEL = "שחק"

    }
}