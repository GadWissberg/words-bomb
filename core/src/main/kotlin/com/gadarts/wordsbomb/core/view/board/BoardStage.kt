package com.gadarts.wordsbomb.core.view.board

import com.badlogic.gdx.Gdx.graphics
import com.badlogic.gdx.Gdx.input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.utils.viewport.FitViewport
import com.gadarts.wordsbomb.core.GameStage
import com.gadarts.wordsbomb.core.GeneralUtils
import com.gadarts.wordsbomb.core.model.assets.GameAssetManager
import com.gadarts.wordsbomb.core.model.assets.TexturesDefinitions
import com.gadarts.wordsbomb.core.controller.GameLogicManagerEventsSubscriber
import com.gadarts.wordsbomb.core.model.Player
import com.gadarts.wordsbomb.core.view.Brick
import com.gadarts.wordsbomb.core.view.StagesCommon
import com.gadarts.wordsbomb.core.view.hud.HudStageEventsSubscriber
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

/**
 * Represents the game's board.
 */
class BoardStage(
    private val stagesCommon: StagesCommon,
    private val assetsManager: GameAssetManager,
    spriteBatch: SpriteBatch,
    private val cameraManipulator: CameraManipulator,
) :
    GameStage<BoardStageEventsSubscriber>(
        FitViewport(
            graphics.width.toFloat(),
            graphics.height.toFloat()
        ), spriteBatch, assetsManager
    ),
    HudStageEventsSubscriber, GestureDetector.GestureListener, GameLogicManagerEventsSubscriber {


    private var glyphLayout: GlyphLayout
    private var fontData = BoardStageFontData(assetsManager)
    private var board = GameBoard()
    private var lastLightUpActor: Actor? = null
    private val prevTouch = Vector3()
    override val subscribers = HashSet<BoardStageEventsSubscriber>()

    init {
        glyphLayout = GlyphLayout(fontData.font80, "א")
    }

    override fun draw() {
        GeneralUtils.resetDisplay(BACKGROUND_COLOR)
        super.draw()
    }

    private fun handlePanning(
        cam: OrthographicCamera,
        screenX: Float,
        screenY: Float,
        deltaX: Float,
        deltaY: Float
    ) {
        if (MathUtils.isEqual(cam.zoom, cameraManipulator.zoomManipulator.zoomTarget, 0.1F)) {
            if (cam.zoom > CameraManipulator.MIN_ZOOM) {
                auxVector3.set(screenX, screenY, 0F).add(deltaX, deltaY, 0F)
                camera.position.sub(deltaX, -deltaY, 0F)
                clampScrolling()
                prevTouch.set(screenX, height - screenY, 0F)
            }
        }
    }

    private fun clampScrolling() {
        val pos = camera.position
        val halfWidth = board.boardTable.prefWidth / 2F
        val halfHeight = board.boardTable.prefHeight / 2F
        val x = board.boardTable.x
        val y = board.boardTable.y
        pos.x = MathUtils.clamp(pos.x, x - halfWidth, x + halfWidth)
        pos.y = MathUtils.clamp(pos.y, y - halfHeight, y + halfHeight)
    }

    override fun onBrickDropped(screenX: Int, screenY: Int) {
        auxVector2.set(screenX.toFloat(), screenY.toFloat())
        screenToStageCoordinates(auxVector2)
        val actor = hit(auxVector2.x, auxVector2.y, true)
        if (actor != null) {
            if (actor is Brick) {
                replaceBrickInPlace(actor)
            } else {
                placeBrickInBoard(actor as BoardCell)
            }
        }
    }

    override fun onGoRequest() {

    }

    override fun onPlacedBrickReturnedBackToDeck(brickToReturn: Brick) {

    }

    override fun onHudStageInitializedForGame(bottomHudImage: Image) {
        val halfBoardHeight = board.boardTable.prefHeight / 2F
        val y = (bottomHudImage.prefHeight - (graphics.height / 2F - halfBoardHeight)) / 2F
        camera.position.sub(0F, y, 0F)
        camera.update()
    }

    private fun replaceBrickInPlace(brickInPlace: Brick) {
        val temp = brickInPlace.onCell
        placedBrickReturned(brickInPlace)
        if (temp != null) {
            placeBrickInBoard(temp)
        }
    }

    private fun placedBrickReturned(brickToReturn: Brick) {
        brickToReturn.remove()
        stagesCommon.draftBricks.remove(brickToReturn)
        brickToReturn.draft = false
        subscribers.forEach { it.onPlacedBrickTakenOutOfBoard(brickToReturn) }
        brickToReturn.onCell = null
    }

    private fun placeBrickInBoard(cell: BoardCell) {
        val brick = stagesCommon.brickCursor.brick
        if (brick != null) {
            addBrickToStage(brick, cell)
            brick.addAction(Actions.scaleTo(1F, 1F, 1F, Interpolation.bounce))
            cameraManipulator.onPlaceBrickInBoard(brick, viewport.camera as OrthographicCamera)
            stagesCommon.draftBricks.add(brick)
            cameraManipulator.repositionCameraIfNeeded(viewport.camera as OrthographicCamera, brick)
            subscribers.forEach { it.onBrickPlacedInBoard(brick) }
        }
    }

    private fun addBrickToStage(
        brick: Brick,
        cell: BoardCell
    ) {
        brick.clearActions()
        brick.remove()
        brick.setPosition(cell.x, cell.y)
        if (brick.onCell != null) {
            subscribers.forEach { it.onPlacedBrickMovedToAnotherCell(brick.onCell, cell, brick) }
        }
        brick.onCell = cell
        brick.draft = true
        addActor(brick)
    }

    override fun act(delta: Float) {
        super.act(delta)
        val orthographicCamera = camera as OrthographicCamera
        cameraManipulator.act(orthographicCamera, delta)
        if (lastLightUpActor != null) {
            lastLightUpActor!!.color = Color.WHITE
            lastLightUpActor = null
        }
        handleHighlightingHoverCell()
    }

    private fun handleHighlightingHoverCell() {
        if (stagesCommon.brickCursor.brick != null) {
            val c = screenToStageCoordinates(auxVector2.set(input.x.toFloat(), input.y.toFloat()))
            val halfWidth = board.boardTable.prefWidth / 2F
            val halfHeight = board.boardTable.prefHeight / 2F
            if (c.x >= board.boardTable.x - halfWidth && c.x <= board.boardTable.x + halfWidth) {
                if (c.y >= board.boardTable.y - halfHeight && c.y <= board.boardTable.y + halfHeight) {
                    highlightCurrentCell()
                }
            }
        }
    }

    private fun highlightCurrentCell() {
        lastLightUpActor = hit(auxVector2.x, auxVector2.y, true)
        if (lastLightUpActor != null) {
            lastLightUpActor!!.color = Color.DARK_GRAY
        }
    }

    override fun touchDown(screenX: Float, screenY: Float, pointer: Int, button: Int): Boolean {
        if (cameraManipulator.zoomManipulator.zooming) return false
        prevTouch.set(screenX, height - screenY, 0F)
        val touchDown = super.touchDown(screenX.toInt(), screenY.toInt(), pointer, button)
        val hit = hit(screenX, screenY, false)
        if (hit != null) {
            if (hit is Brick) {
                subscribers.forEach { sub -> sub.onPlacedBrickTouched(hit) }
            }
        }
        return touchDown

    }

    override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
        return false
    }

    override fun longPress(x: Float, y: Float): Boolean {
        return false
    }

    override fun fling(velocityX: Float, velocityY: Float, button: Int): Boolean {
        return false
    }

    override fun pan(x: Float, y: Float, deltaX: Float, deltaY: Float): Boolean {
        if (cameraManipulator.zoomManipulator.zooming || stagesCommon.brickCursor.brick != null) return false
        val orthographicCamera = camera as OrthographicCamera
        handlePanning(orthographicCamera, x, y, deltaX, deltaY)
        return super.touchDragged(x.toInt(), y.toInt(), 0)
    }

    override fun panStop(x: Float, y: Float, pointer: Int, button: Int): Boolean {
        return false
    }

    override fun zoom(initialDist: Float, distance: Float): Boolean {
        val orthographicCamera = camera as OrthographicCamera
        cameraManipulator.onZoomInput(
            initialDist,
            distance,
            orthographicCamera,
            board.boardTable.x + board.boardTable.width,
            board.boardTable.y
        )
        return true
    }

    override fun pinch(
        initialPointer1: Vector2?,
        initialPointer2: Vector2?,
        pointer1: Vector2?,
        pointer2: Vector2?
    ): Boolean {
        cameraManipulator.onPinchInput(camera as OrthographicCamera)
        return true
    }

    override fun pinchStop() {
        cameraManipulator.onPinchStopInput()
    }

    /**
     * Adds board table and initializes camera.
     */
    fun init() {
        board.init(assetsManager)
        cameraManipulator.init(camera as OrthographicCamera)
        camera.rotate(Vector3.Z, 180F)
        camera.position.set(0F, 0F, 0F)
        addActor(board.boardTable)
    }

    override fun onNewLettersFetched(newLetters: ArrayList<Char>) {
    }

    override fun onGameBegin(player: Player, playersNames: List<String>) {
    }


    override fun onPlayerFinishedTurn(
        draftBricks: HashSet<Brick>,
        success: Boolean,
    ) {
        cameraManipulator.zoomManipulator.animateZoom(null, camera as OrthographicCamera)
    }

    override fun onRivalChangedBrickOnBoard(
        letter: Int,
        row: Int,
        col: Int,
        draftBricks: HashSet<Brick>
    ) {
        if (letter < Player.allowedLetters.length) {
            rivalAddedBrickToBoard(letter, row, col, draftBricks)
        } else {
            val boardCell = board.boardMatrix[row][col].actor
            hit(boardCell.x, boardCell.y, true).remove()
        }
    }

    override fun onMyTurn() {

    }

    override fun onScoreUpdated(updatedScore: Int) {
    }

    override fun onCashierUpdated(cashierSize: Int) {
    }

    override fun onGameFinished() {
    }

    private fun rivalAddedBrickToBoard(
        letter: Int,
        row: Int,
        col: Int,
        draftBricks: HashSet<Brick>
    ) {
        val brick = createRivalBrick(letter)
        draftBricks.add(brick)
        addBrickToStage(brick, board.boardMatrix[row][col].actor)
    }

    private fun createRivalBrick(letter: Int): Brick {
        return Brick(
            Player.allowedLetters[letter].toString(),
            assetsManager.getTexture(TexturesDefinitions.BRICK),
            Vector2(glyphLayout.width, glyphLayout.height),
            fontData.font80,
            fontData.font40,
            ready = true
        )
    }

    companion object {

        private val BACKGROUND_COLOR = Color.valueOf("#c9d8b6")
        private val auxVector3 = Vector3()
        private val auxVector2 = Vector2()

    }
}