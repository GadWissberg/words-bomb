package com.gadarts.wordsbomb.core.controller.network

import com.gadarts.wordsbomb.core.AndroidInterface

interface CommandsListener {
    fun onCommandBeginGame(cashierSize: Int, letters: ArrayList<Char>, players: ArrayList<String>)
    fun onCommandYourTurn(androidInterface: AndroidInterface)
    fun onCommandLetterPlaced(letter: Int, row: Int, col: Int)
    fun onCommandUpdateCashier(cashierSize: Int)
    fun onCommandGameOver(winners: java.util.ArrayList<Int>)
}
