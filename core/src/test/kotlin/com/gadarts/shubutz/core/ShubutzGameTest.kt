package com.gadarts.shubutz.core

import com.gadarts.shubutz.core.screens.menu.MenuScreenImpl
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

internal class ShubutzGameTest {
    private val androidInterface = mockk<AndroidInterface>(relaxed = true)
    private val screenMock = mockk<MenuScreenImpl>(relaxed = true)
    private val subject: ShubutzGame = ShubutzGame(androidInterface)

    @org.junit.jupiter.api.Test
    fun onSuccessfulPurchaseShouldNotCallScreenWhenScreenIsNull() {
        every { screenMock.onSuccessfulPurchase(any()) } returns Unit

        val products = mutableListOf<String>()
        verify { screenMock.onSuccessfulPurchase(products) wasNot Called }
    }

    @org.junit.jupiter.api.Test
    fun onFailedPurchase() {
    }

    @org.junit.jupiter.api.Test
    fun onLeaderboardClosed() {
    }
}