package com.gadarts.shubutz.core

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import java.io.OutputStreamWriter

class GeneralUtils {
    companion object {
        fun resetDisplay(@Suppress("SameParameterValue") color: Color) {
            Gdx.gl.glViewport(0, 0, Gdx.graphics.width, Gdx.graphics.height)
            var coveSampling = 0
            if (Gdx.graphics.bufferFormat.coverageSampling) {
                coveSampling = GL20.GL_COVERAGE_BUFFER_BIT_NV
            }
            Gdx.gl.glClearColor(color.r, color.g, color.b, 1f)
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT or coveSampling)
        }

        fun writeMessage(writer: OutputStreamWriter, request: Int, message: String) {
            writer.append(request.toChar()).append(message).flush()
        }
    }

}
