package com.gadarts.wordsbomb.core.hud

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Graphics
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.viewport.FitViewport
import com.gadarts.wordsbomb.core.model.view.Brick
import com.gadarts.wordsbomb.core.model.assets.GameAssetManager
import com.gadarts.wordsbomb.core.view.StagesCommon
import com.gadarts.wordsbomb.core.controller.TurnsManager
import com.gadarts.wordsbomb.core.view.hud.BrickCursor
import com.gadarts.wordsbomb.core.view.hud.HudStage
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.whenever
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class HudStageTest {


    private lateinit var common: StagesCommon

    @Mock
    private lateinit var brick: Brick

    @Mock
    private lateinit var fitViewport: FitViewport

    @Mock
    private lateinit var turnsManager: TurnsManager

    private lateinit var hudStage: HudStage

    @Mock
    private val assetManager = GameAssetManager()

    private val cursor = BrickCursor()
    private var draftBricks = HashSet<Brick>()

    @Mock
    private lateinit var graphics: Graphics

    @Mock
    private lateinit var spriteBatch: SpriteBatch

    @Before
    fun setUp() {
        common = StagesCommon(draftBricks, cursor)
        doNothing().`when`(fitViewport)
            .update(any(), any(), any())
        Gdx.graphics = graphics
        whenever(turnsManager.myTurn).thenReturn(true)
        doNothing().`when`(brick).remove()
        hudStage = HudStage(assetManager, turnsManager, common, spriteBatch, fitViewport)
    }

    @Test
    fun onBrickTouch() {
        hudStage.onBrickTouch(brick)

        assertEquals(brick, common.brickCursor.brick)
    }

    @Test
    fun onPlacedBrickReturned() {
        hudStage.onPlacedBrickTakenOutOfBoard(brick)

    }
}