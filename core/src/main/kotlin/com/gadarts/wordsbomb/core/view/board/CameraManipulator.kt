package com.gadarts.wordsbomb.core.view.board

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Interpolation.smooth
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector3
import com.gadarts.wordsbomb.core.view.Brick

/**
 * Handles certain camera movements.
 */
open class CameraManipulator {

    var zoomManipulator = ZoomManipulator()
    private var moveCameraToBrick: Brick? = null
    private var moveCameraToBrickProgress = 0F

    /**
     * Initializes the camera's zoom.
     */
    fun init(camera: OrthographicCamera) {
        zoomManipulator.init(camera)
    }

    /**
     * Sets the camera to move to the outside brick.
     */
    open fun repositionCameraIfNeeded(
        camera: OrthographicCamera,
        brick: Brick
    ) {
        if (isBrickNotFullyInsideView(brick, camera)) {
            moveCameraToBrick = brick
            moveCameraToBrickProgress = 0F
        }
    }

    private fun actMoveCameraToBrick(delta: Float, cam: OrthographicCamera) {
        if (moveCameraToBrick != null) {
            val x = moveCameraToBrick!!.x + moveCameraToBrick!!.width / 2F
            val y = moveCameraToBrick!!.y + moveCameraToBrick!!.height / 2F
            cam.position.lerp(auxVector3_1.set(x, y, 0F), smooth.apply(moveCameraToBrickProgress))
            moveCameraToBrickProgress += delta
            if (moveCameraToBrickProgress > 1) {
                moveCameraToBrick = null
            }
        }
    }


    /**
     * Takes step in the zooming & moving procedures.
     */
    fun act(orthographicCamera: OrthographicCamera, delta: Float) {
        zoomManipulator.act(orthographicCamera, delta)
        actMoveCameraToBrick(delta, orthographicCamera)
    }

    private fun isBrickNotFullyInsideView(
        brick: Brick,
        camera: OrthographicCamera
    ): Boolean {
        val leftBoundary = camera.position.x - camera.viewportWidth / 2F * (camera.zoom * -1)
        val rightBoundary = camera.position.x + camera.viewportWidth / 2F * (camera.zoom * -1)
        val topBoundary = camera.position.y + camera.viewportHeight / 2F * (camera.zoom * -1)
        val bottomBoundary = camera.position.y - camera.viewportHeight / 2F * (camera.zoom * -1)
        return (brick.x < leftBoundary
                || brick.x + brick.width > rightBoundary
                || brick.y + brick.height > topBoundary
                || brick.y < bottomBoundary)
    }



    /**
     * Applies the zoom.
     */
    fun onZoomInput(
        initialDist: Float,
        distance: Float,
        camera: OrthographicCamera,
        mostRightX: Float,
        mostBottomY: Float
    ) {
        zoomManipulator.onZoomInput(camera, initialDist, distance)
        camera.position.x = calculateCoordinateByZoom(mostRightX, camera.zoom, camera.position.x)
        camera.position.y = calculateCoordinateByZoom(mostBottomY, camera.zoom, camera.position.y)
        camera.update()
    }

    private fun calculateCoordinateByZoom(
        highestCoordValue: Float,
        zoom: Float,
        coordValue: Float
    ): Float {
        val norm = MathUtils.norm(MIN_ZOOM, MAX_ZOOM, zoom)
        val half = highestCoordValue / 2F
        val min = half - half * norm
        val max = highestCoordValue - half * norm
        return MathUtils.clamp(coordValue, min, max)
    }

    /**
     * Activates the zoom.
     */
    fun onPinchInput(orthographicCamera: OrthographicCamera) {
        zoomManipulator.onPinchInput(orthographicCamera)
    }

    /**
     * Stop the zoom.
     */
    fun onPinchStopInput() {
        zoomManipulator.onPinchStopInput()
    }

    /**
     * Applies the zoom animation.
     */
    open fun onPlaceBrickInBoard(selectedBrick: Brick, orthographicCamera: OrthographicCamera) {
        zoomManipulator.animateZoom(selectedBrick, orthographicCamera)
    }


    companion object {

        /**
         * The minimum value the zoom can have.
         */
        const val MIN_ZOOM = -1.7F

        const val MAX_ZOOM = -0.8F

        private val auxVector3_1 = Vector3()
    }

}
