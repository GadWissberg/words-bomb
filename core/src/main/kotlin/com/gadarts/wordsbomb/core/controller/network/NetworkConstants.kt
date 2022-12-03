package com.gadarts.wordsbomb.core.controller.network

object NetworkConstants {
    object Requests {
        const val REQUEST_CREATE_ROOM = 10
        const val REQUEST_TO_JOIN_ROOM = 11
        const val REQUEST_SUBMIT_LETTERS = 12
        const val REQUEST_LETTER_CHANGED_ON_BOARD = 13
    }

    object Responses {
        const val RESPONSE_OK = 20
        const val RESPONSE_DENIED_GENERAL = 30
        const val RESPONSE_DENIED_INVALID_FIRST_WORD = 31
        const val RESPONSE_DENIED_INVALID_LETTER_PLACING = 35
    }

    object Commands {
        const val COMMAND_BEGIN_GAME = 40
        const val COMMAND_YOUR_TURN = 41
        const val COMMAND_LETTER_PLACED = 42
        const val COMMAND_UPDATE_CASHIER = 43
        const val COMMAND_GAME_OVER = 44
    }
}