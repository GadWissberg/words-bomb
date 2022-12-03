package com.gadarts.wordsbomb.core.view.board

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Interpolation.smooth
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.BoundingBox
import com.gadarts.wordsbomb.core.view.Brick
import com.gadarts.wordsbomb.core.view.board.CameraManipulator.Companion.MIN_ZOOM

class ZoomManipulator {
    var zooming: Boolean = false

    var zoomTarget: Float = MIN_ZOOM

    private var zoomBrickTarget: Brick? = null
    private var zoomAnimationProgress: Float = 0.0f
    private var initialPinchZoom: Float = 0.0f
    private var prevZoom: Float = MIN_ZOOM

    fun animateZoom(selectedBrick: Brick?, camera: OrthographicCamera) {
        if (selectedBrick != null) {
            animateZoomOnBrick(selectedBrick, camera)
        } else {
            prevZoom = camera.zoom
            zoomTarget = MIN_ZOOM
            zoomAnimationProgress = 0F
        }
    }

    private fun animateZoomOnBrick(
        selectedBrick: Brick,
        camera: OrthographicCamera
    ) {
        val box = calculateBrickBoundingBox(selectedBrick)
        if (zoomAnimationProgress == 0F && MathUtils.isEqual(camera.zoom, MIN_ZOOM, EPSILON)) {
            val maxZoom = CameraManipulator.MAX_ZOOM - EPSILON
            if (camera.zoom < maxZoom || !camera.frustum.boundsInFrustum(box)) {
                prevZoom = MIN_ZOOM
                zoomTarget = CameraManipulator.MAX_ZOOM
                zoomBrickTarget = selectedBrick
            }
        }
    }

    fun act(orthographicCamera: OrthographicCamera, delta: Float) {
        if (zoomAnimationProgress < 1) {
            if (!zooming && !MathUtils.isEqual(zoomTarget, orthographicCamera.zoom, EPSILON)) {
                if (zoomBrickTarget != null || zoomTarget == MIN_ZOOM) {
                    actZoomAnimation(delta, orthographicCamera)
                }
            } else {
                zoomAnimationProgress = 0F
            }
        }
    }

    private fun actZoomAnimation(
        delta: Float,
        camera: OrthographicCamera,
    ) {
        zoomAnimationProgress += delta / ZOOM_DURATION_SEC
        camera.zoom = MathUtils.lerp(prevZoom, zoomTarget, smooth.apply(zoomAnimationProgress))
        actZoomAnimationPosition(camera)
        if (zoomAnimationProgress >= 1) {
            zoomBrickTarget = null
            zoomAnimationProgress = 0F
        }
    }

    private fun actZoomAnimationPosition(
        orthographicCamera: OrthographicCamera,
    ) {
        if (zoomBrickTarget != null) {
            val x = zoomBrickTarget!!.x + zoomBrickTarget!!.width / 2F
            val y = zoomBrickTarget!!.y + zoomBrickTarget!!.height / 2F
            orthographicCamera.position.lerp(
                auxVector3_1.set(x, y, 0F),
                smooth.apply(zoomAnimationProgress)
            )
        }
    }

    fun onZoomInput(camera: OrthographicCamera, initialDist: Float, distance: Float) {
        val norm = MathUtils.norm(initialDist, 0F, distance)
        val value = initialPinchZoom - norm * MANUAL_ZOOM_COEF
        val clamp = MathUtils.clamp(value, MIN_ZOOM, CameraManipulator.MAX_ZOOM)
        camera.zoom = clamp
        zoomTarget = camera.zoom
    }

    fun init(camera: OrthographicCamera) {
        camera.zoom = MIN_ZOOM
        initialPinchZoom = camera.zoom
    }

    fun onPinchInput(orthographicCamera: OrthographicCamera) {
        initialPinchZoom = orthographicCamera.zoom
        zooming = true
    }

    fun onPinchStopInput() {
        zooming = false
    }

    private fun calculateBrickBoundingBox(brick: Brick): BoundingBox {
        auxBoundingBox.set(
            auxVector3_1.set(brick.x, brick.y, 0F),
            auxVector3_2.set(brick.x + brick.width, brick.y + brick.height, 0F)
        )
        return auxBoundingBox
    }

    companion object {
        private const val MANUAL_ZOOM_COEF = 0.05F
        private const val EPSILON = 0.01F
        private const val ZOOM_DURATION_SEC = 2F
        private val auxBoundingBox = BoundingBox()
        private val auxVector3_1 = Vector3()
        private val auxVector3_2 = Vector3()

    }
}
