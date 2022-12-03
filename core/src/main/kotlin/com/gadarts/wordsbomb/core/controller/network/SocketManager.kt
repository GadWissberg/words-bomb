package com.gadarts.wordsbomb.core.controller.network

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Net
import com.badlogic.gdx.net.Socket
import com.badlogic.gdx.net.SocketHints
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.GdxRuntimeException
import com.gadarts.wordsbomb.core.screens.menu.MenuScreen

class SocketManager : Disposable {

    var socket: Socket? = null

    override fun dispose() {
        socket?.dispose()
    }

    @Throws(GdxRuntimeException::class)
    fun reset() {
        val socketHints = SocketHints()
        socketHints.connectTimeout =
            MenuScreen.SOCKET_TIMEOUT_SECONDS * 1000
        socket = Gdx.net.newClientSocket(
            Net.Protocol.TCP,
            MenuScreen.HOST_IP,
            1234,
            socketHints
        )
    }

}
