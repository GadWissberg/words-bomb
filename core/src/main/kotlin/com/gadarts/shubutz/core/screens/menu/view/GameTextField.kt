package com.gadarts.shubutz.core.screens.menu.view

import com.badlogic.gdx.scenes.scene2d.ui.TextField

class GameTextField(createTextFieldStyle: TextFieldStyle) : TextField("", createTextFieldStyle) {
    override fun getText(): String {
        return super.getText().reversed()
    }

    fun refreshHebrew() {
        displayText = displayText.reversed()
    }

}
