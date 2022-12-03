package com.gadarts.wordsbomb

import android.os.Bundle
import android.widget.Toast
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.gadarts.wordsbomb.core.AndroidInterface
import com.gadarts.wordsbomb.core.WordsBombGame

class AndroidLauncher : AndroidApplication(), AndroidInterface {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val config = AndroidApplicationConfiguration()
        config.numSamples = 2
        initialize(WordsBombGame(this), config)
    }

    override fun toast(msg: String) {
        runOnUiThread {
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }
}