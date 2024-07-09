package de.fra_uas.fb2.mobiledevices.visualizetilt

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.opengl.GLES20
import android.opengl.GLUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class Axis {

    //for axis
    private val vertexShaderCode = """
        uniform mat4 uMVPMatrix;
        attribute vec4 vPosition;
        void main() {
            gl_Position = uMVPMatrix * vPosition;
        }
    """.trimIndent()

    private val fragmentShaderCode = """
        precision mediump float;
        uniform vec4 vColor;
        void main() {
            gl_FragColor = vColor;
        }
    """.trimIndent()

    //for text
    private val vertexShaderCodeForTexture = """
        uniform mat4 uMVPMatrix;
        attribute vec4 vPosition;
        attribute vec2 aTexCoord;
        varying vec2 vTexCoord;
        void main() {
            gl_Position = uMVPMatrix * vPosition;
            vTexCoord = aTexCoord;
        }
    """.trimIndent()

    private val fragmentShaderCodeForTexture = """
        precision mediump float;
        uniform sampler2D uTexture;
        varying vec2 vTexCoord;
        void main() {
            gl_FragColor = texture2D(uTexture, vTexCoord);
        }
    """.trimIndent()

    private val axisCoords = floatArrayOf(
        // X 축 (빨강)
        -1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
        1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
        // Y 축 (초록)
        0.0f, -1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
        0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
        // Z 축 (파랑)
        0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 1.0f,
        0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f
    )

    private val vertexBuffer: FloatBuffer

    private val vertexStride = 6 * 4 // 4 bytes per vertex

    private val vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
    private val fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

    private val program = GLES20.glCreateProgram().also {
        GLES20.glAttachShader(it, vertexShader)
        GLES20.glAttachShader(it, fragmentShader)
        GLES20.glLinkProgram(it)
    }

    private val textureVertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCodeForTexture)
    private val textureFragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCodeForTexture)

    private val textureProgram = GLES20.glCreateProgram().also {
        GLES20.glAttachShader(it, textureVertexShader)
        GLES20.glAttachShader(it, textureFragmentShader)
        GLES20.glLinkProgram(it)
    }

    init {
        // Initialize vertex byte buffer for shape coordinates
        val bb = ByteBuffer.allocateDirect(axisCoords.size * 4)
        bb.order(ByteOrder.nativeOrder())

        vertexBuffer = bb.asFloatBuffer()
        vertexBuffer.put(axisCoords)
        vertexBuffer.position(0)
    }

    fun draw(mvpMatrix: FloatArray) {
        GLES20.glUseProgram(program)

        val positionHandle = GLES20.glGetAttribLocation(program, "vPosition")
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer)

        val colorHandle = GLES20.glGetUniformLocation(program, "vColor")
        val mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")

        GLES20.glLineWidth(5.0f)

        // Draw each axis
        for (i in 0 until 3) {
            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
            GLES20.glUniform4f(colorHandle, axisCoords[i * 12 + 3], axisCoords[i * 12 + 4], axisCoords[i * 12 + 5], 1.0f)
            GLES20.glDrawArrays(GLES20.GL_LINES, i * 2, 2)
        }

        GLES20.glDisableVertexAttribArray(positionHandle)

        // Draw arrows
        drawArrow(1.0f, 0.0f, 0.0f, 0xFFFF0000.toInt(), mvpMatrix) // X-axis arrow (red)
        drawArrow(0.0f, 1.0f, 0.0f, 0xFF00FF00.toInt(), mvpMatrix) // Y-axis arrow (green)
        drawArrow(0.0f, 0.0f, 1.0f, 0xFF0000FF.toInt(), mvpMatrix) // Z-axis arrow (blue)

        drawText("X", 0.95f, 0.15f, 0.0f, mvpMatrix, 60f, 0xFFFF0000.toInt()) // X-axis label
        drawText("Y", 0.1f, 1.0f, 0.0f, mvpMatrix, 60f, 0xFF00FF00.toInt()) // Y-axis label
        drawText("Z", 0.0f, -0.05f, 1.0f, mvpMatrix, 60f, 0xFF0000FF.toInt()) // Z-axis label

        drawTicks(mvpMatrix)
    }

    private fun drawArrow(x: Float, y: Float, z: Float, color: Int, mvpMatrix: FloatArray) {
        val arrowCoords = when {
            x != 0.0f -> floatArrayOf(
                x, y, z,
                x - 0.05f, y - 0.05f, z,
                x - 0.05f, y + 0.05f, z
            )
            y != 0.0f -> floatArrayOf(
                x, y, z,
                x - 0.05f, y - 0.05f, z,
                x + 0.05f, y - 0.05f, z
            )
            else -> floatArrayOf(
                x, y, z,
                x, y - 0.05f, z - 0.05f,
                x, y + 0.05f, z - 0.05f
            )
        }

        val arrowBuffer = ByteBuffer.allocateDirect(arrowCoords.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        arrowBuffer.put(arrowCoords).position(0)

        val colorHandle = GLES20.glGetUniformLocation(program, "vColor")
        val mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")

        GLES20.glUseProgram(program)

        val positionHandle = GLES20.glGetAttribLocation(program, "vPosition")
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, arrowBuffer)

        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
        GLES20.glUniform4f(colorHandle, Color.red(color) / 255.0f, Color.green(color) / 255.0f, Color.blue(color) / 255.0f, 1.0f)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3)

        GLES20.glDisableVertexAttribArray(positionHandle)
    }


    private fun drawTexturedQuad(textureId: Int, x: Float, y: Float, z: Float, width: Float, height: Float, mvpMatrix: FloatArray) {
        val vertices = floatArrayOf(
            x, y, z,
            x + width, y, z,
            x, y - height, z,
            x + width, y - height, z
        )

        val vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        vertexBuffer.put(vertices).position(0)

        val texCoords = floatArrayOf(
            0f, 0f,
            1f, 0f,
            0f, 1f,
            1f, 1f
        )

        val texCoordBuffer = ByteBuffer.allocateDirect(texCoords.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        texCoordBuffer.put(texCoords).position(0)

        GLES20.glUseProgram(textureProgram)

        val positionHandle = GLES20.glGetAttribLocation(textureProgram, "vPosition")
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, vertexBuffer)

        val texCoordHandle = GLES20.glGetAttribLocation(textureProgram, "aTexCoord")
        GLES20.glEnableVertexAttribArray(texCoordHandle)
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 2 * 4, texCoordBuffer)

        val mvpMatrixHandle = GLES20.glGetUniformLocation(textureProgram, "uMVPMatrix")
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)

        val textureHandle = GLES20.glGetUniformLocation(textureProgram, "uTexture")
        GLES20.glUniform1i(textureHandle, 0)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(texCoordHandle)
    }



    private fun createTextBitmap(text: String, textSize: Float, textColor: Int): Bitmap {
        val paint = Paint()
        paint.textSize = textSize
        paint.color = textColor
        paint.textAlign = Paint.Align.LEFT

        val baseline = -paint.ascent() // ascent() is negative
        val width = (paint.measureText(text) + 0.5f).toInt() // round
        val height = (baseline + paint.descent() + 0.5f).toInt()

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE) // Set the background color to white
        canvas.drawText(text, 0f, baseline, paint)
        return bitmap
    }

    private fun loadTexture(bitmap: Bitmap): Int {
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        val textureId = textures[0]

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

        return textureId
    }

    private fun drawText(text: String, x: Float, y: Float, z: Float, mvpMatrix: FloatArray, textSize: Float, textColor: Int) {
        val bitmap = createTextBitmap(text, textSize, textColor)
        val textureId = loadTexture(bitmap)

        drawTexturedQuad(textureId, x, y, z, bitmap.width * 0.001f, bitmap.height * 0.001f, mvpMatrix)
    }

    private fun drawTicks(mvpMatrix: FloatArray) {
        GLES20.glUseProgram(program)

        val positionHandle = GLES20.glGetAttribLocation(program, "vPosition")
        GLES20.glEnableVertexAttribArray(positionHandle)

        val colorHandle = GLES20.glGetUniformLocation(program, "vColor")
        val mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")

        GLES20.glLineWidth(2.0f)

        // Draw ticks for X axis
        for (i in -10.. 9) {
            val tick = i / 10.0f
            val tickCoords = floatArrayOf(
                tick, -0.02f, 0.0f,
                tick, 0.02f, 0.0f
            )
            val tickBuffer = ByteBuffer.allocateDirect(tickCoords.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
            tickBuffer.put(tickCoords).position(0)

            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
            GLES20.glUniform4f(colorHandle, 1.0f, 0.0f, 0.0f, 1.0f)
            GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, tickBuffer)
            GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2)
        }

        // Draw ticks for Y axis
        for (i in -10..9) {
            val tick = i / 10.0f
            val tickCoords = floatArrayOf(
                -0.02f, tick, 0.0f,
                0.02f, tick, 0.0f
            )
            val tickBuffer = ByteBuffer.allocateDirect(tickCoords.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
            tickBuffer.put(tickCoords).position(0)

            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
            GLES20.glUniform4f(colorHandle, 0.0f, 1.0f, 0.0f, 1.0f)
            GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, tickBuffer)
            GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2)
        }

        // Draw ticks for Z axis
        for (i in -10..9) {
            val tick = i / 10.0f
            val tickCoords = floatArrayOf(
                0.0f, -0.02f, tick,
                0.0f, 0.02f, tick
            )
            val tickBuffer = ByteBuffer.allocateDirect(tickCoords.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
            tickBuffer.put(tickCoords).position(0)

            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
            GLES20.glUniform4f(colorHandle, 0.0f, 0.0f, 1.0f, 1.0f)
            GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, tickBuffer)
            GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2)
        }

        GLES20.glDisableVertexAttribArray(positionHandle)
    }
}