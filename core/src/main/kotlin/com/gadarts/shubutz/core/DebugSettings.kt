package com.gadarts.shubutz.core

import com.gadarts.shubutz.core.model.Phrase

/**
 * Default flags values.
 */
object DebugSettings {

    /**
     * The given phrase to be forced.
     */
    val TEST_PHRASE = Phrase("")

    /**
     * Number of tries 'till game over.
     */
    const val NUMBER_OF_TRIES = 0

    /**
     * Whether to display the UI components borders.
     */
    const val SHOW_UI_BORDERS = false

    /**
     * Whether to play sounds.
     */
    const val ENABLE_SOUNDS = true

    /**
     * Whether to set the coins number at the start.
     */
    const val FORCE_NUMBER_OF_COINS = -1

}

